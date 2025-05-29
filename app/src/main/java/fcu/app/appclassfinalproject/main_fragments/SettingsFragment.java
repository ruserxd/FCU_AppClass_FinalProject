package fcu.app.appclassfinalproject.main_fragments;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.DatabaseUtils;
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
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import fcu.app.appclassfinalproject.ExportExcel;
import fcu.app.appclassfinalproject.LoginActivity;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class SettingsFragment extends Fragment {

  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  private static final String TAG = "SettingsFragment";
  private Button btn_logout, btn_userFriend, btn_add_friend, btn_export_excel, btnChangeLanguage, btnProjectNumber,btnGithub;
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
  }

  private void logout() {
    mAuth.signOut();

    getSharedPrefs().edit().clear().apply();

    Log.d(TAG, "用戶已登出");
    Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show();

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
    Toast.makeText(requireContext(), R.string.changeLanguage_success, Toast.LENGTH_SHORT).show();
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

  private String getCurrentProjectCount() {
    int userId = getSharedPrefs().getInt("user_id", -1);
    long count = 0;
    try {
      SqlDataBaseHelper dbHelper = new SqlDataBaseHelper(getContext());
      db = dbHelper.getReadableDatabase();

      if (db != null && userId != -1) {
        count = DatabaseUtils.queryNumEntries(db, "Projects", "manager_id = ?",
            new String[]{String.valueOf(userId)});
      }
      Log.i("SettingsFragment", "查詢用戶 ID " + userId + " 有多少 project : " + count);
    } catch (Exception e) {
      Log.e("SettingsFragment", "獲取專案數量失敗: " + e.getMessage());
    } finally {
      if (db != null) {
        db.close();
      }
    }
    return String.valueOf(count);
  }

  private void GithubInsert() {
    sqlDataBaseHelper = new SqlDataBaseHelper(this.getContext());
    db = sqlDataBaseHelper.getWritableDatabase();
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle("輸入 GitHub 帳號");

    final EditText input = new EditText(requireContext());
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    builder.setView(input);

    builder.setPositiveButton("確定", (dialog, which) -> {
      String username = input.getText().toString().trim();
      if (!username.isEmpty()) {
        fetchGithub(username);
      }
    });

    builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
    builder.show();
  }

  private void fetchGithub(String username) {
    new Thread(() -> {
      try {
        URL url = new URL("https://api.github.com/users/" + username + "/repos");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
          jsonBuilder.append(line);
        }
        in.close();

        JSONArray repos = new JSONArray(jsonBuilder.toString());
        for (int i = 0; i < repos.length(); i++) {
          JSONObject repo = repos.getJSONObject(i);
          String repoName = repo.getString("name");
          ContentValues values = new ContentValues();

          SharedPreferences prefs = requireContext().getSharedPreferences("FCUPrefs", MODE_PRIVATE);
          int user_id = prefs.getInt("user_id", 0);
          values.put("name", repoName);
          values.put("summary", " "); // 或可設定為空字串 ""
          values.put("manager_id", user_id); // 可改成目前登入使用者 ID

          db.insert("Projects", null, values);
        }

        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "導入成功", Toast.LENGTH_SHORT).show());

      } catch (Exception e) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), "發生錯誤：" + e.getMessage(), Toast.LENGTH_LONG).show());
      }
    }).start();
  }






}