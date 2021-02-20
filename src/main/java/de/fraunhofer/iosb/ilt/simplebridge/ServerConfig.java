package de.fraunhofer.iosb.ilt.simplebridge;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.Expose;
import de.fraunhofer.iosb.ilt.configurable.AnnotatedConfigurable;
import de.fraunhofer.iosb.ilt.configurable.ConfigurationException;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableClass;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorClass;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorInt;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author hylke
 */
@ConfigurableClass()
public class ServerConfig implements AnnotatedConfigurable<Void, Void> {

    public static final String TAG_SERVER_CONFIG = "serverConfig";

    @ConfigurableField(editor = EditorInt.class, optional = false,
            label = "Port", description = "The port to listen on")
    @EditorInt.EdOptsInt(dflt = 1337, min = 1024, max = 65535)
    @Expose
    private int port;

    @ConfigurableField(editor = EditorString.class, optional = false,
            label = "baseUrl", description = "The URL this Bridge can be reached on.")
    @EditorString.EdOptsString(dflt = "http://localhost/")
    @Expose
    private String baseUrl;

    @ConfigurableField(editor = EditorList.class, optional = true,
            label = "LinkServers", description = "Bridges to communicate with")
    @EditorList.EdOptsList(editor = EditorString.class)
    @EditorString.EdOptsString()
    @Expose
    private List<String> linkServers;

    @ConfigurableField(editor = EditorList.class, optional = true,
            label = "EndPoints", description = "REST EndPoints to expose")
    @EditorList.EdOptsList(editor = EditorClass.class)
    @EditorClass.EdOptsClass(clazz = EndPoint.class)
    @Expose
    private List<EndPoint> endpoints;

    private final Map<String, EndPoint> endpointsById = new HashMap<>();

    private boolean inited = false;

    public static ServerConfig fromString(String config) throws ConfigurationException {
        JsonElement json = JsonParser.parseString(config);
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.configure(json, null, null, null);
        return serverConfig;
    }

    public static ServerConfig getServerConfig(HttpServletRequest request) {
        ServerConfig serverConfig = (ServerConfig) request.getServletContext().getAttribute(ServerConfig.TAG_SERVER_CONFIG);
        if (!serverConfig.inited) {
            if (!serverConfig.baseUrl.endsWith("/")) {
                serverConfig.baseUrl += '/';
            }
            serverConfig.inited = true;
            String baseUrl = serverConfig.baseUrl + "resource/";
            for (EndPoint endpoint : serverConfig.endpoints) {
                endpoint.setLocalUrl(baseUrl + endpoint.getResourceId() + "/");
            }
        }
        return serverConfig;
    }

    public String toJson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create()
                .toJson(this);
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return the linkServers
     */
    public List<String> getLinkServers() {
        return linkServers;
    }

    /**
     * @param linkServers the linkServers to set
     */
    public void setLinkServers(List<String> linkServers) {
        this.linkServers = linkServers;
    }

    /**
     * @return the endpoints
     */
    public List<EndPoint> getEndpoints() {
        return endpoints;
    }

    public EndPoint getEndpoint(String id) {
        if (endpointsById.isEmpty() && !endpoints.isEmpty()) {
            for (EndPoint endpoint : endpoints) {
                endpointsById.put(endpoint.getResourceId(), endpoint);
            }
        }
        return endpointsById.get(id);
    }

    /**
     * @param endpoints the endpoints to set
     */
    public void setEndpoints(List<EndPoint> endpoints) {
        this.endpoints = endpoints;
    }

}
