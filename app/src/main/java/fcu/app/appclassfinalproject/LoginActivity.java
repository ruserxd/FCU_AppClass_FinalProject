package fcu.app.appclassfinalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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

public class LoginActivity extends AppCompatActivity {
    // 元件
    private EditText et_login_account;
    private EditText et_login_password;
    private Button btn_login;
    private TextView tv_to_register;

    // SQL
    private static final String DATABASENAME = "FCU_FinalProjectDataBase";
    private static final int DATABASEVERSION = 1;
    private static final String TABLENAME = "Users";
    private SqlDataBaseHelper sqlDataBaseHelper;
    private SQLiteDatabase db;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 找尋對應 id
        et_login_account = findViewById(R.id.et_login_account);
        et_login_password = findViewById(R.id.et_login_password);
        btn_login = findViewById(R.id.btn_login);
        tv_to_register = findViewById(R.id.tv_to_register);

        // 當按下 "尚未註冊" 文字時
        tv_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentTo(RegisterActivity.class);
            }
        });

        sqlDataBaseHelper = new SqlDataBaseHelper(this.getBaseContext(), DATABASENAME, null, DATABASEVERSION, TABLENAME);
        db = sqlDataBaseHelper.getWritableDatabase();

        // 登入檢測
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = et_login_account.getText().toString();
                String password = et_login_password.getText().toString();

                Cursor c = db.rawQuery(
                        "SELECT * FROM " + TABLENAME + " WHERE account = ? AND password = ?",
                        new String[]{account, password}
                );
                boolean isLogin = c.getCount() != 0;
                c.close();

                // 判斷是否登入成功
                if (isLogin) {
                    // 存入登入狀態
                    prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLogin", true);
                    editor.apply();

                    // 切換至主頁面
                    intentTo(HomeActivity.class);
                    Toast.makeText(getApplicationContext(), "登入成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "登入失敗", Toast.LENGTH_SHORT).show();
                }

                // 設為空的
                et_login_account.setText("");
                et_login_password.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
        // 若已登入過不重複登入
        boolean isLogin = prefs.getBoolean("isLogin", false);
        if (isLogin) {
            // 切換至主頁面
            intentTo(HomeActivity.class);
        }
    }

    // 切換至 "指定" 頁面
    private void intentTo(Class<?> page) {
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, page);
        startActivity(intent);
    }
}