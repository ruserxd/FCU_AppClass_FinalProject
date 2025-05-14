package fcu.app.appclassfinalproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.model.Issue;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.ViewHolder> {

    private List<Issue> issueList;

    public IssueAdapter(Context context, List<Issue> list) {
        this.issueList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_issue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Issue issue = issueList.get(position);
        holder.tvName.setText(issue.getName());
        holder.tvSummary.setText(issue.getSummary());
        holder.tvStatus.setText(issue.getStatus());
        holder.tvDesignee.setText(issue.getDesignee());
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSummary, tvStatus, tvDesignee;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_issuename);
            tvSummary = itemView.findViewById(R.id.tv_issuesummary);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDesignee = itemView.findViewById(R.id.tv_designee);
        }
    }
}
