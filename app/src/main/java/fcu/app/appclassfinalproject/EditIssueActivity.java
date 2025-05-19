package fcu.app.appclassfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class EditIssueActivity extends AppCompatActivity {

  private EditText edName;
  private EditText edSummary;
  private EditText edStartTime;
  private EditText edEndTime;
  private EditText edStatus;
  private EditText edDesignee;
  private ImageButton btnHome;
  private ImageButton btnSave;
  private ImageButton btnCancel;

  private FirebaseFirestore db;

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
//        edStatus = findViewById(R.id.ed_status);
//        edDesignee = findViewById(R.id.ed_designee);

    btnHome = findViewById(R.id.ed_btn_back);
    btnSave = findViewById(R.id.ed_btn_save);
    btnCancel = findViewById(R.id.ed_btn_cancel);

//  TODO:須補issue id相關程式碼
//      假設有issue ID
    int id = 1;

//        初始化編輯議題內容
    db.collection("issue").whereEqualTo("id", id).get().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        for (QueryDocumentSnapshot doc : task.getResult()) {
          String name = doc.getString("name");
          String summary = doc.getString("summary");
          String startTime = doc.getString("start_time");
          String endTime = doc.getString("end_time");

//                    TODO:不確定有沒有
//                    String status= doc.getString("status");
//                    String designee= doc.getString("designee");
          edName.setText(name);
          edSummary.setText(summary);
          edStartTime.setText(startTime);
          edEndTime.setText(endTime);
        }
      }

    });
//      TODO:補preferences
//        SharedPreferences prefs = getSharedPreferences("", MODE_PRIVATE);
//        String projectId = prefs.getString("projectId",null);

    // 假設 sharedPreference 有　"projectId"
    btnHome.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(EditIssueActivity.this, ProjectActivity.class);
        startActivity(intent);
      }
    });

    btnSave.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String name = edName.getText().toString().trim();
        String summary = edSummary.getText().toString().trim();
        String start_time = edStartTime.getText().toString().trim();
        String end_time = edEndTime.getText().toString().trim();
//               TODO:確認有無狀態及被指派者
//                String status = etStatus.getText().toString().trim();
//                String designee = etDesignee.getText().toString().trim();

        db.collection("issue").whereEqualTo("id", id).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
              for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                // 使用 map 更新多個欄位
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", name);
                updates.put("summary", summary);
                updates.put("start_time", start_time);
                updates.put("end_time", end_time);

                doc.getReference().update(updates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void unused) {
                        Toast.makeText(EditIssueActivity.this, "更新成功", Toast.LENGTH_LONG)
                            .show();
                        clearFields();
                      }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditIssueActivity.this, "更新失敗，請檢查輸入是否正確",
                            Toast.LENGTH_LONG).show();
                      }
                    });
              }


            });


      }
    });

    btnCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        db.collection("issue").whereEqualTo("id", id).get().addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            for (QueryDocumentSnapshot doc : task.getResult()) {
              String name = doc.getString("name");
              String summary = doc.getString("summary");
              String startTime = doc.getString("start_time");
              String endTime = doc.getString("end_time");

//                    TODO:不確定有沒有
//                    String status= doc.getString("status");
//                    String designee= doc.getString("designee");
              edName.setText(name);
              edSummary.setText(summary);
              edStartTime.setText(startTime);
              edEndTime.setText(endTime);
            }
          }

        });
      }
    });
  }

  private void clearFields() {
    edName.setText("");
    edSummary.setText("");
    edStartTime.setText("");
    edEndTime.setText("");
  }

  ;
}