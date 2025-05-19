package fcu.app.appclassfinalproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.model.issueMonth;
import java.util.Arrays;
import java.util.List;

public class IssueMonthAdapter extends RecyclerView.Adapter<IssueMonthAdapter.ViewHolder> {

  private List<issueMonth> issueList;

  public IssueMonthAdapter(Context context, List<issueMonth> list) {
    this.issueList = list;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_gantt_month, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    issueMonth issue = issueList.get(position);
    int start = Integer.parseInt(issue.getStart_time().substring(5, 7)); // e.g., "2024-05-01"
    int end = Integer.parseInt(issue.getEnd_time().substring(5, 7));
    for (int i = start - 1; i < end; i++) {
      View monthView = holder.vMonth.get(i);
      if (monthView != null) {
        monthView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(),
            R.drawable.border)); // 你可以改成你要的顏色
      }
    }
  }

  @Override
  public int getItemCount() {
    return issueList.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    View vMonth1, vMonth2, vMonth3, vMonth4, vMonth5, vMonth6, vMonth7, vMonth8, vMonth9, vMonth10, vMonth11, vMonth12;
    List<View> vMonth;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      vMonth1 = itemView.findViewById(R.id.v_month1);
      vMonth2 = itemView.findViewById(R.id.v_month2);
      vMonth3 = itemView.findViewById(R.id.v_month3);
      vMonth4 = itemView.findViewById(R.id.v_month4);
      vMonth5 = itemView.findViewById(R.id.v_month5);
      vMonth6 = itemView.findViewById(R.id.v_month6);
      vMonth7 = itemView.findViewById(R.id.v_month7);
      vMonth8 = itemView.findViewById(R.id.v_month8);
      vMonth9 = itemView.findViewById(R.id.v_month9);
      vMonth10 = itemView.findViewById(R.id.v_month10);
      vMonth11 = itemView.findViewById(R.id.v_month11);
      vMonth12 = itemView.findViewById(R.id.v_month12);
      vMonth = Arrays.asList(vMonth1, vMonth2, vMonth3, vMonth4, vMonth5, vMonth6,
          vMonth7, vMonth8, vMonth9, vMonth10, vMonth11, vMonth12);
    }

  }
}
