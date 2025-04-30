package fcu.app.appclassfinalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {
    private EditText et_account;
    private EditText et_password;
    private EditText et_email;
    private Button btn_login;
    private TextView tv_to_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 找尋對應 id
        et_account = findViewById(R.id.et_register_account);
        et_password = findViewById(R.id.et_register_password);
        et_email = findViewById(R.id.et_register_email);
        btn_login = findViewById(R.id.btn_register);
        tv_to_login = findViewById(R.id.tv_to_register);

        tv_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}