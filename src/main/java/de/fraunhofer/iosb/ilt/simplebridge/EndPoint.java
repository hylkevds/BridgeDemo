package de.fraunhofer.iosb.ilt.simplebridge;

import com.google.gson.annotations.Expose;
import de.fraunhofer.iosb.ilt.configurable.AnnotatedConfigurable;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableClass;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorList;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author hylke
 */
@ConfigurableClass()
public class EndPoint implements AnnotatedConfigurable<Void, Void> {

    @ConfigurableField(editor = EditorString.class, optional = true,
            label = "LinkUrl", description = "The Bridges that this endpoint belongs to.")
    @EditorString.EdOptsString(dflt = "")
    @Expose
    private String linkUrl;

    @ConfigurableField(editor = EditorString.class, optional = false,
            label = "BaseUrl", description = "The URL to reach this endpoint on.")
    @EditorString.EdOptsString(dflt = "http://server.de/path/to/api")
    @Expose
    private String baseUrl;

    @ConfigurableField(editor = EditorString.class, optional = false,
            label = "ResourceId", description = "The ID used to access this resource.")
    @EditorString.EdOptsString(dflt = "")
    @Expose
    private String resourceId = UUID.randomUUID().toString();

    @Expose
    private String localUrl = "";

    @ConfigurableField(editor = EditorList.class, optional = true,
            label = "Allowed Headers", description = "Allowed Request headers")
    @EditorList.EdOptsList(editor = EditorString.class)
    @EditorString.EdOptsString()
    @Expose
    private List<String> headerAllowlist;

    @ConfigurableField(editor = EditorList.class, optional = true,
            label = "Allowed Methods", description = "Allowed HTTP Methods")
    @EditorList.EdOptsList(editor = EditorString.class)
    @EditorString.EdOptsString()
    @Expose
    private List<String> methodsAllowlist;

    /**
     * @return the linkUrl
     */
    public String getLinkUrl() {
        return linkUrl;
    }

    /**
     * @param linkUrl the linkUrl to set
     */
    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
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
     * @return the resourceId
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * @param resourceId the resourceId to set
     */
    public void setResourceId(String resourceId) {
        if (resourceId.isEmpty()) {
            return;
        }
        this.resourceId = resourceId;
    }

    /**
     * @return the localUrl
     */
    public String getLocalUrl() {
        return localUrl;
    }

    /**
     * @param localUrl the localUrl to set
     */
    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    /**
     * @return the headerAllowlist
     */
    public List<String> getHeaderAllowlist() {
        return headerAllowlist;
    }

    /**
     * @param headerAllowlist the headerAllowlist to set
     */
    public void setHeaderAllowlist(List<String> headerAllowlist) {
        this.headerAllowlist = headerAllowlist;
    }

    /**
     * @return the methodsAllowlist
     */
    public List<String> getMethodsAllowlist() {
        return methodsAllowlist;
    }

    /**
     * @param methodsAllowlist the methodsAllowlist to set
     */
    public void setMethodsAllowlist(List<String> methodsAllowlist) {
        this.methodsAllowlist = methodsAllowlist;
    }

}
