package fcu.app.appclassfinalproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

  // 元件
  private EditText et_login_email;
  private EditText et_login_password;
  private Button btn_login, btn_translate;
  private TextView tv_to_register;

  // Firebase
  private FirebaseAuth mAuth;

  private SharedPreferences prefs;
  private static final String TAG = "LoginActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // 設定語言 - 必須在 setContentView 之前
    setLocale();

    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_login);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_login), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    // 初始化 Firebase Auth
    FirebaseApp.initializeApp(this);
    mAuth = FirebaseAuth.getInstance();

    // 找尋對應 id
    et_login_email = findViewById(R.id.et_login_email);
    et_login_password = findViewById(R.id.et_login_password);
    btn_login = findViewById(R.id.btn_login);
    tv_to_register = findViewById(R.id.tv_to_register);
    btn_translate = findViewById(R.id.btn_translate);

    // 在 home 按下返回鍵，firebase的使用者確定有登入過，不會回到此頁面
    if (mAuth.getCurrentUser() != null) {
      intentTo(HomeActivity.class);
    }

    // 當按下 "尚未註冊" 文字時
    tv_to_register.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        intentTo(RegisterActivity.class);
      }
    });

    // 語言切換按鈕
    btn_translate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        toggleLanguage();
      }
    });

    // 登入檢測
    btn_login.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String email = et_login_email.getText().toString().trim();
        String password = et_login_password.getText().toString();

        // 檢查輸入是否為空
        if (email.isEmpty() || password.isEmpty()) {
          Toast.makeText(LoginActivity.this, getString(R.string.register_all_fields_required), Toast.LENGTH_SHORT).show();
          return;
        }

        // 使用 Firebase 進行登入
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                  // 登入成功
                  Log.d(TAG, "signInWithEmail:success");
                  FirebaseUser user = mAuth.getCurrentUser();

                  // 同步 Firebase 用戶與本地數據庫
                  int localUserId = UserSyncHelper.syncFirebaseUserWithLocalDB(
                      LoginActivity.this,
                      user.getUid(),
                      user.getEmail(),
                      null
                  );

                  // 存入登入狀態
                  prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
                  SharedPreferences.Editor editor = prefs.edit();
                  editor.putString("email", email);
                  editor.putString("uid", user.getUid());
                  editor.putInt("user_id", localUserId);
                  editor.apply();

                  Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                  // 切換至主頁面
                  intentTo(HomeActivity.class);
                  finish();
                } else {
                  // 登入失敗
                  String errorMessage = getString(R.string.login_failed);
                  Log.w(TAG, "signInWithEmail:failure", task.getException());
                  if (task.getException() != null) {

                    String error = task.getException().getMessage();
                    if (error != null) {
                      if (error.contains("password is invalid")) {
                        errorMessage = getString(R.string.password_incorrect);
                      } else if (error.contains("no user record")) {
                        errorMessage = getString(R.string.email_not_registered);
                      } else if (error.contains("badly formatted")) {
                        errorMessage = getString(R.string.email_format_incorrect);
                      }
                    }
                  }

                  Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
              }
            });
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    // 檢查用戶是否已經登入
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser != null) {
      // 用戶已登入，直接進入主頁面
      prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putString("email", currentUser.getEmail());
      editor.putString("uid", currentUser.getUid());
      editor.apply();

      intentTo(HomeActivity.class);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    // 檢查 Firebase 用戶是否登入
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser != null) {
      // 用戶已登入，直接進入主頁面
      intentTo(HomeActivity.class);
    }
  }

  // 切換至 "指定" 頁面
  private void intentTo(Class<?> page) {
    Intent intent = new Intent();
    intent.setClass(LoginActivity.this, page);
    startActivity(intent);
  }

  // 設定語言
  private void setLocale() {
    prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    String language = prefs.getString("language", "zh");

    Locale locale = new Locale(language);
    Locale.setDefault(locale);

    Configuration config = new Configuration();
    config.setLocale(locale);
    getResources().updateConfiguration(config, getResources().getDisplayMetrics());
  }

  // 切換語言
  private void toggleLanguage() {
    prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    String currentLang = prefs.getString("language", "zh");
    Log.d(TAG, "Current language: " + currentLang);

    String newLang = currentLang.equals("zh") ? "en" : "zh";
    Log.d(TAG, "Switching to language: " + newLang);

    SharedPreferences.Editor editor = prefs.edit();
    editor.putString("language", newLang);
    editor.apply();

    // 重新啟動 Activity 以套用新語言
    recreate();
  }
}