package in.andonsystem;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.andonsystem.v2.activity.HomeActivity2;
import in.andonsystem.v2.authenticator.AuthConstants;
import in.andonsystem.v2.entity.Buyer;
import in.andonsystem.v2.entity.User;
import in.andonsystem.v2.service.BuyerService;
import in.andonsystem.v2.service.IssueService;
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
        Log.d(TAG, "onCreate()");

        app = (App) getApplication();
        buyerService = new BuyerService(app);
        userService = new UserService(app);
        progress = (ProgressBar) findViewById(R.id.loading_progress);
        appPref = getSharedPreferences(Constants.APP_PREF, 0);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

        AlertDialog dialog = getAlertDialog();
        Boolean isConnected = MiscUtil.isConnectedToInternet(this);

        if (!isConnected) {
            dialog.show();
        } else {
            progress.setVisibility(View.VISIBLE);

            Boolean firstLaunch = appPref.getBoolean(Constants.FIRST_LAUNCH, true);
            Log.i(TAG, "first launch: " + firstLaunch);
            if (firstLaunch) {
                init();
                //set first launch to false
            } else {
                //Delete older issue
                new IssueService(app).deleteAllOlder();
                //Set teams and problems in App
                String problems = appPref.getString(Constants.APP_PROBLEMS, "");
                app.setProblems(problems.split(";"));
                String teams = appPref.getString(Constants.APP_TEAMS, "");
                app.setTeams(teams.split(";"));

                String url = Constants.API_BASE_URL + "/misc/config?version=" + appPref.getString(Constants.APP_VERSION, "");
                Log.i(TAG, "config url: " + url);
                Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Config Response :" + response.toString());

                        try {
                            if (response.has("update") && response.getBoolean("update")) {
                                Log.d(TAG, "Update application");
                                progress.setVisibility(View.GONE);
                            }
                            if (response.getBoolean("initialize")) {
                                init();
                            }
                            if (response.has("version")) {
                                String version = response.getString("version");
                                appPref.edit().putString(Constants.APP_VERSION, version).commit();
                                progress.setVisibility(View.GONE);
                            }
                            //logic for transition to login
                            if (!response.getBoolean("initialize") && (!response.has("update") || !response.getBoolean("update"))) {
                                goToLogin();
                                //getTokenForAccountCreateIfNeeded();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                };
                Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e(TAG, error.getMessage());
                    }
                };

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, listener, errorListener);
                request.setTag(TAG);
                AppController.getInstance().addToRequestQueue(request);
            }
        }
    }

    private void init() {
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
                } catch (Exception e) {
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
        JsonArrayRequest request1 = new JsonArrayRequest(Request.Method.GET, url1, null, listener1, errorListener1);
        request1.setTag(TAG);
        appController.addToRequestQueue(request1);

        /////////////////////////////// get problems ////////////////////////////
        String url2 = Constants.API_BASE_URL + "/problems";
        Response.Listener<JSONArray> listener2 = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.i(TAG, "problem Response :" + response.toString());

                String[] problems = new String[response.length() + 1];
                problems[0] = "Select Problem";
                try {
                    for (int i = 0; i < response.length(); i++) {
                        problems[i + 1] = response.getString(i);
                    }
                    app.setProblems(problems);
                    StringBuilder builder = new StringBuilder();
                    for (String p : problems) {
                        builder.append(p + ";");
                    }
                    builder.setLength(builder.length() - 1);
                    appPref.edit().putString(Constants.APP_PROBLEMS, builder.toString()).commit();

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
        JsonArrayRequest request2 = new JsonArrayRequest(Request.Method.GET, url2, null, listener2, errorListener2);
        request2.setTag(TAG);
        appController.addToRequestQueue(request2);

        //get Teams
        String url3 = Constants.API_BASE_URL + "/teams";
        Response.Listener<JSONArray> listener3 = new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.i(TAG, "teams Response :" + response.toString());

                String[] teams = new String[response.length() + 1];
                teams[0] = "Select Team";
                try {
                    for (int i = 0; i < response.length(); i++) {
                        teams[i + 1] = response.getString(i);
                    }
                    app.setTeams(teams);
                    StringBuilder builder = new StringBuilder();
                    for (String t : teams) {
                        builder.append(t + ";");
                    }
                    builder.setLength(builder.length() - 1);
                    appPref.edit().putString(Constants.APP_TEAMS, builder.toString()).commit();
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
        JsonArrayRequest request3 = new JsonArrayRequest(Request.Method.GET, url3, null, listener3, errorListener3);
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
                    JSONObject u;
                    for (int i = 0; i < response.length(); i++) {
                        u = response.getJSONObject(i);
                        users.add(new User(u.getLong("id"),
                                u.getString("name"),
                                u.getString("email"),
                                u.getString("mobile"),
                                u.getString("role"),
                                u.getString("userType"),
                                u.getString("level")
                        ));
                    }

                    userService.saveOrUpdate(users);
                } catch (Exception e) {
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
        JsonArrayRequest request4 = new JsonArrayRequest(Request.Method.GET, url4, null, listener4, errorListener4);
        request4.setTag(TAG);
        appController.addToRequestQueue(request4);
    }

    public void goToLoginAfterInit() {
        Log.i(TAG, "goToLoginAfterInit()");
        noOfRequest++;
        if (noOfRequest == 4) {
            Log.i(TAG, "setting first launch to false");
            appPref.edit().putBoolean(Constants.FIRST_LAUNCH, false).commit();
            progress.setVisibility(View.GONE);

            goToLogin();
        }
    }

    public void goToLogin() {
        //getTokenForAccountCreateIfNeeded();
        Intent i = new Intent(this, AuthActivity.class);
        startActivity(i);
    }

    private AlertDialog getAlertDialog() {
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

    private void getTokenForAccountCreateIfNeeded() {
        AccountManager mAccountManager = AccountManager.get(this);
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(AuthConstants.VALUE_ACCOUNT_TYPE, AuthConstants.AUTH_TOKEN_TYPE_FULL_ACCESS, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                            showMessage(((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL"));
                            Log.d("udinic", "GetTokenForAccount Bundle is " + bnd);
                            goToLogin();

                        } catch (Exception e) {
                            e.printStackTrace();
                            showMessage(e.getMessage());
                        }
                    }
                }
                , null);
    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void getExistingAccountAuthToken() {
//        AccountManager mAccountManager = AccountManager.get(this);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Account[] availableAccounts = mAccountManager.getAccountsByType(AuthConstants.VALUE_ACCOUNT_TYPE);
//
//        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Bundle bnd = future.getResult();
//
//                    final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
//                    showMessage((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL");
//                    Log.d("udinic", "GetToken Bundle is " + bnd);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    showMessage(e.getMessage());
//                }
//            }
//        }).start();
//    }

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
