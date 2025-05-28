package fcu.app.appclassfinalproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.model.IssueName;
import java.util.List;

public class IssueNameAdapter extends RecyclerView.Adapter<IssueNameAdapter.ViewHolder> {

  private List<IssueName> issueList;

  public IssueNameAdapter(Context context, List<IssueName> list) {
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
    IssueName issue = issueList.get(position);
    holder.tvIssueName.setText(issue.getName());
  }

  @Override
  public int getItemCount() {
    return issueList.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    TextView tvIssueName;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvIssueName = itemView.findViewById(R.id.tv_issue_name);
    }
  }
}
