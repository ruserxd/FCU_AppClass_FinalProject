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

import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;

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

        // 獲取 DataBase 相關訊息
        SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(requireContext());
        SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase();
        Cursor cursor;

        int project_id = prefs.getInt("project_id", 0);
        Log.i("project_id", String.valueOf(project_id));
        try {
            cursor = db.rawQuery("SELECT * FROM Projects WHERE id = ?", new String[]{String.valueOf(project_id)});

            // 檢查是否有結果
            if (cursor.moveToFirst()) {
                String projectName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                tv_projectName.setText(projectName);
            } else {
                tv_projectName.setText("找不到項目");
                Log.e("ProjectInfoFragment", "找不到 ID 為 " + project_id + " 的項目");
            }
        } catch (Exception e) {
            tv_projectName.setText("加載時發生錯誤");
            Log.e("ProjectInfoFragment", "加載時發生錯誤: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "加載時發生錯誤: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return view;
    }
}