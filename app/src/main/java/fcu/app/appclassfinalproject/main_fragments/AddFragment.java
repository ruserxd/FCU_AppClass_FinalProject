package fcu.app.appclassfinalproject.main_fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass. Use the {@link AddFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddFragment extends Fragment {

  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";

  // TODO: Rename and change types of parameters
  private String mParam1;
  private String mParam2;

  public AddFragment() {
    // Required empty public constructor
  }

  private TextView etPName;
  private TextView etPSummary;
  private AutoCompleteTextView actvPM;
  private Button btnADD;
  private SqlDataBaseHelper sqlDataBaseHelper;
  private SQLiteDatabase db;

  private static String[] accountList = new String[]{};
  private ArrayAdapter<String> adapter;


  /**
   * Use this factory method to create a new instance of this fragment using the provided
   * parameters.
   *
   * @param param1 Parameter 1.
   * @param param2 Parameter 2.
   * @return A new instance of fragment AddFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static AddFragment newInstance(String param1, String param2) {
    AddFragment fragment = new AddFragment();
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
    View view = inflater.inflate(R.layout.fragment_add, container, false);

    // 初始化 UI 元件
    etPName = view.findViewById(R.id.et_ProjectName);
    etPSummary = view.findViewById(R.id.et_ProjectSummary);
    actvPM = view.findViewById(R.id.actv_PM);
    btnADD = view.findViewById(R.id.btn_addProject);

    sqlDataBaseHelper = new SqlDataBaseHelper(this.getContext());
    db = sqlDataBaseHelper.getWritableDatabase();
    accountList = getAccountList();
    adapter = new ArrayAdapter<String>(this.requireContext(),
        android.R.layout.simple_dropdown_item_1line, accountList);
    actvPM.setAdapter(adapter);

    btnADD.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String name = etPName.getText().toString();
        String summary = etPSummary.getText().toString();
        String pm = actvPM.getText().toString();

        // 先檢查 manager 是否有該使用者
        Cursor cursor = db.rawQuery("SELECT id FROM Users WHERE account = ?", new String[]{pm});
        if (cursor.moveToFirst()) {
          int managerId = cursor.getInt(0);

          ContentValues values = new ContentValues();
          values.put("name", name);
          values.put("summary", summary);
          values.put("manager_id", managerId);  // 修正欄位名稱

          long result = db.insert("Projects", null, values);
          if (result != -1) {
            Toast.makeText(getContext(), R.string.AddProject_success, Toast.LENGTH_SHORT).show();
            ContentValues userProjectValues = new ContentValues();
            userProjectValues.put("user_id", managerId);
            userProjectValues.put("project_id", (int) result);  // result 是 Projects 表的 id

            long linkResult = db.insert("UserProject", null, userProjectValues);
          } else {
            Toast.makeText(getContext(), R.string.AddProject_fail, Toast.LENGTH_SHORT).show();
          }
        } else {
          Toast.makeText(getContext(), R.string.AddProject_NoAccount, Toast.LENGTH_SHORT).show();
          Log.d("AddFragment", "NO account！");
        }

        cursor.close();
      }
    });
    // Inflate the layout for this fragment
    return view;
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
}