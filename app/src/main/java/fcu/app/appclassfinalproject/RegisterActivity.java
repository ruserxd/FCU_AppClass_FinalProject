package fcu.app.appclassfinalproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;

public class RegisterActivity extends AppCompatActivity {

  // 元件
  private EditText et_account;
  private EditText et_password;
  private EditText et_email;
  private Button btn_register;
  private TextView tv_to_login;

  private SqlDataBaseHelper sqlDataBaseHelper;
  private SQLiteDatabase db;
  private FirebaseAuth mAuth;


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

    FirebaseApp.initializeApp(this);
    mAuth = FirebaseAuth.getInstance();

    // 登入按鈕
    btn_register.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String account = et_account.getText().toString();
        String password = et_password.getText().toString();
        String email = et_email.getText().toString();

        // 檢查輸入不為空
        if (account.isEmpty() || password.isEmpty() || email.isEmpty()) {
          Toast.makeText(RegisterActivity.this, "所有欄位不能為空", Toast.LENGTH_SHORT).show();
          return;
        }

        // 初始化資料庫
        sqlDataBaseHelper = new SqlDataBaseHelper(getBaseContext());
        db = sqlDataBaseHelper.getWritableDatabase();

        // 與 firebase 進行註冊
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  FirebaseUser user = mAuth.getCurrentUser();

                  try {
                    //放入到 SQLite 中
                    ContentValues values = new ContentValues();
                    values.put("account", account);
                    values.put("password", password);
                    values.put("email", email);

                    long newRowId = db.insert("Users", null, values);

                    if (newRowId == -1) {
                      Log.e("DATABASE", "插入 Users 表失敗，返回 -1");
                      Toast.makeText(RegisterActivity.this, "資料庫插入失敗", Toast.LENGTH_SHORT)
                          .show();
                    } else {
                      Log.d("DATABASE", "成功插入 Users 表，新行 ID: " + newRowId);
                      Toast.makeText(RegisterActivity.this, "註冊成功：" + user.getEmail(),
                          Toast.LENGTH_SHORT).show();

                      intentTo(LoginActivity.class);
                    }
                  } catch (Exception e) {
                    Log.e("DATABASE", "資料庫操作異常: " + e.getMessage(), e);
                    Toast.makeText(RegisterActivity.this, "資料庫操作錯誤: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                  }
                }
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                // 捕獲任何可能發生的異常
                String errorMessage = "註冊過程發生錯誤: " + e.getMessage();
                Log.e("FIREBASE_AUTH", errorMessage, e);
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
              }
            });
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
    finish();
  }
}