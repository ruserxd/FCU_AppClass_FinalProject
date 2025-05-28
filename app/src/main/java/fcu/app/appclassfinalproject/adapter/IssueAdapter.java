package fcu.app.appclassfinalproject.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.EditIssueActivity;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.Issue;
import java.util.List;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.ViewHolder> {

  private List<Issue> issueList;
  private Context context;
  private static final String TAG = "IssueAdapter";

  public IssueAdapter(Context context, List<Issue> list) {
    this.context = context;
    this.issueList = list;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_issue, parent, false);
    return new ViewHolder(view);
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    Issue issue = issueList.get(position);

    // 設定基本資訊
    holder.tvName.setText(issue.getName());
    holder.tvSummary.setText(issue.getSummary());
    holder.tvStatus.setText(issue.getStatus());
    String lang = getCurrentLanguage();
    if(lang.equals("en")) {
      if(issue.getStatus().equals("TO-DO")) holder.tvStatus.setText("未開始");
      if(issue.getStatus().equals("In progress")) holder.tvStatus.setText("進行中");
      if(issue.getStatus().equals("Finished")) holder.tvStatus.setText("已完成");
    }
    if(lang.equals("zh")) {
      if(issue.getStatus().equals("未開始")) holder.tvStatus.setText("TO-DO");
      if(issue.getStatus().equals("進行中")) holder.tvStatus.setText("In progress");
      if(issue.getStatus().equals("已完成")) holder.tvStatus.setText("Finished");
    }

    // 處理負責人顯示
    setDesigneeText(holder, issue);

    // 設定點擊事件
    setClickListener(holder, issue);
  }

  private void setDesigneeText(ViewHolder holder, Issue issue) {
    SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(context);

    try (SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase()) {
      String designeeValue = issue.getDesignee();

      Log.d(TAG, "Designee value: " + designeeValue);

      if (designeeValue == null || designeeValue.trim().isEmpty()) {
        String nodesignee = getCurrentLanguage().equals("zh")?" 未指派":" No Designee";
        holder.tvDesignee.setText(context.getString(R.string.IssueList_Designe)+nodesignee);
        return;
      }

      // 首先嘗試當作 ID 查詢
      String designeeName = findUserById(db, designeeValue);

      if (designeeName == null) {
        // 如果當作 ID 找不到，嘗試當作帳號名稱查詢
        designeeName = findUserByAccount(db, designeeValue);
      }

      if (designeeName != null) {
        holder.tvDesignee.setText(context.getString(R.string.IssueList_Designe)+" " + designeeName);
      } else {
        // 如果都找不到，直接顯示原始值
        holder.tvDesignee.setText(context.getString(R.string.IssueList_Designe)+" " + designeeValue);
        Log.w(TAG, "找不到負責人資訊: " + designeeValue);
      }

    } catch (Exception e) {
      Log.e(TAG, "查詢負責人時發生錯誤: " + e.getMessage(), e);
      String nodesignee = getCurrentLanguage().equals("zh")?" 查詢失敗":" Failed";
      holder.tvDesignee.setText(context.getString(R.string.IssueList_Designe)+nodesignee);
    }
  }

  /**
   * 根據 ID 查詢使用者
   */
  private String findUserById(SQLiteDatabase db, String designeeValue) {
    Cursor cursor = null;
    try {
      // 檢查是否為數字
      Integer.parseInt(designeeValue);

      cursor = db.rawQuery("SELECT account, name FROM Users WHERE id = ?",
          new String[]{designeeValue});

      if (cursor.moveToFirst()) {
        String name = getColumnValueSafe(cursor, "name");
        String account = getColumnValueSafe(cursor, "account");

        // 優先顯示 name，如果沒有則顯示 account
        return (name != null && !name.trim().isEmpty()) ? name : account;
      }
    } catch (NumberFormatException e) {
      Log.d(TAG, "Designee value is not a number: " + designeeValue);
    } catch (Exception e) {
      Log.e(TAG, "根據 ID 查詢使用者時發生錯誤: " + e.getMessage());
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  /**
   * 根據帳號查詢使用者
   */
  private String findUserByAccount(SQLiteDatabase db, String account) {
    try (Cursor cursor = db.rawQuery("SELECT account, name FROM Users WHERE account = ?",
        new String[]{account})) {

      if (cursor.moveToFirst()) {
        String name = getColumnValueSafe(cursor, "name");
        String accountName = getColumnValueSafe(cursor, "account");

        return (name != null && !name.trim().isEmpty()) ? name : accountName;
      }
    } catch (Exception e) {
      Log.e(TAG, "根據帳號查詢使用者時發生錯誤: " + e.getMessage());
    }
    return null;
  }

  private void setClickListener(ViewHolder holder, Issue issue) {
    holder.itemView.setOnClickListener(v -> {
      Integer issueId = findIssueId(issue.getName());
      if (issueId != null) {
        Intent intent = new Intent(context, EditIssueActivity.class);
        SharedPreferences prefs = context.getSharedPreferences("FCUPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("issue_Id", issueId);
        editor.apply();
        context.startActivity(intent);
      } else {
        Toast.makeText(context, "找不到該 Issue", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private Integer findIssueId(String issueName) {
    SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(context);

    try (SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase(); Cursor cursor = db.rawQuery(
        "SELECT id FROM Issues WHERE name = ?", new String[]{issueName})) {

      if (cursor.moveToFirst()) {
        return cursor.getInt(cursor.getColumnIndexOrThrow("id"));
      }
    } catch (Exception e) {
      Log.e(TAG, "查詢 Issue ID 時發生錯誤: " + e.getMessage());
    }
    return null;
  }

  /**
   * 安全地從 Cursor 取得欄位值
   */
  private String getColumnValueSafe(Cursor cursor, String columnName) {
    try {
      int columnIndex = cursor.getColumnIndex(columnName);
      if (columnIndex != -1) {
        return cursor.getString(columnIndex);
      } else {
        Log.w(TAG, "欄位不存在: " + columnName);
        return null;
      }
    } catch (Exception e) {
      Log.e(TAG, "取得欄位值時發生錯誤 " + columnName + ": " + e.getMessage());
      return null;
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

  private String getCurrentLanguage() {
    return getSharedPrefs().getString("app_language", "zh");
  }
  private SharedPreferences getSharedPrefs() {
    return context.getSharedPreferences("FCUPrefs", MODE_PRIVATE);
  }

}