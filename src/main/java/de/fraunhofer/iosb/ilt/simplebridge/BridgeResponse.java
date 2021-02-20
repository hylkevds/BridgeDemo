package de.fraunhofer.iosb.ilt.simplebridge;

import com.google.gson.annotations.Expose;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hylke
 */
public class BridgeResponse {

    @Expose
    public int statusCode;
    @Expose
    public Map<String, String> headers = new HashMap<>();
    @Expose
    public byte[] body = new byte[0];

}
