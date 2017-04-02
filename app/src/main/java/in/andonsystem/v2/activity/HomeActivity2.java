package in.andonsystem.v2.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

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
import in.andonsystem.v2.entity.Issue;
import in.andonsystem.v2.entity.User;
import in.andonsystem.v2.service.IssueService;
import in.andonsystem.v2.util.Constants;

public class HomeActivity2 extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = HomeActivity2.class.getSimpleName();

    private Context mContext;
    private App app;

    private RelativeLayout container;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private TextView textView;
    private Spinner teamFilter;

    private SharedPreferences syncPref;
    private IssueService issueService;

    private AdapterHome rvAdapter; //Recycler View Adapter
    private Boolean rvAdded;  //Whether recycler view is added to container
    private String selectedTeam = "Select Team";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        mContext = this;
        app = (App) getApplication();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, RaiseIssueActivity2.class);
                startActivity(i);
            }
        });
 //       fab.hide();
//        if(level == 0){
//            fab.show();
//        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //DrawerLayout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //NavigationView
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView username = (TextView) header.findViewById(R.id.nav_header_username);
        username.setText("Md Zahid Raza");
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ProfileActivity.class);
                startActivity(i);
            }
        });
//        if(level != 0){
//            Menu menu = navigationView.getMenu();
//            menu.removeItem(R.id.nav_stylechangeover);
//        }

        //create recycler view
        recyclerView = new RecyclerView(this);
        RecyclerView.LayoutParams params1 = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        recyclerView.setLayoutParams(params1);

        //Create swipe refresh layout
        refreshLayout = new SwipeRefreshLayout(this);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params2.addRule(RelativeLayout.BELOW, R.id.home_team_filter);
        refreshLayout.setLayoutParams(params2);

        //create text view
        textView = new TextView(this);
        //params1.topMargin = 50;
        textView.setLayoutParams(params2);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextColor(ContextCompat.getColor(this, R.color.limeGreen));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setText("No Open Issues Found.");

        //////////////////////////////////////////////////////////////////////////////////////
        teamFilter = (Spinner) findViewById(R.id.home_team_filter);
        final String[] teams = app.getTeams();
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

        //Swipe Refresh
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncIssues();
            }
        });

        container = (RelativeLayout) findViewById(R.id.content_home);
        syncPref = getSharedPreferences(Constants.SYNC_PREF, 0);
        issueService = new IssueService(app);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showIssues();
        syncIssues();
    }


    private void showIssues() {
        Log.i(TAG, "showIssues()");

        TreeSet<Issue> issues = new TreeSet<>();
        List<Issue> list;
        if(selectedTeam.contains("Select")){
            list = issueService.findAll();
        }else{
            list = issueService.findAllByTeam(selectedTeam);
        }
        for (Issue i : list) {
            issues.add(i);
        }

        if (issues.size() > 0) {
            //It is necesssay to remove existing view (if exist)
            container.removeView(textView);
            refreshLayout.removeView(recyclerView);
            container.removeView(refreshLayout);

            //Add recyclerView
            container.addView(refreshLayout);
            refreshLayout.addView(recyclerView);
            rvAdded = true;
            rvAdapter = new AdapterHome(this, issues);
            recyclerView.setAdapter(rvAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            //Remove both views first if exist
            container.removeView(textView);
            refreshLayout.removeView(recyclerView);
            container.removeView(refreshLayout);
            rvAdded = false;
            //Add textView
            container.addView(textView);
        }
    }

    private void syncIssues() {
        Log.i(TAG,"syncIssues()");
        refreshLayout.setRefreshing(true);
        String url = Constants.API_BASE_URL + "/issues?start=" + syncPref.getLong(Constants.LAST_ISSUE_SYNC, 0);
        Log.i(TAG, "url = " + url);
        Response.Listener<JSONObject> listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "Issue Response :" + response.toString());
                Long syncTime;

                try {
                    syncTime = response.getLong("issueSync");
                    JSONArray issues = response.getJSONArray("issues");
                    if (issues.length() > 0 & !rvAdded) {
//                        container.removeView(textView);
//                        refreshLayout.removeView(recyclerView);
//                        container.removeView(refreshLayout);
                        TreeSet<Issue> issue = new TreeSet<>();
                        rvAdapter = new AdapterHome(mContext, issue);
                        container.addView(refreshLayout);
                        refreshLayout.addView(recyclerView);
                        recyclerView.setAdapter(rvAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                        rvAdded = true;
                    }

                    /*Save or Update Issues in database*/
                    List<Issue> issueList = new ArrayList<>();
                    for (int i = 0; i < issues.length(); i++) {
                        issueList.add(getIssue(issues.getJSONObject(i)));
                    }
                    issueService.saveOrUpdate(issueList);
                    syncPref.edit().putLong(Constants.LAST_ISSUE_SYNC,syncTime).commit();
                    for(Issue issue: issueList) {

                        if (true) {      //If Issue belongs to applied filter then add or update rvAdapter
                            if (issue.getFixAt() != null && issue.getAckAt() != null) {
                                Log.i(TAG, "Adapter : add Issue");
                                rvAdapter.insert(issue);
                            } else {
                                Log.i(TAG, "Adapter : update Issue");
                                rvAdapter.update(issue);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                refreshLayout.setRefreshing(false);
            }
        };
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
                Toast.makeText(mContext,"Unable to Sync. Check your Internet Connection.",Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false);
            }
        };

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,url,null,listener,errorListener);
        request.setTag(TAG);
        AppController.getInstance().addToRequestQueue(request);
    }

    private Issue getIssue(JSONObject i) {
        Issue mIssue = null;
        try {
            mIssue = new Issue(i.getLong("id"),i.getLong("buyerId"), i.getString("problem"), i.getString("description"), new Date(i.getLong("raisedAt")), null, null);
            mIssue.setRaisedBy(new User(i.getLong("raisedBy")));

            if (! i.getString("ackBy").equals("null")) {
                mIssue.setAckBy(new User(i.getLong("ackBy")));
            }
            if (! i.getString("ackAt").equals("null")) {
                mIssue.setAckAt(new Date(i.getLong("ackAt")));
            }
            if (! i.getString("fixBy").equals("null")) {
                mIssue.setFixBy(new User(i.getLong("fixBy")));
            }
            if (! i.getString("fixAt").equals("null")) {
                mIssue.setFixAt(new Date(i.getLong("fixAt")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mIssue;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_contacts) {
            Intent i = new Intent(this, ContactActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_help) {
            Intent i = new Intent(this, HelpActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_logout) {
            //sharedPref.edit().putBoolean(Constants.LOGGED_IN,false).commit();
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
