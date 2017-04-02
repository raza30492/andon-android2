package in.andonsystem.v2.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.andonsystem.App;
import in.andonsystem.R;
import in.andonsystem.v2.adapter.CustomBuyerAdapter;
import in.andonsystem.v2.entity.Buyer;
import in.andonsystem.v2.service.BuyerService;

public class RaiseIssueActivity2 extends AppCompatActivity {

    private final String TAG = RaiseIssueActivity2.class.getSimpleName();

    private Context mContect;
    private App app;

    private Spinner teamFilter;
    private Spinner buyerFilter;
    private Spinner problemFilter;

    private String selectedTeam = "Select Team";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raise_issue2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContect = this;
        app = (App) getApplication();

        teamFilter = (Spinner) findViewById(R.id.ri_team_filter);
        buyerFilter = (Spinner) findViewById(R.id.ri_buyer_filter);
        problemFilter = (Spinner) findViewById(R.id.ri_problem_filter);

        /*//////////////// Populating team filter //////////////////////*/
        final String[] teams = app.getTeams();
        ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, teams);
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamFilter.setAdapter(teamAdapter);
        teamFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemSelect() : team");
                selectedTeam = teams[position];
                updateBuyer();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        updateBuyer();

         /*//////////////// Populating problem filter //////////////////////*/
        final String[] problems = app.getProblems();
        ArrayAdapter<String> problemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, problems);
        problemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        problemFilter.setAdapter(problemAdapter);


    }

    private void updateBuyer(){
        Log.d(TAG, "updateBuyer()");

        BuyerService buyerService = new BuyerService(app);
        List<Buyer> buyers;
        if(! selectedTeam.contains("Select")){
            buyers = buyerService.findByTeam(selectedTeam);
        }else {
            buyers = new ArrayList<>();
            buyers.add(new Buyer(0L,"Select Buyer",""));
        }

        CustomBuyerAdapter buyerAdapter = new CustomBuyerAdapter(this,R.layout.spinner_list_item,buyers);
        buyerAdapter.setDropDownViewResource(R.layout.spinner_list_item);
        buyerFilter.setAdapter(buyerAdapter);

    }

    public void raiseIssue(View v){
        Log.d(TAG,"raiseIssue");

        String team = teamFilter.getSelectedItem().toString();
        String buyer = ((TextView)buyerFilter.findViewById(R.id.id)).getText().toString();
        String problem = problemFilter.getSelectedItem().toString();

        Log.i(TAG, "team = " + team);
        Log.i(TAG, "buyerId = " + buyer);
        Log.i(TAG, "problem = " + problem);
    }

}
