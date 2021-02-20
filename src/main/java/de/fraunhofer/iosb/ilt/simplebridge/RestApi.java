package de.fraunhofer.iosb.ilt.simplebridge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fraunhofer.iosb.ilt.configurable.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
@WebServlet(
        name = "RestBridge",
        urlPatterns = {"/resource/*"},
        initParams = {
            @WebInitParam(name = "readonly", value = "false")
        }
)
@MultipartConfig()
public class RestApi extends HttpServlet {

    public static final String ENCODING_UTF8 = "UTF-8";
    public static final String CONTENT_TYPE_APPLICATIONJSON = "application/json";
    public static final String CONTENT_TYPE_APPLICATIONGEOJSON = "application/geo+json";

    private static final Logger LOGGER = LoggerFactory.getLogger(RestApi.class.getName());

    private static CloseableHttpClient client;
    private static Gson gson;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServerConfig serverConfig = ServerConfig.getServerConfig(request);

        String pathInfo = request.getPathInfo();
        if (Utils.isNullOrEmpty(pathInfo) || "/".equals(pathInfo)) {
            getIndex(response, serverConfig);
            LOGGER.info("Index Request.");
            return;
        }

        EndPoint endpoint = getEndpoint(request, serverConfig);
        if (endpoint == null) {
            response.setStatus(404);
            return;
        }

        BridgeRequest bridgeRequest = createBridgeRequest(request, serverConfig, endpoint);
        if (bridgeRequest == null) {
            response.setStatus(403);
            return;
        }
        if (!endpoint.getMethodsAllowlist().contains(bridgeRequest.method)) {
            response.setStatus(403);
            return;
        }
        LOGGER.info("Request: {} {} {}", bridgeRequest.method, bridgeRequest.resourceId, bridgeRequest.url);
        LOGGER.info("  Headers: {}, bodySize {}", bridgeRequest.headers.size(), bridgeRequest.body.length);
        final String linkUrl = endpoint.getLinkUrl();
        BridgeResponse bridgeResponse;
        if (Utils.isNullOrEmpty(linkUrl)) {
            bridgeResponse = LinkApi.executeRequest(endpoint, bridgeRequest);
        } else {
            bridgeResponse = sendBridgeRequest(bridgeRequest, endpoint);
        }
        LOGGER.info("  Response {}, Headers: {}, bodySize {}", bridgeResponse.statusCode, bridgeResponse.headers.size(), bridgeResponse.body.length);
        response.setStatus(bridgeResponse.statusCode);
        for (Map.Entry<String, String> entry : bridgeResponse.headers.entrySet()) {
            final String headerName = entry.getKey();
            if ("Content-Length".equals(headerName)) {
                // Don't pass this one, it may be incorrect.
                continue;
            }
            response.addHeader(headerName, entry.getValue());
        }
        String contentType = bridgeResponse.headers.get("Content-Type");
        if (contentType != null && contentType.startsWith(CONTENT_TYPE_APPLICATIONJSON)) {
            final String localUrl = endpoint.getLocalUrl();
            final String baseUrl = endpoint.getBaseUrl();
            String data = StringUtils.replace(new String(bridgeResponse.body, ENCODING_UTF8), baseUrl, localUrl.substring(0, localUrl.length() - 1));
            response.getWriter().write(data);
        } else {
            response.getOutputStream().write(bridgeResponse.body);
        }
    }

    private EndPoint getEndpoint(HttpServletRequest request, ServerConfig serverConfig) {
        String pathInfo = request.getPathInfo();
        String resourceId = pathInfo.substring(1, pathInfo.indexOf('/', 1));
        return serverConfig.getEndpoint(resourceId);
    }

    private BridgeRequest createBridgeRequest(HttpServletRequest request, ServerConfig serverConfig, EndPoint endpoint) {
        final String method = request.getMethod();
        String pathInfo = request.getPathInfo();
        String queryString = request.getQueryString();

        BridgeRequest bridgeRequest = new BridgeRequest();
        bridgeRequest.method = method;
        bridgeRequest.resourceId = endpoint.getResourceId();

        bridgeRequest.url = pathInfo.substring(pathInfo.indexOf('/', 1));
        if (queryString != null && !queryString.isEmpty()) {
            bridgeRequest.url += "?" + queryString;
        }

        byte[] requestData;
        try {
            requestData = readRequestData(request.getInputStream());
            bridgeRequest.body = requestData;
        } catch (IOException ex) {
            LOGGER.warn("Failed to read body");
        }

        for (String headerName : endpoint.getHeaderAllowlist()) {
            String header = request.getHeader(headerName);
            if (!Utils.isNullOrEmpty(header)) {
                bridgeRequest.headers.put(headerName, header);
            }
        }
        return bridgeRequest;
    }

    public static BridgeResponse sendBridgeRequest(BridgeRequest bridgeRequest, EndPoint endPoint) {
        HttpPost httpPost = new HttpPost(endPoint.getLinkUrl() + "/link/");
        String requestData = getGson().toJson(bridgeRequest);
        httpPost.setEntity(new StringEntity(requestData, ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = getHttpClient().execute(httpPost)) {
            String responseData = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
            return getGson().fromJson(responseData, BridgeResponse.class);
        } catch (IOException ex) {
            LOGGER.error("Failed to execute request", ex);
        }
        BridgeResponse response = new BridgeResponse();
        response.statusCode = 500;
        response.body = new byte[0];
        return response;
    }

    private void getIndex(HttpServletResponse response, ServerConfig serverConfig) {
        response.setContentType(CONTENT_TYPE_APPLICATIONJSON);
        response.setCharacterEncoding(ENCODING_UTF8);
        try {
            List<EndPoint> endpoints = serverConfig.getEndpoints();
            String asJson = getGson().toJson(endpoints);
            response.getWriter().write(asJson);
        } catch (IOException ex) {
            LOGGER.error("Failed to write response", ex);
        }
    }

    private static CloseableHttpClient getHttpClient() {
        if (client == null) {
            client = HttpClients.createSystem();
        }
        return client;
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
        }
        return gson;
    }

    private static byte[] readRequestData(InputStream reader) {
        try {
            return reader.readAllBytes();
        } catch (IOException ex) {
            LOGGER.error("Failed to read body", ex);
            return new byte[0];
        }
    }
}
