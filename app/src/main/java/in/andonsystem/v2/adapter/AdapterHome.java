package in.andonsystem.v2.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.TreeSet;

import in.andonsystem.R;
import in.andonsystem.v2.activity.IssueDetailActivity2;
import in.andonsystem.v2.entity.Issue;

public class AdapterHome extends RecyclerView.Adapter<HolderHome> {

    private Context context;
    private TreeSet<Issue> set;

    public AdapterHome(Context context, TreeSet<Issue> set){
        this.context = context;
        this.set = set;
    }

    @Override
    public HolderHome onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home,parent,false);
        LinearLayout container = (LinearLayout) view.findViewById(R.id.issue_container);

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = ((TextView)v.findViewById(R.id.issue_id)).getText().toString();
                Intent i = new Intent(context, IssueDetailActivity2.class);
                i.putExtra("issueId", Long.parseLong(idStr));
                context.startActivity(i);
            }
        });
        if(viewType == 0){
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.tomato));
        }else if(viewType == 1){
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
        }else if(viewType == 2){
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.limeGreen));
        }

        return new HolderHome(view);
    }

    @Override
    public void onBindViewHolder(HolderHome holder, int position) {

        Object[] array = set.toArray();
        Issue issue = (Issue)array[position];
        String problem = issue.getProblem();

        holder.icon.setLetter(issue.getProblem().charAt(0));
        holder.icon.setOval(true);
        holder.problem.setText(problem);
        DateFormat df = new SimpleDateFormat("hh:mm aa");
        df.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        holder.time.setText(df.format(issue.getRaisedAt()));
        holder.team.setText(issue.getBuyer().getTeam());
        holder.buyer.setText(issue.getBuyer().getName());
        holder.issueId.setText(String.valueOf(issue.getId()));
    }

    @Override
    public int getItemCount() {
        return set.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object[] array = set.toArray();
        Issue issue = (Issue)array[position];
        if(issue.getFixAt() != null) return 2;
        else if(issue.getAckAt() != null) return 1;
        else return 0;
    }

    public void insert(Issue issue){
        set.add(issue);
        notifyDataSetChanged();
    }

    /**
     * Since Comparison is based on id, fixAt, ackAt and raisedAt fields, and the received object has changed state,
     * So, it is required to try deleting by moving it into previous states
     * @param issue
     */
    public void update(Issue issue){
        Issue temp = new Issue();
        temp.setId(issue.getId());
        set.remove(temp);
        temp.setFixAt(null);
        set.remove(temp);
        temp.setAckAt(null);
        set.remove(temp);

        set.add(issue);
        notifyDataSetChanged();
    }
}
