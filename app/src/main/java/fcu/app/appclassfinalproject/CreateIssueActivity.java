package fcu.app.appclassfinalproject;

import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateIssueActivity extends AppCompatActivity {
    private EditText etPurpose;
    private EditText etOverview;
    private EditText etStartTime;
    private EditText etEndTime;
    private EditText etStatus;
    private EditText etDesignee;

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

        //找對應id
        etPurpose = findViewById(R.id.et_purpose);
        etOverview = findViewById(R.id.et_overview);
        etStartTime = findViewById(R.id.et_start_time);
        etEndTime = findViewById(R.id.et_endtime);
        etStatus = findViewById(R.id.et_status);
        etDesignee = findViewById(R.id.et_designee);
    }
}