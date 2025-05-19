package fcu.app.appclassfinalproject.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.Issue;
import java.util.List;

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
    SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(holder.itemView.getContext());
    SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase();
    Cursor cursor = null;
    cursor = db.rawQuery("SELECT * FROM Users WHERE id = ?",
        new String[]{String.valueOf(issue.getDesignee())});
    // 檢查是否有結果
    if (cursor != null && cursor.moveToFirst()) {
      String desingeeName = cursor.getString(cursor.getColumnIndexOrThrow("account"));
      holder.tvDesignee.setText("負責人: " + desingeeName);
    } else {
      holder.tvDesignee.setText("負責人: noOne");
      Log.e("ProjectInfoFragment", "找不到 ID 為 " + issue.getDesignee() + " 的項目");
    }
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
