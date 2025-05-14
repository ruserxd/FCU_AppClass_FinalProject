package fcu.app.appclassfinalproject.main_fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.adapter.IssueAdapter;
import fcu.app.appclassfinalproject.adapter.ProjectAdapter;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.Issue;
import fcu.app.appclassfinalproject.model.Project;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProjectInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectInfoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private IssueAdapter issueAdapter;
    private List<Issue> issueList;

    public ProjectInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProjectINFOFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProjectInfoFragment newInstance(String param1, String param2) {
        ProjectInfoFragment fragment = new ProjectInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project_info, container, false);
        TextView tv_projectName = view.findViewById(R.id.tv_project_name);
        SharedPreferences prefs = view.getContext().getSharedPreferences("FCUPrefs", MODE_PRIVATE);

        recyclerView = view.findViewById(R.id.rcy_view_issues);
        issueList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 獲取 DataBase 相關訊息
        SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(requireContext());
        SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase();
        Cursor cursor = null;
        Cursor issueCursor = null;

        int project_id = prefs.getInt("project_id", 0);

        try {
            cursor = db.rawQuery("SELECT * FROM Projects WHERE id = ?", new String[]{String.valueOf(project_id)});

            // 檢查是否有結果
            if (cursor != null && cursor.moveToFirst()) {
                String projectName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                tv_projectName.setText(projectName);
            } else {
                tv_projectName.setText("找不到項目");
                Log.e("ProjectInfoFragment", "找不到 ID 為 " + project_id + " 的項目");
            }

            // 查找該專案下的所有問題
            issueCursor = db.rawQuery("SELECT * FROM Issues WHERE project_id = ?", new String[]{String.valueOf(project_id)});
            if (issueCursor != null && issueCursor.moveToFirst()) {
                do {
                    String name = issueCursor.getString(issueCursor.getColumnIndexOrThrow("name"));
                    String summary = issueCursor.getString(issueCursor.getColumnIndexOrThrow("summary"));
                    String start_time = issueCursor.getString(issueCursor.getColumnIndexOrThrow("start_time"));
                    String end_time = issueCursor.getString(issueCursor.getColumnIndexOrThrow("end_time"));
                    String status = issueCursor.getString(issueCursor.getColumnIndexOrThrow("status"));

                    issueList.add(new Issue(name, summary, start_time, end_time, status, String.valueOf(project_id)));
                } while (issueCursor.moveToNext());
            } else {
                Toast.makeText(requireContext(), "此專案沒有任何問題", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            tv_projectName.setText("加載時發生錯誤");
            Log.e("ProjectInfoFragment", "加載時發生錯誤: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "加載時發生錯誤: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (issueCursor != null) {
                issueCursor.close();
            }
        }

        // 設置 RecyclerView 的 Adapter
        issueAdapter = new IssueAdapter(getContext(), issueList);
        recyclerView.setAdapter(issueAdapter);

        return view;
    }

}