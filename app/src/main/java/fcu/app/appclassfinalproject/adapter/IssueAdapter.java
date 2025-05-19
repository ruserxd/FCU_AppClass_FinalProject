package fcu.app.appclassfinalproject.adapter;

import static android.content.Context.MODE_PRIVATE;

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

import java.util.List;
import java.util.Properties;

import fcu.app.appclassfinalproject.EditIssueActivity;
import fcu.app.appclassfinalproject.ProjectActivity;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.Issue;

public class IssueAdapter extends RecyclerView.Adapter<IssueAdapter.ViewHolder> {

    private List<Issue> issueList;
    private Context context;

    public IssueAdapter(Context context, List<Issue> list) {
        this.context=context;
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
        Integer id=-1;
        Issue issue = issueList.get(position);
        holder.tvName.setText(issue.getName());
        holder.tvSummary.setText(issue.getSummary());
        holder.tvStatus.setText(issue.getStatus());
        SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(holder.itemView.getContext());
        SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase();
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT * FROM Users WHERE id = ?", new String[]{String.valueOf(issue.getDesignee())});
        // 檢查是否有結果
        if (cursor != null && cursor.moveToFirst()) {
            String desingeeName = cursor.getString(cursor.getColumnIndexOrThrow("account"));
            holder.tvDesignee.setText("負責人: "+desingeeName);
        } else {
            holder.tvDesignee.setText("負責人: noOne");
            Log.e("ProjectInfoFragment", "找不到 ID 為 " + issue.getDesignee() + " 的項目");
        }
        cursor = db.rawQuery("SELECT id FROM Issues WHERE name = ?", new String[]{issue.getName()});
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        } else {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }

        Integer finalId = id;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditIssueActivity.class);
                // SharedPreferences 存入 Project 的 ID
                SharedPreferences prefs = context.getSharedPreferences("FCUPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("issue_Id", finalId);
                editor.apply();
                context.startActivity(intent);
            }
        });
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
