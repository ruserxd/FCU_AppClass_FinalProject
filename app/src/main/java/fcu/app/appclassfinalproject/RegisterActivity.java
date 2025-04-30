package fcu.app.appclassfinalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;

public class RegisterActivity extends AppCompatActivity {
    // 元件
    private EditText et_account;
    private EditText et_password;
    private EditText et_email;
    private Button btn_register;
    private TextView tv_to_login;


    // SQL
    private static final String DATABASENAME = "FCU_FinalProjectDataBase";
    private static final int DATABASEVERSION = 1;
    private static final String TABLENAME = "Users";
    private SqlDataBaseHelper sqlDataBaseHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 找尋對應 id
        et_account = findViewById(R.id.et_register_account);
        et_password = findViewById(R.id.et_register_password);
        et_email = findViewById(R.id.et_register_email);
        btn_register = findViewById(R.id.btn_register);
        tv_to_login = findViewById(R.id.tv_to_register);

        sqlDataBaseHelper = new SqlDataBaseHelper(this.getBaseContext(), DATABASENAME, null, DATABASEVERSION, TABLENAME);
        db = sqlDataBaseHelper.getWritableDatabase();


        // TODO: 登入資料的儲存
        // 登入按鈕
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = et_account.getText().toString();
                String password = et_password.getText().toString();
                String email = et_email.getText().toString();

                ContentValues values = new ContentValues();
                values.put("account", account);
                values.put("password", password);
                values.put("email", email);
                db.insert(TABLENAME, null, values);

                et_account.setText("");
                et_password.setText("");
                et_email.setText("");

                // 切換至 Login 頁面
                intentTo(LoginActivity.class);

                // 註冊成功的回應
                Toast.makeText(getApplicationContext(), "註冊成功", Toast.LENGTH_SHORT).show();
            }
        });

        // 切換至 註冊 頁面
        tv_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 切換至 Login 頁面
                intentTo(LoginActivity.class);
            }
        });
    }

    // 切換至 "指定" 頁面
    private void intentTo(Class<?> page) {
        Intent intent = new Intent();
        intent.setClass(RegisterActivity.this, page);
        startActivity(intent);
    }
}