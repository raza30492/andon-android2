package in.andonsystem.v2.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import in.andonsystem.App;
import in.andonsystem.AppClose;
import in.andonsystem.R;
import in.andonsystem.v2.entity.Issue;
import in.andonsystem.v2.service.IssueService;

public class IssueDetailActivity2 extends AppCompatActivity {

    private Context mContext;
    private App app;

    private TextView problem;
    private TextView team;
    private TextView buyer;
    private TextView raisedAt;
    private TextView ackAt;
    private TextView fixAt;
    private TextView raisedBy;
    private TextView ackBy;
    private TextView fixBy;
    private TextView desc;
    private TextView processingAt;
    private Button ackButton;
    private Button fixButton;
    private LinearLayout layout;


    private IssueService issueService;
    private Long issueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
        app = (App)getApplication();
        issueService = new IssueService(app);

        Intent i = getIntent();
        issueId = i.getLongExtra("issueId",0L);

        problem = (TextView)findViewById(R.id.detail_problem);
        team = (TextView)findViewById(R.id.detail_team);
        buyer = (TextView)findViewById(R.id.detail_buyer);
        raisedAt = (TextView)findViewById(R.id.detail_raised_at);
        ackAt = (TextView)findViewById(R.id.detail_ack_at);
        fixAt = (TextView)findViewById(R.id.detail_fix_at);
        raisedBy = (TextView)findViewById(R.id.detail_raised_by);
        ackBy = (TextView)findViewById(R.id.detail_ack_by);
        fixBy = (TextView)findViewById(R.id.detail_fix_by);
        processingAt = (TextView)findViewById(R.id.detail_processing_at);
        desc = (TextView)findViewById(R.id.detail_desc);
        layout = (LinearLayout)findViewById(R.id.issue_detail_layout);

    }

    @Override
    protected void onStart() {
        super.onStart();
        DateFormat df = new SimpleDateFormat("hh:mm aa");
        df.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));

        Issue issue = issueService.findOne(issueId);

        problem.setText(issue.getProblem());
        team.setText(issue.getBuyer().getTeam());
        buyer.setText(issue.getBuyer().getName());
        raisedAt.setText(df.format(issue.getRaisedAt()));
        raisedBy.setText(issue.getRaisedByUser().getName());
        ackAt.setText(( (issue.getAckAt() != null) ? df.format(issue.getAckAt()) : "-" ));
        ackBy.setText(( (issue.getAckByUser() != null) ? issue.getAckByUser().getName() : "-" ));
        fixAt.setText(( (issue.getFixAt() != null) ? df.format(issue.getFixAt()) : "-" ));
        fixBy.setText(( (issue.getFixByUser() != null) ? issue.getFixByUser().getName() : "-" ));
        processingAt.setText("Processing At Level " + issue.getProcessingAt());
        desc.setText(issue.getDescription());
    }
}
