package de.fraunhofer.iosb.ilt.simplebridge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.fraunhofer.iosb.ilt.configurable.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ByteArrayEntity;
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
        name = "BridgeLink",
        urlPatterns = {"/link/*"},
        initParams = {
            @WebInitParam(name = "readonly", value = "false")
        }
)
@MultipartConfig()
public class LinkApi extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkApi.class.getName());
    private static CloseableHttpClient client;
    private static Gson gson;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServerConfig serverConfig = ServerConfig.getServerConfig(request);
        getIndex(response, serverConfig);
        LOGGER.info("Index Request.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServerConfig serverConfig = ServerConfig.getServerConfig(request);
        String requestData = readRequestData(request.getReader());
        BridgeRequest bridgeRequest = parseBridgeRequest(requestData);
        LOGGER.info("Request: {} {} {}", bridgeRequest.method, bridgeRequest.resourceId, bridgeRequest.url);
        LOGGER.info("  Headers: {}, bodySize {}", bridgeRequest.headers.size(), bridgeRequest.body.length);
        EndPoint endpoint = serverConfig.getEndpoint(bridgeRequest.resourceId);
        BridgeResponse bridgeResponse = executeRequest(endpoint, bridgeRequest);
        LOGGER.info("  Response {}, Headers: {}, bodySize {}", bridgeResponse.statusCode, bridgeResponse.headers.size(), bridgeResponse.body.length);
        response.getWriter().write(getGson().toJson(bridgeResponse));
    }

    public static BridgeResponse executeRequest(EndPoint endpoint, BridgeRequest bridgeRequest) {
        BridgeResponse bridgeResponse = new BridgeResponse();

        final String linkUrl = endpoint.getLinkUrl();
        if (Utils.isNullOrEmpty(linkUrl)) {
            String targetUrl = endpoint.getBaseUrl() + bridgeRequest.url;
            LOGGER.info("  Target URL: {}", targetUrl);
            CloseableHttpClient httpClient = getHttpClient();
            final String method = bridgeRequest.method;
            HttpEntityEnclosingRequestBase httpRequest = new HttpEntityEnclosingRequestBase() {
                @Override
                public String getMethod() {
                    return method;
                }
            };
            httpRequest.setURI(URI.create(targetUrl));
            if (bridgeRequest.body != null && bridgeRequest.body.length != 0) {
                httpRequest.setEntity(new ByteArrayEntity(bridgeRequest.body));
            }

            for (Map.Entry<String, String> entry : bridgeRequest.headers.entrySet()) {
                httpRequest.setHeader(entry.getKey(), entry.getValue());
            }

            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                bridgeResponse.statusCode = response.getStatusLine().getStatusCode();
                for (Header header : response.getAllHeaders()) {
                    bridgeResponse.headers.put(header.getName(), header.getValue());
                }
                bridgeResponse.body = EntityUtils.toByteArray(response.getEntity());
            } catch (IOException ex) {
                bridgeResponse.statusCode = 500;
            }
        }
        return bridgeResponse;
    }

    private BridgeRequest parseBridgeRequest(String data) {
        return getGson().fromJson(data, BridgeRequest.class);
    }

    private void getIndex(HttpServletResponse response, ServerConfig serverConfig) {
        response.setContentType(RestApi.CONTENT_TYPE_APPLICATIONJSON);
        response.setCharacterEncoding(RestApi.ENCODING_UTF8);
        try {
            response.getWriter().write(serverConfig.toJson());
        } catch (IOException ex) {
            LOGGER.error("Failed to write response", ex);
        }
    }

    private static String readRequestData(BufferedReader reader) {
        return reader.lines().collect(Collectors.joining("\n"));
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

}
