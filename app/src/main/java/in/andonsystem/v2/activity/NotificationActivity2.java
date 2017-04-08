package in.andonsystem.v2.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import in.andonsystem.App;
import in.andonsystem.R;
import in.andonsystem.v2.adapter.AdapterNotification;
import in.andonsystem.v2.dto.Notification;
import in.andonsystem.v2.entity.Buyer;
import in.andonsystem.v2.entity.Issue;
import in.andonsystem.v2.entity.User;
import in.andonsystem.v2.service.IssueService;
import in.andonsystem.v2.service.UserService;
import in.andonsystem.v2.util.Constants;

public class NotificationActivity2 extends AppCompatActivity {

    private final String TAG = NotificationActivity2.class.getSimpleName();

    private Context mContext;
    private App app;
    private UserService userService;
    private IssueService issueService;
    private SharedPreferences userPref;

    private RelativeLayout container;
    private ProgressBar progress;
    private RecyclerView recyclerView;
    private TextView emptyMessage;

    private User user;
    private Long currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
        app = (App) getApplication();
        userService = new UserService(app);
        issueService = new IssueService(app);
        userPref = getSharedPreferences(Constants.USER_PREF,0);
        user = userService.findByEmail(userPref.getString(Constants.USER_EMAIL, null));

        container = (RelativeLayout) findViewById(R.id.content_nfn2);
        progress = (ProgressBar) findViewById(R.id.nfn2_loading);
        prepareScreen();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentTime == null){
            return;
        }

        long timeNow = System.currentTimeMillis();
        TreeSet<Notification> list = new TreeSet<>();
        String message;
        long timeAt;

        if (user.getUserType().equalsIgnoreCase(Constants.USER_MERCHANDISING)) {
            List<Issue> issues = issueService.findAllByBuyers(user.getBuyers());

            if (issues.size() > 0){
                for ( Issue issue: issues){
                    Log.d(TAG, "MERCHANDISING: issue = " + issue.getProblem());
                    if(issue.getFixAt() != null){
                        message = "Problem " + issue.getProblem() + " of " + issue.getBuyer().getTeam() + ":" + issue.getBuyer().getName() + " was resolved.";
                        timeAt = timeNow - issue.getFixAt().getTime();
                        list.add(new Notification(issue.getId(),message,timeAt, 2));
                    }
                    else if(issue.getAckAt() != null){
                        message = "Problem " +  issue.getProblem() + " of " + issue.getBuyer().getTeam() + ":" + issue.getBuyer().getName() + " was acknowledged by "
                                + (issue.getAckBy() == user.getId() ? "you" : issue.getAckByUser().getName());
                        timeAt = timeNow - issue.getAckAt().getTime();
                        list.add(new Notification(issue.getId(),message,timeAt, 1));
                    }
                    else {
                        message = "Problem " +  issue.getProblem() + " of " + issue.getBuyer().getTeam() + ":" + issue.getBuyer().getName() + " was raised by " + issue.getRaisedByUser().getName();
                        timeAt = timeNow - issue.getRaisedAt().getTime();
                        list.add(new Notification(issue.getId(),message,timeAt, 0));
                    }
                }
            }
        }
        else if (user.getUserType().equalsIgnoreCase(Constants.USER_SAMPLING)) {
            List<Issue> issues = issueService.findAllByUser(user);

            if (issues.size() > 0) {
                for (Issue issue: issues) {
                    Log.d(TAG, "SAMPLING: issue = " + issue.getProblem());
                    if (issue.getFixAt() == null) {
                        if (issue.getAckAt() != null) {
                            message = issue.getAckByUser().getName() + " acknowledged " + "problem " +  issue.getProblem() + " of " + issue.getBuyer().getTeam() + ":" + issue.getBuyer().getName();
                            timeAt = timeNow - issue.getAckAt().getTime();
                            list.add(new Notification(issue.getId(),message,timeAt, 1));
                        }else {
                            message = "Problem " +  issue.getProblem() + " of " + issue.getBuyer().getTeam() + ":" + issue.getBuyer().getName() + " was raised by you. ";
                            timeAt = timeNow - issue.getRaisedAt().getTime();
                            list.add(new Notification(issue.getId(),message,timeAt, 0));
                        }
                    }
                }
            }

        }

        ///////////////////////////
        if (list.size() > 0){
            Log.d(TAG, "No of Notifications: = " + list.size());
            container.addView(recyclerView);
            AdapterNotification adapter = new AdapterNotification(mContext,list,2);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }else {
            container.addView(emptyMessage);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        container.removeAllViews();
    }

    private void prepareScreen(){
        recyclerView = new RecyclerView(this);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        recyclerView.setLayoutParams(params);

        emptyMessage = new TextView(this);

        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params2.topMargin = 50;
        emptyMessage.setLayoutParams(params2);
        emptyMessage.setGravity(Gravity.CENTER_HORIZONTAL);
        emptyMessage.setTextColor(Color.parseColor("#00FF00"));
        emptyMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        emptyMessage.setText("No Notification available.");
    }

    private void getCurrentTime(){
        
    }
}
