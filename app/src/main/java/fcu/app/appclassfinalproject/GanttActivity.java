package fcu.app.appclassfinalproject;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fcu.app.appclassfinalproject.adapter.IssueAdapter;
import fcu.app.appclassfinalproject.adapter.IssueMonthAdapter;
import fcu.app.appclassfinalproject.adapter.IssueNameAdapter;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.Issue;
import fcu.app.appclassfinalproject.model.issueMonth;
import fcu.app.appclassfinalproject.model.issueName;

public class GanttActivity extends AppCompatActivity {

    private RecyclerView issueRecyclerView;
    private RecyclerView monthRecyclerView;
    private IssueNameAdapter issueNameAdapter;
    private IssueMonthAdapter issueMonthAdapter;
    private List<issueName> issueNameList;
    private List<issueMonth> issueMonthList;
    private ImageButton btnBackToIssueList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gantt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        issueRecyclerView = findViewById(R.id.rcv_ganttIssue);
        monthRecyclerView = findViewById(R.id.rcv_ganttMonth);
        btnBackToIssueList = findViewById(R.id.btn_backToIssueList);
        issueNameList = new ArrayList<>();
        issueMonthList = new ArrayList<>();
        issueRecyclerView.setLayoutManager(new LinearLayoutManager(GanttActivity.this));
        monthRecyclerView.setLayoutManager(new LinearLayoutManager(GanttActivity.this));
        SharedPreferences prefs = GanttActivity.this.getSharedPreferences("FCUPrefs", MODE_PRIVATE);
        int project_id = prefs.getInt("project_id", 0);
        SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(GanttActivity.this);
        SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase();
        Cursor issueCursor = null;

        issueCursor = db.rawQuery("SELECT * FROM Issues WHERE project_id = ?", new String[]{String.valueOf(project_id)});
        if (issueCursor != null && issueCursor.moveToFirst()) {
            do {
                String name = issueCursor.getString(issueCursor.getColumnIndexOrThrow("name"));
                String summary = issueCursor.getString(issueCursor.getColumnIndexOrThrow("summary"));
                String start_time = issueCursor.getString(issueCursor.getColumnIndexOrThrow("start_time"));
                String end_time = issueCursor.getString(issueCursor.getColumnIndexOrThrow("end_time"));
                String status = issueCursor.getString(issueCursor.getColumnIndexOrThrow("status"));

                issueNameList.add(new issueName(name));
                issueMonthList.add(new issueMonth(start_time, end_time, status));
            } while (issueCursor.moveToNext());
        } else {
            Toast.makeText(GanttActivity.this, "此專案沒有任何問題", Toast.LENGTH_SHORT).show();
        }
        issueNameAdapter = new IssueNameAdapter(GanttActivity.this, issueNameList);
        issueRecyclerView.setAdapter(issueNameAdapter);
        issueMonthAdapter = new IssueMonthAdapter(GanttActivity.this, issueMonthList);
        monthRecyclerView.setAdapter(issueMonthAdapter);

        btnBackToIssueList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentTo(ProjectActivity.class);
            }
        });

    }
    private void intentTo(Class<?> page) {
        Intent intent = new Intent();
        intent.setClass(GanttActivity.this, page);
        startActivity(intent);
    }
}