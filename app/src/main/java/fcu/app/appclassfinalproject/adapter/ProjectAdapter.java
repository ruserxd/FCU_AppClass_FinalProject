package fcu.app.appclassfinalproject.adapter;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fcu.app.appclassfinalproject.ProjectActivity;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.model.Project;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projectList;
    private Context context;

    public ProjectAdapter(Context context, List<Project> list) {
        this.context = context;
        this.projectList = list;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    // 為每一個 Project 的 Recycle View 的子項目 綁定資料 (project_name, id ....)
    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        String tv_project = "ProjectName: " + project.getName();
        String tv_summary = "Summary: " + project.getSummary();
        holder.textViewName.setText(tv_project);
        holder.textViewSummary.setText(tv_summary);

        // 當 project 被點擊時
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProjectActivity.class);
                // SharedPreferences 存入 Project 的 ID
                SharedPreferences prefs = context.getSharedPreferences("FCUPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("project_id", project.getId());
                editor.apply();
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewSummary;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewSummary = itemView.findViewById(R.id.textViewSummary);
        }
    }
}