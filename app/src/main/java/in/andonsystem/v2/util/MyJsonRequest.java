package in.andonsystem.v2.util;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by razamd on 4/2/2017.
 */

public class MyJsonRequest extends JsonObjectRequest {
    public MyJsonRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener, String authToken) {
        super(method, url, jsonRequest, listener, errorListener);
    }
}
