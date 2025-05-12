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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateIssueActivity extends AppCompatActivity {
    private EditText etName;
    private EditText etSummary;
    private EditText etStartTime;
    private EditText etEndTime;
    private EditText etStatus;
    private EditText etDesignee;
    private ImageButton btnHome;
    private ImageButton btnSave;

    //TODO:確認使用Fire base/ SQLite
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_issue);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        //找對應id
        etName = findViewById(R.id.et_name);
        etSummary = findViewById(R.id.et_summary);
        etStartTime = findViewById(R.id.et_start_time);
        etEndTime = findViewById(R.id.et_endtime);
        etStatus = findViewById(R.id.et_status);
        etDesignee = findViewById(R.id.et_designee);

        btnHome = findViewById(R.id.cr_btn_back);
        btnSave = findViewById(R.id.cr_btn_save);

//      TODO:補preferences
//        SharedPreferences prefs = getSharedPreferences("", MODE_PRIVATE);
//        String projectId = prefs.getString("projectId",null);

        // 假設 sharedPreference 有　"projectId"
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateIssueActivity.this, ProjectActivity.class);
                startActivity(intent);
            }
        });

        // 假設 sharedPreference 有　"projectId"
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = etName.getText().toString().trim();
                String summary = etSummary.getText().toString().trim();
                String start_time = etStartTime.getText().toString().trim();
                String end_time = etEndTime.getText().toString().trim();
//               TODO:確認有無狀態及被指派者
//                String status = etStatus.getText().toString().trim();
//                String designee = etDesignee.getText().toString().trim();


                Map<String, Object> issue = new HashMap<>();
//                TODO:新增issue id
//                issue.put("id",id);
                issue.put("name", name);
                issue.put("summary", summary);
                issue.put("start_time", start_time);
                issue.put("end_time", end_time);
//                issue.put("project_id",projectId);
//                issue.put("status",status);
//                issue.put("designee",designee);
                db.collection("issues").add(issue).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(CreateIssueActivity.this, "建立成功，ID:", Toast.LENGTH_LONG).show();
                        clearFields();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateIssueActivity.this, "建立失敗，請檢查輸入是否正確", Toast.LENGTH_LONG).show();
                    }
                });


            }
        });
    }

    private void clearFields() {
        etName.setText("");
        etSummary.setText("");
        etStartTime.setText("");
        etEndTime.setText("");
    }

}