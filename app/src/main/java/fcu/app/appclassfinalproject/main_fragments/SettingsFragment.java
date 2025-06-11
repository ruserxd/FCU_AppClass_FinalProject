package fcu.app.appclassfinalproject.main_fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import fcu.app.appclassfinalproject.ExportExcel;
import fcu.app.appclassfinalproject.LoginActivity;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.helper.SqlDataBaseHelper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.json.JSONArray;

public class SettingsFragment extends Fragment {

  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  private static final String TAG = "SettingsFragment";
  private Button btn_logout, btn_userFriend, btn_add_friend, btn_export_excel, btnChangeLanguage, btnProjectNumber, btnGithub, btn_del_account;
  private SQLiteDatabase db;

  private SqlDataBaseHelper sqlDataBaseHelper;
  private FirebaseAuth mAuth;

  private String mParam1;
  private String mParam2;

  public SettingsFragment() {
    // Required empty public constructor
  }

  public static SettingsFragment newInstance(String param1, String param2) {
    SettingsFragment fragment = new SettingsFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAuth = FirebaseAuth.getInstance();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_settings, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initViews(view);

    // 設定按鈕點擊事件
    setupClickListeners();
  }

  private void initViews(View view) {
    btn_logout = view.findViewById(R.id.btn_logout);
    btn_userFriend = view.findViewById(R.id.btn_userFriends);
    btn_add_friend = view.findViewById(R.id.btn_add_friend);
    btn_export_excel = view.findViewById(R.id.btn_excel);
    btnChangeLanguage = view.findViewById(R.id.btn_changeLanguage);
    btnProjectNumber = view.findViewById(R.id.btn_projectNumber);
    btnProjectNumber.setText(getString(R.string.setting_countporject, getCurrentProjectCount()));
    btnGithub = view.findViewById(R.id.btn_github);
    btn_del_account = view.findViewById(R.id.btn_delAccount);
  }

  private void setupClickListeners() {
    // 登出按鈕
    btn_logout.setOnClickListener(v -> logout());

    // 好友列表
    btn_userFriend.setOnClickListener(v -> navigateToFragment(new FriendFragment()));

    // 新增好友
    btn_add_friend.setOnClickListener(v -> navigateToFragment(new AddFriendFragment()));

    // 匯出 Excel
    btn_export_excel.setOnClickListener(v -> exportExcel());

    // 切換語言
    btnChangeLanguage.setOnClickListener(v -> changeLanguageSetting());

    //匯入GitHub專案
    btnGithub.setOnClickListener(v -> GithubInsert());

    // 刪除帳號
    btn_del_account.setOnClickListener(v -> showDeleteAccountConfirmDialog());
  }

  private void logout() {
    mAuth.signOut();

    getSharedPrefs().edit().clear().apply();

    Log.d(TAG, "用戶已登出");
    showToast(getString(R.string.logout_success));

    // 回到登入頁面
    Intent intent = new Intent(requireActivity(), LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    requireActivity().finish();
  }

  private void navigateToFragment(Fragment fragment) {
    requireActivity().getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_main, fragment)
        .addToBackStack(null)
        .commit();
  }

  private void exportExcel() {
    new ExportExcel(getContext(), db).exportToExcel("Project.xlsx");
  }

  private void changeLanguageSetting() {
    String currentLang = getCurrentLanguage();
    Log.d(TAG, "當前語言: " + currentLang);
    String newLang = currentLang.equals("zh") ? "en" : "zh";
    Log.d(TAG, "切換語言: " + newLang);

    Locale locale = new Locale(newLang);
    Locale.setDefault(locale);

    Configuration config = new Configuration();
    config.setLocale(locale);
    requireActivity().getResources().updateConfiguration(config,
        requireActivity().getResources().getDisplayMetrics());

    saveLanguage(newLang);
    showToast(getString(R.string.changeLanguage_success));
    updateLanguageAndReload(newLang);
  }

  // 預設中文
  private String getCurrentLanguage() {
    return getSharedPrefs().getString("app_language", "zh");
  }

  @SuppressLint("ApplySharedPref")
  private void saveLanguage(String language) {
    getSharedPrefs().edit().putString("app_language", language).commit();
  }

  private SharedPreferences getSharedPrefs() {
    return requireActivity().getSharedPreferences("FCUPrefs", MODE_PRIVATE);
  }

  private void updateLanguageAndReload(String language) {
    // 整個 activity 重來，延遲避免未更新就跳轉頁面
    new android.os.Handler().postDelayed(() -> {
      Intent intent = new Intent(requireActivity(), requireActivity().getClass());
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
      requireActivity().finish();
    }, 500);
  }

  /**
   * 獲取當前用戶參與的專案數量（新版本）
   */
  private String getCurrentProjectCount() {
    int userId = getSharedPrefs().getInt("user_id", -1);
    int count = 0;

    if (userId == -1) {
      Log.w(TAG, "User ID not found");
      return "0";
    }

    SqlDataBaseHelper dbHelper = null;
    SQLiteDatabase database = null;
    Cursor cursor = null;

    try {
      dbHelper = new SqlDataBaseHelper(getContext());
      database = dbHelper.getReadableDatabase();

      if (database != null) {
        // 查詢用戶參與的專案數量（通過 UserProject 表）
        String query = "SELECT COUNT(*) FROM UserProject WHERE user_id = ?";
        cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
          count = cursor.getInt(0);
        }
      }

      Log.i(TAG, "查詢用戶 ID " + userId + " 參與的專案數量: " + count);
    } catch (Exception e) {
      Log.e(TAG, "查詢專案數量失敗: " + e.getMessage());
      showToast(getString(R.string.project_count_query_failed, e.getMessage()));
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      if (database != null) {
        database.close();
      }
    }

    return String.valueOf(count);
  }

  private void GithubInsert() {
    sqlDataBaseHelper = new SqlDataBaseHelper(this.getContext());
    db = sqlDataBaseHelper.getWritableDatabase();
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle(getString(R.string.github_input_title));

    final EditText input = new EditText(requireContext());
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    builder.setView(input);

    builder.setPositiveButton(getString(R.string.github_confirm), (dialog, which) -> {
      String username = input.getText().toString().trim();
      if (!username.isEmpty()) {
        fetchGithub(username);
      }
    });

    builder.setNegativeButton(getString(R.string.github_cancel),
        (dialog, which) -> dialog.cancel());
    builder.show();
  }

  // 抓取 GitHub API 上的資訊透過獲取 JSON -> name 的資訊
  private void fetchGithub(String username) {
    CompletableFuture
        .supplyAsync(() -> {
          try {
            return fetchGithubRepos(username);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        })
        .thenAccept(this::saveReposToDatabase)
        .thenRun(() -> {
          showToast(getString(R.string.github_import_success));
          // 更新專案計數
          updateProjectCount();
        })
        .exceptionally(throwable -> {
          showToast(getString(R.string.github_import_error, throwable.getMessage()));
          return null;
        });
  }

  // 從 GitHub API 獲取使用者的所有專案
  private JSONArray fetchGithubRepos(String username) throws Exception {
    URL url = new URL("https://api.github.com/users/" + username + "/repos");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    StringBuilder json = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      json.append(line);
    }
    reader.close();

    return new JSONArray(json.toString());
  }

  /**
   * 將 GitHub 專案存入資料庫（新版本 - 使用簡化結構）
   */
  private void saveReposToDatabase(JSONArray repos) {
    try {
      int userId = requireContext().getSharedPreferences("FCUPrefs", MODE_PRIVATE)
          .getInt("user_id", 0);

      if (userId == 0) {
        Log.e(TAG, "User ID not found, cannot import GitHub repos");
        return;
      }

      int importedCount = 0;
      db.beginTransaction();

      try {
        for (int i = 0; i < repos.length(); i++) {
          String repoName = repos.getJSONObject(i).getString("name");
          String description = repos.getJSONObject(i).optString("description", "");

          // 檢查專案名稱是否已存在
          Cursor existingProject = db.rawQuery(
              "SELECT id FROM Projects WHERE name = ?",
              new String[]{repoName}
          );

          if (existingProject.moveToFirst()) {
            // 專案已存在，檢查用戶是否已是成員
            int existingProjectId = existingProject.getInt(0);
            existingProject.close();

            Cursor existingMember = db.rawQuery(
                "SELECT 1 FROM UserProject WHERE user_id = ? AND project_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(existingProjectId)}
            );

            if (!existingMember.moveToFirst()) {
              // 用戶不是成員，加入專案
              ContentValues relation = new ContentValues();
              relation.put("user_id", userId);
              relation.put("project_id", existingProjectId);
              db.insert("UserProject", null, relation);
              importedCount++;
              Log.d(TAG, "User added to existing project: " + repoName);
            }
            existingMember.close();
          } else {
            existingProject.close();

            // 新增專案（簡化版本：無 manager_id）
            ContentValues project = new ContentValues();
            project.put("name", repoName);
            project.put("summary", description.isEmpty() ? "Imported from GitHub" : description);
            long projectId = db.insert("Projects", null, project);

            if (projectId != -1) {
              // 將用戶加入專案
              ContentValues relation = new ContentValues();
              relation.put("user_id", userId);
              relation.put("project_id", projectId);
              db.insert("UserProject", null, relation);
              importedCount++;
              Log.d(TAG, "New project created and user added: " + repoName);
            }
          }
        }

        db.setTransactionSuccessful();
        Log.d(TAG, "GitHub import completed. Total imported/joined: " + importedCount);

      } finally {
        db.endTransaction();
      }

    } catch (Exception e) {
      Log.e(TAG, "Error saving GitHub repos: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }

  /**
   * 更新專案計數顯示
   */
  private void updateProjectCount() {
    requireActivity().runOnUiThread(() -> {
      if (btnProjectNumber != null) {
        btnProjectNumber.setText(
            getString(R.string.setting_countporject, getCurrentProjectCount()));
      }
    });
  }

  // 顯示 Toast 訊息
  private void showToast(String message) {
    requireActivity().runOnUiThread(() ->
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
  }

  // 顯示刪除帳號確認對話框
  private void showDeleteAccountConfirmDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle(getString(R.string.delete_account_title));
    builder.setMessage(getString(R.string.delete_account_message));
    builder.setIcon(android.R.drawable.ic_dialog_alert);

    builder.setPositiveButton(getString(R.string.delete_account_confirm), (dialog, which) -> {
      showPasswordConfirmDialog();
    });

    builder.setNegativeButton(getString(R.string.delete_account_cancel), (dialog, which) -> {
      dialog.dismiss();
    });

    AlertDialog dialog = builder.create();
    dialog.show();

    // 設定確定按鈕為紅色，表示危險操作
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
        requireContext().getResources().getColor(android.R.color.holo_red_dark));
  }

  // 顯示密碼確認對話框
  private void showPasswordConfirmDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle(getString(R.string.password_confirm_title));
    builder.setMessage(getString(R.string.password_confirm_message));

    final EditText passwordInput = new EditText(requireContext());
    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    passwordInput.setHint(getString(R.string.password_hint));
    builder.setView(passwordInput);

    builder.setPositiveButton(getString(R.string.password_confirm_delete), (dialog, which) -> {
      String password = passwordInput.getText().toString().trim();
      if (password.isEmpty()) {
        showToast(getString(R.string.password_required));
        showPasswordConfirmDialog(); // 重新顯示對話框
      } else {
        deleteAccount(password);
      }
    });

    builder.setNegativeButton(getString(R.string.delete_account_cancel),
        (dialog, which) -> dialog.dismiss());

    AlertDialog dialog = builder.create();
    dialog.show();

    // 設定確定按鈕為紅色
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
        requireContext().getResources().getColor(android.R.color.holo_red_dark));
  }

  // 刪除帳號主要方法
  private void deleteAccount(String password) {
    // 顯示進度提示
    showToast(getString(R.string.deleting_account));

    // 禁用刪除按鈕，避免重複點擊
    btn_del_account.setEnabled(false);

    // 獲取當前用戶
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser == null) {
      showToast(getString(R.string.user_auth_failed));
      btn_del_account.setEnabled(true);
      return;
    }

    String email = currentUser.getEmail();
    if (email == null) {
      showToast(getString(R.string.cannot_get_email));
      btn_del_account.setEnabled(true);
      return;
    }

    // 重新驗證用戶身份
    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
    currentUser.reauthenticate(credential)
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            Log.d(TAG, "用戶重新驗證成功");
            // 先刪除本地資料，再刪除 Firebase 帳號
            deleteLocalUserData();
          } else {
            Log.w(TAG, "用戶重新驗證失敗", task.getException());
            String errorMessage = getString(R.string.password_verification_failed);
            if (task.getException() != null) {
              String error = task.getException().getMessage();
              if (error != null && error.contains("password")) {
                errorMessage = getString(R.string.password_incorrect);
              }
            }
            showToast(errorMessage);
            btn_del_account.setEnabled(true);
          }
        });
  }

  /**
   * 刪除本地用戶資料（更新版本 - 適應新資料庫結構）
   */
  private void deleteLocalUserData() {
    int userId = getSharedPrefs().getInt("user_id", -1);
    if (userId == -1) {
      Log.e(TAG, "無法獲取用戶 ID");
      deleteFirebaseAccount(); // 直接刪除 Firebase 帳號
      return;
    }

    SqlDataBaseHelper dbHelper = new SqlDataBaseHelper(requireContext());
    SQLiteDatabase database = null;

    try {
      database = dbHelper.getWritableDatabase();
      database.beginTransaction();

      // 刪除使用者與議題的關聯
      String deleteUserIssueQuery = "DELETE FROM UserIssue WHERE user_id = ?";
      database.execSQL(deleteUserIssueQuery, new String[]{String.valueOf(userId)});
      Log.d(TAG, "刪除用戶議題關聯");

      // 刪除使用者參與的專案中的議題（如果用戶是唯一成員）
      String deleteIssuesQuery = "DELETE FROM Issues WHERE project_id IN " +
          "(SELECT project_id FROM UserProject WHERE user_id = ? " +
          "GROUP BY project_id HAVING COUNT(*) = 1)";
      database.execSQL(deleteIssuesQuery, new String[]{String.valueOf(userId)});
      Log.d(TAG, "刪除用戶唯一參與專案的議題");

      // 刪除使用者唯一參與的專案
      String deleteProjectsQuery = "DELETE FROM Projects WHERE id IN " +
          "(SELECT project_id FROM UserProject WHERE user_id = ? " +
          "GROUP BY project_id HAVING COUNT(*) = 1)";
      database.execSQL(deleteProjectsQuery, new String[]{String.valueOf(userId)});
      Log.d(TAG, "刪除用戶唯一參與的專案");

      // 刪除使用者與專案關聯
      int deletedUserProjects = database.delete("UserProject", "user_id = ?",
          new String[]{String.valueOf(userId)});
      Log.d(TAG, "刪除用戶專案關聯：" + deletedUserProjects + " 筆");

      // 刪除使用者的好友關係
      int deletedFriends1 = database.delete("Friends", "user_id = ?",
          new String[]{String.valueOf(userId)});
      int deletedFriends2 = database.delete("Friends", "friend_id = ?",
          new String[]{String.valueOf(userId)});
      Log.d(TAG, "刪除好友關係：" + (deletedFriends1 + deletedFriends2) + " 筆");

      // 刪除使用者本身
      int deletedUser = database.delete("Users", "id = ?",
          new String[]{String.valueOf(userId)});
      Log.d(TAG, "刪除用戶：" + deletedUser + " 筆");

      database.setTransactionSuccessful();
      Log.d(TAG, "本地資料刪除成功");

      // 刪除 Firebase 帳號
      deleteFirebaseAccount();

    } catch (Exception e) {
      Log.e(TAG, "刪除本地資料時發生錯誤: " + e.getMessage());
      showToast(getString(R.string.delete_local_data_failed, e.getMessage()));
      btn_del_account.setEnabled(true);
    } finally {
      if (database != null) {
        if (database.inTransaction()) {
          database.endTransaction();
        }
        database.close();
      }
    }
  }

  // 刪除 Firebase 帳號
  private void deleteFirebaseAccount() {
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser != null) {
      currentUser.delete()
          .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
              Log.d(TAG, "Firebase 帳號刪除成功");

              // 清除 SharedPreferences
              getSharedPrefs().edit().clear().apply();

              // 顯示成功訊息
              showToast(getString(R.string.account_deleted_success));

              // 回到登入頁面
              navigateToLogin();

            } else {
              Log.w(TAG, "Firebase 帳號刪除失敗", task.getException());
              showToast(getString(R.string.account_delete_failed));
              btn_del_account.setEnabled(true);
            }
          });
    } else {
      getSharedPrefs().edit().clear().apply();
      showToast(getString(R.string.account_data_cleared));
      navigateToLogin();
    }
  }

  private void navigateToLogin() {
    Intent intent = new Intent(requireActivity(), LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    requireActivity().finish();
  }
}