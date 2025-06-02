package fcu.app.appclassfinalproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

        // 基本驗證
        if (account.isEmpty() || password.isEmpty() || email.isEmpty()) {
          Toast.makeText(RegisterActivity.this, "所有欄位不能為空", Toast.LENGTH_SHORT).show();
          return;
        }

        if (password.length() < 6) {
          Toast.makeText(RegisterActivity.this, "密碼至少需要6個字元", Toast.LENGTH_SHORT).show();
          return;
        }

        // 檢查網路
        if (!isNetworkAvailable()) {
          Toast.makeText(RegisterActivity.this, "請檢查網路連線", Toast.LENGTH_SHORT).show();
          return;
        }

        // 開始註冊
        registerUser(email, password, account);
      }
    });

    // 切換至登入頁面
    tv_to_login.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        intentTo(LoginActivity.class);
      }
    });
  }

  // 檢查網路連線
  private boolean isNetworkAvailable() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm != null) {
      NetworkInfo networkInfo = cm.getActiveNetworkInfo();
      return networkInfo != null && networkInfo.isConnected();
    }
    return false;
  }

  // 註冊用戶
  private void registerUser(String email, String password, String account) {
    btn_register.setEnabled(false);
    Toast.makeText(this, "註冊中...", Toast.LENGTH_SHORT).show();

    mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            btn_register.setEnabled(true);

            if (task.isSuccessful()) {
              // 註冊成功
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
                // 保存登入狀態
                SharedPreferences prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", email);
                editor.putString("uid", user.getUid());
                editor.putInt("user_id", localUserId);
                editor.apply();

                Toast.makeText(RegisterActivity.this, "註冊成功！", Toast.LENGTH_SHORT).show();
                intentTo(HomeActivity.class);
              } else {
                Toast.makeText(RegisterActivity.this, "資料同步失敗，請重新登入", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                intentTo(LoginActivity.class);
              }
            } else {
              String errorMessage = "註冊失敗";
              if (task.getException() != null) {
                String error = task.getException().getMessage();
                if (error != null) {
                  if (error.contains("email address is already in use")) {
                    errorMessage = "此電子郵件已被註冊";
                  } else if (error.contains("badly formatted")) {
                    errorMessage = "電子郵件格式不正確";
                  } else if (error.contains("weak password")) {
                    errorMessage = "密碼強度不足";
                  } else if (error.contains("network")) {
                    errorMessage = "網路連接錯誤，請稍後重試";
                  }
                }
              }

              Log.w(TAG, "註冊失敗", task.getException());
              Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
          }
        });
  }

  // 切換頁面
  private void intentTo(Class<?> page) {
    Intent intent = new Intent(RegisterActivity.this, page);
    startActivity(intent);
    finish();
  }
}