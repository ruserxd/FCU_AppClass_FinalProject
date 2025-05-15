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
import fcu.app.appclassfinalproject.model.issueName;

public class IssueNameAdapter extends RecyclerView.Adapter<IssueNameAdapter.ViewHolder> {

    private List<issueName> issueList;

    public IssueNameAdapter(Context context, List<issueName> list) {
        this.issueList = list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gantt_issue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        issueName issue = issueList.get(position);
        holder.tvissueName.setText(issue.getName());
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvissueName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvissueName = itemView.findViewById(R.id.tv_issue_name);
        }
    }
}
