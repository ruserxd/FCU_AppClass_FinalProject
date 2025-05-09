package fcu.app.appclassfinalproject.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.model.Project;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projectList;

    public ProjectAdapter(List<Project> list) {
        this.projectList = list;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projectList.get(position);
        holder.textViewName.setText("Project: " + project.getName());
        holder.textViewSummary.setText("Summary: " + project.getSummary());
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
