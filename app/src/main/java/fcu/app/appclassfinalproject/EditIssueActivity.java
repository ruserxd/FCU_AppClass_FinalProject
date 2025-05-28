package fcu.app.appclassfinalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.main_fragments.ProjectInfoFragment;

public class EditIssueActivity extends AppCompatActivity {

  private EditText edName;
  private EditText edSummary;
  private EditText edStartTime;
  private EditText edEndTime;
  private Spinner spin_Status;
  private EditText edDesignee;
  private Button btnSave;
  private Button btnCancel;
  int id;
  String[] items = {"未開始", "進行中", "已完成"};
  String[] itemsEN = {"TO-DO", "In progress", "Finished"};



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_edit_issue);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    edName = findViewById(R.id.ed_name);
    edSummary = findViewById(R.id.ed_summary);
    edStartTime = findViewById(R.id.ed_start_time);
    edEndTime = findViewById(R.id.ed_end_time);
    spin_Status = findViewById(R.id.spin_EditStatus);

    ArrayAdapter<String> adapter = new ArrayAdapter<>(
        EditIssueActivity.this, // 或 requireContext()
        android.R.layout.simple_spinner_item,
            getCurrentLanguage().equals("en")?items:itemsEN // String[] 陣列或 List<String>
    );
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spin_Status.setAdapter(adapter);

    edDesignee = findViewById(R.id.ed_designee);

    btnSave = findViewById(R.id.ed_btn_save);
    btnCancel = findViewById(R.id.ed_btn_cancel);

    SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(this);
    SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase();

//  TODO:須補issue id相關程式碼
//      假設有issue ID
    SharedPreferences prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    id = prefs.getInt("issue_Id", 0);

    Cursor cursor = null;
    cursor = db.rawQuery("SELECT * FROM Issues WHERE id = ?", new String[]{String.valueOf(id)});

    if (cursor != null && cursor.moveToFirst()) {
      do {
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String summary = cursor.getString(cursor.getColumnIndexOrThrow("summary"));
        String start_time = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
        String end_time = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));
        String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
        String designee = cursor.getString(cursor.getColumnIndexOrThrow("designee"));

        edName.setText(name);
        edSummary.setText(summary);
        edStartTime.setText(start_time);
        edEndTime.setText(end_time);
        int statusId = 2;
        if (status.equals("未開始")) {
          statusId = 0;
        }
        if (status.equals("進行中")) {
          statusId = 1;
        }

        spin_Status.setSelection(statusId);
        edDesignee.setText(designee);

      } while (cursor.moveToNext());
    } else {
      Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
    }
//      TODO:補preferences
//        SharedPreferences prefs = getSharedPreferences("", MODE_PRIVATE);
//        String projectId = prefs.getString("projectId",null);

    btnSave.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        String name = edName.getText().toString().trim();
        String summary = edSummary.getText().toString().trim();
        String start_time = edStartTime.getText().toString().trim();
        String end_time = edEndTime.getText().toString().trim();
        String status = spin_Status.getSelectedItem().toString().trim();
        String designee = edDesignee.getText().toString().trim();
        SharedPreferences prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
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
        long rowId = db.update("Issues", values, "id=?", new String[]{String.valueOf(id)});
        if (rowId != -1) {
          Toast.makeText(EditIssueActivity.this, "資料插入成功", Toast.LENGTH_SHORT).show();
          clearFields();
          back();
        } else {
          Toast.makeText(EditIssueActivity.this, "資料插入失敗", Toast.LENGTH_SHORT).show();
        }
      }
    });

    btnCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clearFields();
        back();
      }
    });
  }

  private void back() {
    SharedPreferences prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt("issue_Id", -1);
    editor.apply();
    Intent intent = new Intent(EditIssueActivity.this, ProjectActivity.class);
    startActivity(intent);
  }

  private void clearFields() {
    edName.setText("");
    edSummary.setText("");
    edStartTime.setText("");
    edEndTime.setText("");
    spin_Status.setSelection(0);
    edDesignee.setText("");
  }

  private String getCurrentLanguage() {
    return getSharedPrefs().getString("app_language", "zh");
  }
  private SharedPreferences getSharedPrefs() {
    return this.getSharedPreferences("FCUPrefs", MODE_PRIVATE);
  }
  ;
}