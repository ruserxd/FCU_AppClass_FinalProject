package fcu.app.appclassfinalproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.main_fragments.AddIssueFragment;
import fcu.app.appclassfinalproject.main_fragments.ProjectInfoFragment;
import fcu.app.appclassfinalproject.main_fragments.SettingsFragment;

public class ProjectActivity extends AppCompatActivity {

  private static final String TAG = "ProjectActivity";
  private int currentProjectId = -1;
  private String currentProjectName = "";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_project);
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_project);
    Fragment projectInfoFragment = ProjectInfoFragment.newInstance("", "");
    Fragment settingsFragment = SettingsFragment.newInstance("", "");
    Fragment addIssueFragment = AddIssueFragment.newInstance("", "");
    setCurrentFragment(projectInfoFragment);

    loadCurrentProjectInfo();

    bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_project_issues) {
          setCurrentFragment(projectInfoFragment);
        } else if (item.getItemId() == R.id.menu_projct_back) {
          intentTo(HomeActivity.class);
        } else if (item.getItemId() == R.id.menu_project_add) {
          setCurrentFragment(addIssueFragment);
        } else if (item.getItemId() == R.id.menu_project_setting) {
          setCurrentFragment(settingsFragment);
        } else if (item.getItemId() == R.id.menu_project_delete) {
          showDeleteProjectConfirmDialog();
        }
        return true;
      }
    });

  }

  /**
   * 載入當前專案資訊
   */
  private void loadCurrentProjectInfo() {
    SharedPreferences prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    currentProjectId = prefs.getInt("project_id", -1);

    if (currentProjectId == -1) {
      Log.e(TAG, "無法獲取專案 ID");
      Toast.makeText(this, getString(R.string.project_id_not_found), Toast.LENGTH_SHORT).show();
      return;
    }

    // 查詢專案名稱
    SqlDataBaseHelper dbHelper = new SqlDataBaseHelper(this);

    try (SQLiteDatabase db = dbHelper.getReadableDatabase(); Cursor cursor = db.rawQuery(
        "SELECT name FROM Projects WHERE id = ?",
        new String[]{String.valueOf(currentProjectId)})) {

      if (cursor.moveToFirst()) {
        currentProjectName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        Log.d(TAG, "當前專案: " + currentProjectName + " (ID: " + currentProjectId + ")");
      } else {
        currentProjectName = "未知專案";
        Log.w(TAG, "找不到專案 ID: " + currentProjectId);
      }
    } catch (Exception e) {
      Log.e(TAG, "查詢專案資訊時發生錯誤: " + e.getMessage());
      currentProjectName = "未知專案";
    }
  }

  /**
   * 顯示刪除專案確認對話框
   */
  private void showDeleteProjectConfirmDialog() {
    if (currentProjectId == -1) {
      Toast.makeText(this, getString(R.string.project_id_not_found), Toast.LENGTH_SHORT).show();
      return;
    }

    // 先查詢專案中的 Issue 數量
    int issueCount = getProjectIssueCount();

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.delete_project_title));

    String message;
    if (issueCount > 0) {
      message = getString(R.string.delete_project_message_with_issues,
          currentProjectName, issueCount);
    } else {
      message = getString(R.string.delete_project_message, currentProjectName);
    }

    builder.setMessage(message);
    builder.setIcon(android.R.drawable.ic_dialog_alert);

    builder.setPositiveButton(getString(R.string.delete_project_confirm), (dialog, which) -> {
      deleteCurrentProject();
    });

    builder.setNegativeButton(getString(R.string.delete_project_cancel), (dialog, which) -> {
      dialog.dismiss();
    });

    AlertDialog dialog = builder.create();
    dialog.show();

    // 設定確定按鈕為紅色，表示危險操作
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
        getResources().getColor(android.R.color.holo_red_dark));
  }

  /**
   * 獲取專案中的 Issue 數量
   */
  private int getProjectIssueCount() {
    SqlDataBaseHelper dbHelper = new SqlDataBaseHelper(this);

    try (SQLiteDatabase db = dbHelper.getReadableDatabase(); Cursor cursor = db.rawQuery(
        "SELECT COUNT(*) FROM Issues WHERE project_id = ?",
        new String[]{String.valueOf(currentProjectId)})) {

      if (cursor.moveToFirst()) {
        return cursor.getInt(0);
      }
    } catch (Exception e) {
      Log.e(TAG, "查詢 Issue 數量時發生錯誤: " + e.getMessage());
    }

    return 0;
  }

  /**
   * 刪除當前專案
   */
  private void deleteCurrentProject() {
    if (currentProjectId == -1) {
      Toast.makeText(this, getString(R.string.project_id_not_found), Toast.LENGTH_SHORT).show();
      return;
    }

    SqlDataBaseHelper dbHelper = new SqlDataBaseHelper(this);
    SQLiteDatabase db = null;

    try {
      db = dbHelper.getWritableDatabase();
      db.beginTransaction();

      // 刪除專案相關的 UserIssue 關聯
      String deleteUserIssueQuery = "DELETE FROM UserIssue WHERE issue_id IN " +
          "(SELECT id FROM Issues WHERE project_id = ?)";
      db.execSQL(deleteUserIssueQuery, new String[]{String.valueOf(currentProjectId)});

      // 刪除專案的所有 Issues
      int deletedIssues = db.delete("Issues", "project_id = ?",
          new String[]{String.valueOf(currentProjectId)});

      // 刪除專案的 UserProject 關聯
      int deletedUserProjects = db.delete("UserProject", "project_id = ?",
          new String[]{String.valueOf(currentProjectId)});

      // 刪除專案本身
      int deletedProject = db.delete("Projects", "id = ?",
          new String[]{String.valueOf(currentProjectId)});

      db.setTransactionSuccessful();

      Log.d(TAG, String.format("刪除專案成功 - 專案: %d, Issues: %d, UserProject: %d",
          deletedProject, deletedIssues, deletedUserProjects));

      Toast.makeText(this, getString(R.string.delete_project_success, currentProjectName),
          Toast.LENGTH_SHORT).show();

      // 清除 SharedPreferences 中的專案 ID
      SharedPreferences prefs = getSharedPreferences("FCUPrefs", MODE_PRIVATE);
      SharedPreferences.Editor editor = prefs.edit();
      editor.remove("project_id");
      editor.apply();

      // 返回到主頁
      intentTo(HomeActivity.class);
      finish();

    } catch (Exception e) {
      Log.e(TAG, "刪除專案時發生錯誤: " + e.getMessage(), e);
      Toast.makeText(this, getString(R.string.delete_project_failed, e.getMessage()),
          Toast.LENGTH_LONG).show();
    } finally {
      if (db != null) {
        if (db.inTransaction()) {
          db.endTransaction();
        }
        db.close();
      }
    }
  }

  private void setCurrentFragment(Fragment fragment) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_main_project, fragment)
        .commit();
  }

  private void intentTo(Class<?> page) {
    Intent intent = new Intent();
    intent.setClass(ProjectActivity.this, page);
    startActivity(intent);
  }
}