package fcu.app.appclassfinalproject.main_fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddIssueFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddIssueFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText etPurpose;
    private EditText etOverview;
    private EditText etStartTime;
    private EditText etEndTime;
    private Spinner spiStatus;
    private AutoCompleteTextView actvDesignee;

    private ImageButton btnSave;

    String[] items = {"未開始", "進行中", "已完成"};
    private static String[] accountList = new String[]{};
    private SqlDataBaseHelper sqlDataBaseHelper;
    private SQLiteDatabase db;


    public AddIssueFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddIssueFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddIssueFragment newInstance(String param1, String param2) {
        AddIssueFragment fragment = new AddIssueFragment();
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

    private void showDatePickerDialog(final EditText editText) {
        // 獲取當前日期
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // 創建DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // 格式化日期為 yyyy-MM-dd
                        String selectedDate = String.format("%04d-%02d-%02d", year, monthOfYear + 1, dayOfMonth);
                        editText.setText(selectedDate);
                    }
                },
                year, month, day);

        datePickerDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_issue, container, false);

        sqlDataBaseHelper = new SqlDataBaseHelper(this.getContext());
        db = sqlDataBaseHelper.getWritableDatabase();

        //找對應id
        etPurpose = view.findViewById(R.id.et_purpose);
        etOverview = view.findViewById(R.id.et_overview);
        etStartTime = view.findViewById(R.id.et_start_time);
        etEndTime = view.findViewById(R.id.et_endtime);
        actvDesignee = view.findViewById(R.id.actv_designee);
        spiStatus = view.findViewById(R.id.spin_status);
        btnSave = view.findViewById(R.id.btn_save);

        etStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(etStartTime);
            }
        });

        etEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(etEndTime);
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), // 或 requireContext()
                android.R.layout.simple_spinner_item,
                items // String[] 陣列或 List<String>
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiStatus.setAdapter(adapter);

        accountList = getAccountList();
        ArrayAdapter<String> useradapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, accountList);
        actvDesignee.setAdapter(useradapter);

        // 假設 sharedPreference 有　"projectId"
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = etPurpose.getText().toString().trim();
                String summary = etOverview.getText().toString().trim();
                String start_time = etStartTime.getText().toString().trim();
                String end_time = etEndTime.getText().toString().trim();
                String status = spiStatus.getSelectedItem().toString().trim();
                String designee = actvDesignee.getText().toString().trim();
                SharedPreferences prefs = view.getContext().getSharedPreferences("FCUPrefs", MODE_PRIVATE);
                int project_id = prefs.getInt("project_id", 0);
                Fragment projectInfoFragment = ProjectInfoFragment.newInstance("", "");


                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("summary", summary);
                values.put("start_time", start_time);
                values.put("end_time", end_time);
                values.put("status", status);
                values.put("designee", designee);
                values.put("project_id", project_id);
                long rowId = db.insert("Issues", null, values);
                if (rowId != -1) {
                    Toast.makeText(getContext(), "資料插入成功", Toast.LENGTH_SHORT).show();
                    setCurrentFragment(projectInfoFragment);
                } else {
                    Toast.makeText(getContext(), "資料插入失敗", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    private void clearFields() {
        etPurpose.setText("");
        etOverview.setText("");
        etStartTime.setText("");
        etEndTime.setText("");
    }

    public String[] getAccountList() {
        List<String> accountList = new ArrayList<>();
        final String SQL = "SELECT account FROM Users";

        SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL, null);

        while (cursor.moveToNext()) {
            String account = cursor.getString(0); // 只有一欄 account，index 是 0
            accountList.add(account);
        }

        cursor.close();

        return accountList.toArray(new String[0]);
    }

    private void setCurrentFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_main_project, fragment)
                .commit();
    }


}
