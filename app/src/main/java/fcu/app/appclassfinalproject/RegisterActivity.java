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

    // 設定註冊按鈕
    setupRegisterButton();

    // 切換至登入頁面
    tv_to_login.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        intentTo(LoginActivity.class);
      }
    });
  }

  // 設定註冊按鈕點擊事件
  private void setupRegisterButton() {
    btn_register.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String account = et_account.getText().toString().trim();
        String password = et_password.getText().toString();
        String email = et_email.getText().toString().trim();

        // 基本驗證
        if (!validateInput(account, password, email)) {
          return;
        }

        // 檢查網路
        if (!isNetworkAvailable()) {
          Toast.makeText(RegisterActivity.this,
              getString(R.string.register_check_network), Toast.LENGTH_SHORT).show();
          return;
        }

        // 開始註冊
        registerUser(email, password, account);
      }
    });
  }

  // 輸入驗證方法
  private boolean validateInput(String account, String password, String email) {
    if (account.isEmpty() || password.isEmpty() || email.isEmpty()) {
      Toast.makeText(this, getString(R.string.register_all_fields_required),
          Toast.LENGTH_SHORT).show();
      return false;
    }

    if (password.length() < 6) {
      Toast.makeText(this, getString(R.string.register_password_min_length),
          Toast.LENGTH_SHORT).show();
      return false;
    }

    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      Toast.makeText(this, getString(R.string.register_email_invalid_format),
          Toast.LENGTH_SHORT).show();
      return false;
    }

    return true;
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
    Toast.makeText(this, getString(R.string.register_registering), Toast.LENGTH_SHORT).show();

    mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            btn_register.setEnabled(true);

            if (task.isSuccessful()) {
              // 註冊成功
              FirebaseUser user = mAuth.getCurrentUser();
              if (user != null) {
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
                  saveUserToSharedPreferences(email, user.getUid(), localUserId, account);

                  Toast.makeText(RegisterActivity.this,
                      getString(R.string.register_success), Toast.LENGTH_SHORT).show();
                  intentTo(HomeActivity.class);
                } else {
                  Toast.makeText(RegisterActivity.this,
                      getString(R.string.register_sync_failed), Toast.LENGTH_LONG).show();
                  mAuth.signOut();
                  intentTo(LoginActivity.class);
                }
              }
            } else {
              // 註冊失敗，處理錯誤訊息
              handleRegistrationError(task);
            }
          }
        });
  }

  // 處理註冊錯誤
  private void handleRegistrationError(Task<AuthResult> task) {
    String errorMessage = getString(R.string.register_failed);

    if (task.getException() != null) {
      String error = task.getException().getMessage();
      if (error != null) {
        if (error.contains("email address is already in use")) {
          errorMessage = getString(R.string.register_email_already_used);
        } else if (error.contains("badly formatted")) {
          errorMessage = getString(R.string.register_email_badly_formatted);
        } else if (error.contains("weak password")) {
          errorMessage = getString(R.string.register_weak_password);
        } else if (error.contains("network")) {
          errorMessage = getString(R.string.register_network_error);
        }
      }
    }

    Log.w(TAG, "註冊失敗", task.getException());
    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
  }

  // 儲存用戶資料到 SharedPreferences
  private void saveUserToSharedPreferences(String email, String firebaseUid, int localUserId,
      String account) {
    SharedPreferences prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString("email", email);
    editor.putString("uid", firebaseUid);
    editor.putInt("user_id", localUserId);
    editor.putString("user_account", account);
    editor.putString("user_email", email);
    editor.apply();
  }

  // 切換頁面
  private void intentTo(Class<?> page) {
    Intent intent = new Intent(RegisterActivity.this, page);
    startActivity(intent);
    finish();
  }
}