package in.andonsystem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.andonsystem.v2.activity.HomeActivity;
import in.andonsystem.v2.entity.Buyer;
import in.andonsystem.v2.entity.DaoSession;
import in.andonsystem.v2.entity.User;
import in.andonsystem.v2.entity.UserDao;
import in.andonsystem.v2.service.BuyerService;
import in.andonsystem.v2.service.UserService;
import in.andonsystem.v2.util.Constants;
import in.andonsystem.v2.util.MiscUtil;

public class LoadingActivity extends AppCompatActivity {
    private final String TAG = LoadingActivity.class.getSimpleName();

    private BuyerService buyerService;
    private UserService userService;
    private ProgressBar progress;
    private SharedPreferences appPref;
    private int noOfRequest = 0;
    private App app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        Log.d(TAG,"onCreate()");

        buyerService = new BuyerService((App)getApplication());
        userService = new UserService((App)getApplication());
        progress = (ProgressBar)findViewById(R.id.loading_progress);
        appPref = getSharedPreferences(Constants.APP_PREF,0);
        app = (App) getApplication();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart()");

        AlertDialog dialog = getAlertDialog();
        Boolean isConnected = MiscUtil.isConnectedToInternet(this);

        if(!isConnected){
            dialog.show();
        }else{
            progress.setVisibility(View.VISIBLE);

            Boolean firstLaunch = appPref.getBoolean(Constants.FIRST_LAUNCH,true);
            Log.i(TAG, "first launch: " + firstLaunch);
            if(firstLaunch){
                init();
                //set first launch to false
            }else {

                //Set teams and problems in App
                String problems = appPref.getString(Constants.APP_PROBLEMS,"");
                app.setProblems(problems.split(";"));
                String teams = appPref.getString(Constants.APP_TEAMS,"");
                app.setTeams(teams.split(";"));

                String url = Constants.API_BASE_URL + "/misc/config?version=" + appPref.getString(Constants.APP_VERSION,"");
                Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Config Response :" + response.toString());

                        try {
                            if(response.has("update") && response.getBoolean("update")){
                                Log.d(TAG, "Update application");
                                progress.setVisibility(View.GONE);
                            }
                            if(response.getBoolean("initialize")){
                                init();
                            }
                            if(response.has("version")){
                                String version = response.getString("version");
                                appPref.edit().putString(Constants.APP_VERSION,version).commit();
                                progress.setVisibility(View.GONE);
                            }
                            //logic for transition to login
                            if(!response.getBoolean("initialize") && (!response.has("update") || !response.getBoolean("update"))){
                                goToLogin();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.getMessage());
                    }
                };

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener);
                request.setTag(TAG);
                AppController.getInstance().addToRequestQueue(request);
            }
        }
    }

    private void init(){
        AppController appController = AppController.getInstance();
        buyerService.deleteAll();
        userService.deleteAll();
        /////////////////////////// get Buyers ////////////////////////
        String url1 = Constants.API_BASE_URL + "/buyers";
        Response.Listener<JSONArray> listener1 = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.i(TAG, "buyers Response :" + response.toString());
                try {
                    List<Buyer> buyers = new ArrayList<>();
                    JSONObject obj;
                    for (int i = 0; i < response.length(); i++) {
                        obj = response.getJSONObject(i);
                        buyers.add(new Buyer(obj.getLong("id"), obj.getString("name"), obj.getString("team")));
                    }

                    buyerService.saveAll(buyers);
                }catch (Exception e){
                    e.printStackTrace();
                }
                goToLoginAfterInit();
            }
        };
        Response.ErrorListener errorListener1 = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        };
        JsonArrayRequest request1 = new JsonArrayRequest(Request.Method.GET,url1,null,listener1,errorListener1);
        request1.setTag(TAG);
        appController.addToRequestQueue(request1);

        /////////////////////////////// get problems ////////////////////////////
        String url2 = Constants.API_BASE_URL + "/problems";
        Response.Listener<JSONArray> listener2 = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.i(TAG, "problem Response :" + response.toString());

                String[] problems = new String[response.length()];
                try {
                    for (int i = 0; i < response.length(); i++){
                        problems[i] = response.getString(i);
                    }
                    app.setProblems(problems);
                    StringBuilder builder = new StringBuilder();
                    for (String p: problems){
                        builder.append(p + ";");
                    }
                    builder.setLength(builder.length()-1);
                    appPref.edit().putString(Constants.APP_PROBLEMS,builder.toString()).commit();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                goToLoginAfterInit();
            }
        };
        Response.ErrorListener errorListener2 = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        };
        JsonArrayRequest request2 = new JsonArrayRequest(Request.Method.GET,url2,null,listener2,errorListener2);
        request2.setTag(TAG);
        appController.addToRequestQueue(request2);

        //get Teams
        String url3 = Constants.API_BASE_URL + "/teams";
        Response.Listener<JSONArray> listener3 = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.i(TAG, "teams Response :" + response.toString());

                String[] teams = new String[response.length()];
                try {
                    for (int i = 0; i < response.length(); i++){
                        teams[i] = response.getString(i);
                    }
                    app.setTeams(teams);
                    StringBuilder builder = new StringBuilder();
                    for (String t: teams){
                        builder.append(t + ";");
                    }
                    builder.setLength(builder.length()-1);
                    appPref.edit().putString(Constants.APP_TEAMS,builder.toString()).commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                goToLoginAfterInit();
            }
        };
        Response.ErrorListener errorListener3 = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        };
        JsonArrayRequest request3 = new JsonArrayRequest(Request.Method.GET,url3,null,listener3,errorListener3);
        request3.setTag(TAG);
        appController.addToRequestQueue(request3);

        /////////////////////////// get Users ////////////////////////
        String url4 = Constants.API_BASE_URL + "/users";
        Response.Listener<JSONArray> listener4 = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.i(TAG, "users Response :" + response.toString());
                try {
                    List<User> users = new ArrayList<>();
                    JSONObject obj;
                    for (int i = 0; i < response.length(); i++) {
                        obj = response.getJSONObject(i);
                        users.add(new User( obj.getLong("id"),
                                            obj.getString("name"),
                                            obj.getString("email"),
                                            obj.getString("mobile"),
                                            obj.getString("role"),
                                            obj.getString("userType"),
                                            obj.getString("level")
                                ));
                    }

                    userService.saveOrUpdate(users);
                }catch (Exception e){
                    e.printStackTrace();
                }
                goToLoginAfterInit();
            }
        };
        Response.ErrorListener errorListener4 = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        };
        JsonArrayRequest request4 = new JsonArrayRequest(Request.Method.GET,url4,null,listener4,errorListener4);
        request4.setTag(TAG);
        appController.addToRequestQueue(request4);
    }

    public void goToLoginAfterInit(){
        Log.i(TAG, "goToLoginAfterInit()");
        noOfRequest++;
        if(noOfRequest == 3){
            Log.i(TAG, "setting first launch to false");
            appPref.edit().putBoolean(Constants.FIRST_LAUNCH,false).commit();
            progress.setVisibility(View.GONE);

            goToLogin();
        }
    }

    public void  goToLogin(){
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
    }

    private AlertDialog getAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet");
        builder.setMessage("No Internet Connection Available.Do you want to try again?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                onStart();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        return builder.create();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        Log.i(TAG,"finish()");
    }

}
