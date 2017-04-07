package in.andonsystem.v2.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import in.andonsystem.App;
import in.andonsystem.AppController;
import in.andonsystem.R;
import in.andonsystem.v2.adapter.AdapterHome;
import in.andonsystem.v2.authenticator.AuthConstants;
import in.andonsystem.v2.entity.Issue;
import in.andonsystem.v2.entity.User;
import in.andonsystem.v2.service.IssueService;
import in.andonsystem.v2.service.UserService;
import in.andonsystem.v2.util.Constants;
import in.andonsystem.v2.util.MyJsonRequest;

public class HomeActivity extends AppCompatActivity {
    
    private final String TAG = HomeActivity.class.getSimpleName();
    private final String USER_FACTORY = "FACTORY";
    private final String USER_SAMPLING = "SAMPLING";
    private final String USER_MERCHANDISING = "MERCHANDISING";
    private final long ACCOUNT_ADD = 10L;
    private final long ACCOUNT_MANAGE = 11L;

    private RelativeLayout container;
    private SwipeRefreshLayout refreshLayout2;
    private RecyclerView recyclerView;
    private TextView emptyMessage;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private boolean rvAdded;  //Recycler view added?

    /*App2 specific*/
    private Spinner teamFilter;
    private String selectedTeam = "All Team";
    AdapterHome rvAdapter2;
    
    
    private int appNo;
    private int accountSelected = -1;
    private Context mContext;
    private App app;
    private IssueService issueService;
    private UserService userService;
    private SharedPreferences syncPref;
    private SharedPreferences userPref;

    private AccountManager mAccountManager;
    private List<String> accountList = new ArrayList<>();
    private AccountHeader accountHeader;
    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        mContext = this;
        app = (App)getApplication();
        mAccountManager = AccountManager.get(this);
        issueService = new IssueService(app);
        userService = new UserService(app);
        syncPref = getSharedPreferences(Constants.SYNC_PREF,0);
        userPref = getSharedPreferences(Constants.USER_PREF,0);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, RaiseIssueActivity2.class);
                startActivity(i);
            }
        });
        container = (RelativeLayout) findViewById(R.id.content_home);
        prepareScreen();
        buildAccountHeader();
        buildDrawer();

        Account[] accounts = mAccountManager.getAccountsByType(AuthConstants.VALUE_ACCOUNT_TYPE);
        if(accounts.length == 0){
            getTokenForAccountCreateIfNeeded();
        }else {
            String email = userPref.getString(Constants.USER_EMAIL, null);
            User user = userService.findByEmail(email);
            chooseScreen(user.getUserType());
            onAccountChange();
            ProfileDrawerItem profile;
            for(int i = 0; i < accounts.length; i++){
                Account a = accounts[i];
                accountList.add(a.name);
                profile = new ProfileDrawerItem().withEmail(a.name).withName(a.name).withIcon(getResources().getDrawable(R.drawable.profile1)).withIdentifier(i);
                if(email == null && i == 0){
                    profile.withSetSelected(true);
                    accountSelected = 0;
                }else {
                    if(a.name.equals(email)){
                        profile.withSetSelected(true);
                        accountSelected = i;
                    }
                }
                accountHeader.addProfile(profile, accountHeader.getProfiles().size() - 2);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        appNo = 2;
        showIssues();
        syncIssues();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            syncIssues();
        }
        if (id == R.id.action_notification) {
            Intent i = new Intent(this, NotificationActivity2.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showIssues() {
        Log.i(TAG, "showIssues()");
        if(appNo == 2){
            TreeSet<Issue> issues = getIssue2();
            if(issues.size() > 0){
                rvAdapter2 = new AdapterHome(this, issues);
                if(rvAdded){
                    recyclerView.swapAdapter(rvAdapter2, false);
                }else {
                    container.addView(refreshLayout2);
                    refreshLayout2.addView(recyclerView);
                    recyclerView.setAdapter(rvAdapter2);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    rvAdded = true;
                }
            }else {
                if(rvAdded){
                    container.removeView(refreshLayout2);
                    refreshLayout2.removeView(recyclerView);
                }
                container.removeView(emptyMessage);
                container.addView(emptyMessage);
                rvAdded =false;
            }
        }
    }
    
    private void syncIssues(){
        Log.d(TAG, "synchIssues()");
        
        if(appNo == 2) {
            refreshLayout2.setRefreshing(true);
            String url = Constants.API_BASE_URL + "/issues?start=" + syncPref.getLong(Constants.LAST_ISSUE2_SYNC, 0);
            Log.i(TAG, "url = " + url);
            Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(TAG, "Issue Response :" + response.toString());
                    Long syncTime;

                    try {
                        syncTime = response.getLong("issueSync");
                        JSONArray issues = response.getJSONArray("issues");
                        if (issues.length() > 0) {
                            if(!rvAdded){
                                container.addView(refreshLayout2);
                                refreshLayout2.addView(recyclerView);
                                TreeSet<Issue> issue = new TreeSet<>();
                                rvAdapter2 = new AdapterHome(mContext, issue);
                                recyclerView.setAdapter(rvAdapter2);
                                recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                                rvAdded = true;
                            }
                            /*Save or Update Issues in database*/
                            List<Issue> issueList = new ArrayList<>();
                            for (int i = 0; i < issues.length(); i++) {
                                issueList.add(getIssue(issues.getJSONObject(i)));
                            }
                            issueService.saveOrUpdate(issueList);

                            for (Issue issue : issueList) {

                                if (true) {      //If Issue belongs to applied filter then add or update rvAdapter
                                    if (issue.getFixAt() != null && issue.getAckAt() != null) {
                                        Log.i(TAG, "Adapter : add Issue");
                                        rvAdapter2.insert(issue);
                                    } else {
                                        Log.i(TAG, "Adapter : update Issue");
                                        rvAdapter2.update(issue);
                                    }
                                }
                            }

                        }
                        syncPref.edit().putLong(Constants.LAST_ISSUE2_SYNC, syncTime).commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    refreshLayout2.setRefreshing(false);
                }
            };
            Response.ErrorListener errorListener = new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, error.toString());
                    NetworkResponse resp = error.networkResponse;
                    String data = new String(resp.data);
                    Log.i(TAG, "response status: " + data);
                    if (resp.statusCode == 401) {
                        invalidateAccessToken();
                    } else {
                        Toast.makeText(mContext, "Unable to Sync. Check your Internet Connection.", Toast.LENGTH_SHORT).show();
                    }
                    refreshLayout2.setRefreshing(false);
                }
            };

            String accessToken = userPref.getString(Constants.USER_ACCESS_TOKEN, null);
            if (accessToken == null) {
                if(accountSelected != -1) {
                    getAuthToken();
                }
                return;
            }

            MyJsonRequest request = new MyJsonRequest(Request.Method.GET, url, null, listener, errorListener, accessToken);
            request.setTag(TAG);
            AppController.getInstance().addToRequestQueue(request);
        }
    }

    private TreeSet<Issue> getIssue2(){
        Log.d(TAG,"getIssue2");
        TreeSet<Issue> issues = new TreeSet<>();
        List<Issue> list;
        if(selectedTeam.contains("All Team")){
            list = issueService.findAll();
        }else{
            list = issueService.findAllByTeam(selectedTeam);
        }
        for (Issue i : list) {
            issues.add(i);
        }
        return issues;
    }

    private Issue getIssue(JSONObject i) {
        Issue mIssue = null;
        try {
            mIssue = new Issue(i.getLong("id"),i.getLong("buyerId"), i.getString("problem"), i.getString("description"), new Date(i.getLong("raisedAt")), null, null, i.getInt("processingAt"));
            mIssue.setRaisedBy(i.getLong("raisedBy"));

            if (! i.getString("ackBy").equals("null")) {
                mIssue.setAckBy(i.getLong("ackBy"));
            }
            if (! i.getString("ackAt").equals("null")) {
                mIssue.setAckAt(new Date(i.getLong("ackAt")));
            }
            if (! i.getString("fixBy").equals("null")) {
                mIssue.setFixBy(i.getLong("fixBy"));
            }
            if (! i.getString("fixAt").equals("null")) {
                mIssue.setFixAt(new Date(i.getLong("fixAt")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mIssue;
    }

    private void prepareScreen(){
        Log.d(TAG,"prepareScreen");
        RecyclerView.LayoutParams param1 = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );

        teamFilter = new Spinner(this);
        teamFilter.setLayoutParams(param1);
        teamFilter.setId(R.id.home_team_filter);

        refreshLayout2 = new SwipeRefreshLayout(this);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params2.addRule(RelativeLayout.BELOW, R.id.home_team_filter);
        refreshLayout2.setLayoutParams(params2);

        recyclerView = new RecyclerView(this);
        recyclerView.setLayoutParams(param1);

        //create text view
        emptyMessage = new TextView(this);
        emptyMessage.setLayoutParams(params2);
        emptyMessage.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyMessage.setTextColor(ContextCompat.getColor(this, R.color.limeGreen));
        emptyMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyMessage.setText("No Open Issues Found.");

        //Set adapter for Team Filter
        final String[] teams = app.getTeams();
        teams[0] = "All Team";
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, teams);
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamFilter.setAdapter(teamAdapter);

        teamFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemSelect() : team");
                selectedTeam = teams[position];
                showIssues();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        refreshLayout2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncIssues();
            }
        });
    }

    private void buildAccountHeader(){
        Log.d(TAG,"buildAccountHeader");
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileSettingDrawerItem().withName("Add Account").withIcon(android.R.drawable.ic_input_add).withIdentifier(ACCOUNT_ADD),
                        new ProfileSettingDrawerItem().withName("Manage Account").withIcon(android.R.drawable.ic_menu_preferences).withIdentifier(ACCOUNT_MANAGE)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        if (profile instanceof IDrawerItem) {
                            if(((IDrawerItem) profile).getIdentifier() == ACCOUNT_ADD){
                                Log.d(TAG, "addAccount");
                                addAccount();
                            }else if(((IDrawerItem) profile).getIdentifier() == ACCOUNT_MANAGE){
                                Log.d(TAG, "Manage Account");
                                Intent i = new Intent(Settings.ACTION_SYNC_SETTINGS);
                                startActivity(i);
                            }else {
                                int profileId = (int) ((IDrawerItem) profile).getIdentifier();
                                if (profileId != accountSelected && profileId < 10) {
                                    String user = accountList.get(profileId);
                                    processUserType(user);
                                    accountSelected = profileId;
                                }
                            }
                        }
                        return false;
                    }
                })
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        Log.d(TAG, "onProfileImageClick");
                        if (profile instanceof IDrawerItem) {
                            int profileId = (int)((IDrawerItem) profile).getIdentifier();
                            if(profileId == accountSelected){
                                //start profile activity
                                Intent i = new Intent(mContext, ProfileActivity.class);
                                startActivity(i);
                            }
                            else if( profileId != accountSelected && profileId < 10){
                                //check usertype and thereby render appropriate home screen
                                String user = accountList.get(profileId);
                                processUserType(user);
                                accountSelected = profileId;
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();

    }

    private void buildDrawer(){
        Log.d(TAG,"buildDrawer");
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(true)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Report").withIdentifier(1)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            Log.d(TAG, ((Nameable) drawerItem).getName().getText(mContext));
                        }
                        return false;
                    }
                })
                .build();
    }

    /*If acoount is changed re-render entire view*/
    private void onAccountChange(){
        Log.d(TAG,"onAccountChange");
        refreshLayout2.removeView(recyclerView);
        container.removeAllViews();

        if(appNo == 2){
            container.addView(teamFilter);
            TreeSet<Issue> issues = getIssue2();
            if(issues.size() > 0){
                container.addView(refreshLayout2);
                refreshLayout2.addView(recyclerView);
                AdapterHome rvAdapter2 = new AdapterHome(this, issues);
                recyclerView.setAdapter(rvAdapter2);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                rvAdded = true;
            }else {
                if(rvAdded){
                    container.removeView(refreshLayout2);
                    refreshLayout2.removeView(recyclerView);
                }
                container.addView(emptyMessage);
                rvAdded =false;
            }
        }
    }

    private void getTokenForAccountCreateIfNeeded() {
        Log.d(TAG, "getTokenForAccountCreateIfNeeded");
        AccountManager mAccountManager = AccountManager.get(this);
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(AuthConstants.VALUE_ACCOUNT_TYPE, AuthConstants.AUTH_TOKEN_TYPE_FULL_ACCESS, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            Log.d(TAG, "bundle: " + bnd);
                            String username = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                            String authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                            SharedPreferences.Editor editor = userPref.edit();
                            editor.putString(Constants.USER_EMAIL,username);
                            editor.putString(Constants.USER_ACCESS_TOKEN,authToken);
                            editor.putBoolean(Constants.IS_USER_LOGGED_IN, true);
                            editor.commit();
                            updateAccountHeader(username);
                        } catch (Exception e) {
                            e.printStackTrace();
                            //showMessage(e.getMessage());
                        }
                    }
                }
                , null);
    }

    private void invalidateAccessToken(){
        Log.d(TAG,"invalidateAccessToken");
        String accessToken = userPref.getString(Constants.USER_ACCESS_TOKEN,null);
        mAccountManager.invalidateAuthToken(AuthConstants.VALUE_ACCOUNT_TYPE,accessToken);
    }

    private void getAuthToken(){
        Log.d(TAG,"getAuthToken");
        Account[] accounts = mAccountManager.getAccounts();
        String email = userPref.getString(Constants.USER_EMAIL, null);
        Account account = null;
        for (Account a: accounts){
            if(a.name.equals(email)){
                account = a;
                break;
            }
        }

        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, AuthConstants.AUTH_TOKEN_TYPE_FULL_ACCESS, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    String authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    userPref.edit().putString(Constants.USER_ACCESS_TOKEN,authToken).commit();
                    syncIssues();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG,e.getMessage());
                }
            }
        }).start();
    }

    private void updateAccountHeader(String name){
        Log.d(TAG,"updateAccountHeader: email = " + name);
        for (String account: accountList ){
            if(name.equalsIgnoreCase(account)){
                return;
            }
        }
        //First user being added
        if(accountList.size() == 0){
            accountSelected = 0;
            User user = userService.findByEmail(name);
            userPref.edit().putString(Constants.USER_EMAIL, user.getEmail()).commit();
            chooseScreen(user.getUserType());
            onAccountChange();
        }
        ProfileDrawerItem profile =  new ProfileDrawerItem().withEmail(name).withName(name).withIcon(getResources().getDrawable(R.drawable.profile1)).withIdentifier(accountList.size());
        accountHeader.addProfile(profile, accountHeader.getProfiles().size() - 2);
        accountList.add(name);
    }

    private void addAccount(){
        Log.i(TAG, "addAccount");
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(AuthConstants.VALUE_ACCOUNT_TYPE, AuthConstants.AUTH_TOKEN_TYPE_FULL_ACCESS, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    String username = bnd.getString(AccountManager.KEY_ACCOUNT_NAME);
                    updateAccountHeader(username);
                } catch (Exception e) {
                    e.printStackTrace();
                    //showMessage(e.getMessage());
                }
            }
        }, null);
    }

    /**
     * Call when Account is changed.
     * 1. Get user from database.
     * 2. Find UserType
     * 3. If UserType == Factory && appNo == 2, show factory screen
     *      else if (userType == Non-Factory && appNo == 1, show City screen
     * 4. update user details in userPref including new authToken
     * @param email
     */
    private void processUserType(String email){
        Log.d(TAG,"processUserType: email = " + email);
        invalidateAccessToken();
        User user = userService.findByEmail(email);
        userPref.edit().putString(Constants.USER_EMAIL,email).commit();
        chooseScreen(user.getUserType());
        onAccountChange();
    }

    private void chooseScreen(String userType){
        Log.d(TAG,"chooseScreen: userType = " + userType);
        if(userType.equalsIgnoreCase(USER_FACTORY)){
            appNo = 1;
        }else if(userType.equalsIgnoreCase(USER_SAMPLING)){
            appNo = 2;
            showFab(true);
        }else {
            appNo = 2;
            showFab(false);
        }

    }

    private void showFab(boolean value){
        Log.d(TAG, "showFab: value = " + value);
        if(value){
            fab.show();
        }else {
            fab.hide();
        }
    }

}
