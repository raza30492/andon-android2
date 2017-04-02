package in.andonsystem.v2.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import in.andonsystem.AppController;
import in.andonsystem.v2.authenticator.AuthConstants;

/**
 * Created by razamd on 4/2/2017.
 */

public class LoginUtil {
    private static final String TAG = LoginUtil.class.getSimpleName();

    public static Bundle authenticate(final String username, final String password){
        Log.d(TAG,"authenticate()");
        String url = "http://zahidraza.in/andon-system/oauth/token?grant_type=password&username=" + username + "&password=" + password;
        Bundle result = new Bundle();

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,url,null,future,future){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                //params.put("Authorization", "Basic " + Base64.encodeToString("client:secret".getBytes(),0));
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);

        JSONObject response = null;
        try {
            response = future.get(30, TimeUnit.SECONDS);
            Log.i(TAG,response.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            if(e.getCause() instanceof ServerError) {
                ServerError error = (ServerError) e.getCause();
                NetworkResponse resp = error.networkResponse;
                Log.i(TAG, "response: " + resp.toString());
                Log.i(TAG, "response status: " + resp.statusCode);
                if(resp.statusCode == 400){
                    result.putString(AuthConstants.ARG_AUTHENTICATION_ERROR,"Incorrect credentials. Try again");
                }
                if(resp.statusCode == 401){
                    result.putString(AuthConstants.ARG_AUTHENTICATION_ERROR,"Client is not authorized.");
                }
            }

        } catch (TimeoutException e) {
            e.printStackTrace();
            result.putString(AuthConstants.ARG_AUTHENTICATION_ERROR,"Check your Internet Connection.");
        }


        if(response != null){
            Log.i(TAG,"login response: " + response.toString());
            try {
                result.putString(AuthConstants.ARG_ACCESS_TOKEN, response.getString("access_token"));
                result.putString(AuthConstants.ARG_REFRESH_TOKEN, response.getString("refresh_token"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Bundle authenticateWithRefreshToken(final String refreshToken){
        Log.d(TAG,"authenticateWithRefreshToken()");
        String url = "http://zahidraza.in/andon-system/oauth/token?grant_type=refresh_token&refresh_token=" + refreshToken;
        return login(url);
    }
    private static Bundle login(String url){
        Log.d(TAG,"login()");
        Log.d(TAG,"loginUrl: " + url);
        Bundle result = new Bundle();

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,url,null,future,future){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("Authorization", "Basic " + Base64.encodeToString("client:secret".getBytes(),0));
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(request);

        JSONObject response = null;
        try {
            response = future.get(30, TimeUnit.SECONDS);
            Log.i(TAG,response.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            if(e.getCause() instanceof ServerError) {
                ServerError error = (ServerError) e.getCause();
                NetworkResponse resp = error.networkResponse;
                Log.i(TAG, "response: " + resp.toString());
                Log.i(TAG, "response status: " + resp.statusCode);
                if(resp.statusCode == 400){
                    result.putString(AuthConstants.ARG_AUTHENTICATION_ERROR,"Incorrect credentials. Try again");
                }
                if(resp.statusCode == 401){
                    result.putString(AuthConstants.ARG_AUTHENTICATION_ERROR,"Client is not authorized.");
                }
            }

        } catch (TimeoutException e) {
            e.printStackTrace();
            result.putString(AuthConstants.ARG_AUTHENTICATION_ERROR,"Check your Internet Connection.");
        }


        if(response != null){
            Log.i(TAG,"login response: " + response.toString());
            try {
                result.putString(AuthConstants.ARG_ACCESS_TOKEN, response.getString("access_token"));
                result.putString(AuthConstants.ARG_REFRESH_TOKEN, response.getString("refresh_token"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
