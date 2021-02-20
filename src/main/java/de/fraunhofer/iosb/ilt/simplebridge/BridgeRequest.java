package de.fraunhofer.iosb.ilt.simplebridge;

import com.google.gson.annotations.Expose;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hylke
 */
public class BridgeRequest {

    @Expose
    public String resourceId;
    @Expose
    public String url;
    @Expose
    public String method;
    @Expose
    public Map<String, String> headers = new HashMap<>();
    @Expose
    public byte[] body;

}
