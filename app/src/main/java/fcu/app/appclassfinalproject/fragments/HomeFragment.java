package fcu.app.appclassfinalproject.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.adapter.ProjectAdapter;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.Project;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView recyclerView;
    private TextView tvName;
    private ProjectAdapter adapter;
    private List<Project> projectList;
    private SqlDataBaseHelper dbHelper;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycle);
        tvName = view.findViewById(R.id.tv_name);
        projectList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SharedPreferences prefs = requireContext().getSharedPreferences("FCUPrefs", MODE_PRIVATE);
        String account = prefs.getString("account", "使用者");
        tvName.setText(account);
        dbHelper = new SqlDataBaseHelper(
                requireContext(),
                "FCU_FinalProjectDataBase",
                null,
                2,
                "Projects"
        );
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        projectList = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM Projects", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String summary = cursor.getString(cursor.getColumnIndexOrThrow("summary"));
                int managerId = cursor.getInt(cursor.getColumnIndexOrThrow("manager_id"));

                projectList.add(new Project(id, name, summary, managerId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        adapter = new ProjectAdapter(projectList);
        recyclerView.setAdapter(adapter);
    }

}