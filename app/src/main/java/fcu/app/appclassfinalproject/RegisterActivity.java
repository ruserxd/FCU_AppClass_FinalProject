package fcu.app.appclassfinalproject;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class RegisterActivity extends AppCompatActivity {

  // 元件
  private EditText et_account;
  private EditText et_password;
  private EditText et_email;
  private Button btn_register;
  private TextView tv_to_login;

  FirebaseAuth mAuth;
  private static final String TAG = "RegisterActivity";

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

    // 註冊按鈕
    btn_register.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String account = et_account.getText().toString().trim();
        String password = et_password.getText().toString();
        String email = et_email.getText().toString().trim();

        // 檢查輸入不為空
        if (account.isEmpty() || password.isEmpty() || email.isEmpty()) {
          Toast.makeText(RegisterActivity.this, "所有欄位不能為空", Toast.LENGTH_SHORT).show();
          return;
        }

        // 基本的密碼強度檢查
        if (password.length() < 6) {
          Toast.makeText(RegisterActivity.this, "密碼至少需要6個字元", Toast.LENGTH_SHORT).show();
          return;
        }

        // 與 Firebase 進行註冊
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  // Firebase 註冊成功
                  FirebaseUser user = mAuth.getCurrentUser();
                  Log.d(TAG, "Firebase 註冊成功: " + user.getEmail());

                  // 同步到本地數據庫
                  int localUserId = UserSyncHelper.syncFirebaseUserWithLocalDB(
                      RegisterActivity.this,
                      user.getUid(),
                      user.getEmail(),
                      account
                  );

                  if (localUserId != -1) {
                    // 註冊成功，保存登入狀態
                    SharedPreferences prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("email", email);
                    editor.putString("uid", user.getUid());
                    editor.putInt("user_id", localUserId);
                    editor.apply();

                    Log.d(TAG, "本地數據庫同步成功，用戶 ID: " + localUserId);
                    Toast.makeText(RegisterActivity.this, "註冊成功！", Toast.LENGTH_SHORT).show();

                    // 直接進入主頁面，不需要再次登入
                    intentTo(HomeActivity.class);
                  } else {
                    // 本地數據庫同步失敗
                    Log.e(TAG, "本地數據庫同步失敗");
                    Toast.makeText(RegisterActivity.this, "註冊成功，但資料同步失敗，請重新登入",
                        Toast.LENGTH_LONG).show();

                    // 登出 Firebase 用戶，讓用戶重新登入
                    mAuth.signOut();
                    intentTo(LoginActivity.class);
                  }
                } else {
                  // Firebase 註冊失敗
                  String errorMessage = "註冊失敗";
                  Log.w(TAG, "Firebase 註冊失敗", task.getException());

                  if (task.getException() != null) {
                    String error = task.getException().getMessage();
                    if (error != null) {
                      if (error.contains("email address is already in use")) {
                        errorMessage = "此電子郵件已被註冊";
                      } else if (error.contains("badly formatted")) {
                        errorMessage = "電子郵件格式不正確";
                      } else if (error.contains("weak password")) {
                        errorMessage = "密碼強度不足";
                      } else if (error.contains("network error")) {
                        errorMessage = "網路連接錯誤";
                      }
                    }
                  }

                  Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                // 捕獲任何可能發生的異常
                String errorMessage = "註冊過程發生錯誤: " + e.getMessage();
                Log.e(TAG, errorMessage, e);
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
              }
            });
      }
    });

    // 切換至登入頁面
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