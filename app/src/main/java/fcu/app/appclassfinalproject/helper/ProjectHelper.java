package fcu.app.appclassfinalproject.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import fcu.app.appclassfinalproject.model.Project;
import java.util.ArrayList;
import java.util.List;

public class ProjectHelper {

  /**
   * 根據專案ID獲取所有成員的ID列表
   */
  public static List<Integer> getProjectMemberIds(SQLiteDatabase db, int projectId) {
    List<Integer> memberIds = new ArrayList<>();
    String query = "SELECT user_id FROM UserProject WHERE project_id = ?";
    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});

    while (cursor.moveToNext()) {
      memberIds.add(cursor.getInt(0));
    }
    cursor.close();
    return memberIds;
  }

  /**
   * 根據專案ID獲取所有成員的帳號名稱列表
   */
  public static List<String> getProjectMemberNames(SQLiteDatabase db, int projectId) {
    List<String> memberNames = new ArrayList<>();
    String query = "SELECT u.account FROM Users u " +
        "INNER JOIN UserProject up ON u.id = up.user_id " +
        "WHERE up.project_id = ? ORDER BY u.account";
    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});

    while (cursor.moveToNext()) {
      memberNames.add(cursor.getString(0));
    }
    cursor.close();
    return memberNames;
  }

  /**
   * 根據用戶ID獲取該用戶參與的所有專案
   */
  public static List<Project> getProjectsByUser(SQLiteDatabase db, int userId) {
    List<Project> projects = new ArrayList<>();
    String query = "SELECT p.id, p.name, p.summary FROM Projects p " +
        "INNER JOIN UserProject up ON p.id = up.project_id " +
        "WHERE up.user_id = ? ORDER BY p.name";
    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

    while (cursor.moveToNext()) {
      int projectId = cursor.getInt(0);
      String name = cursor.getString(1);
      String summary = cursor.getString(2);

      List<Integer> memberIds = getProjectMemberIds(db, projectId);
      List<String> memberNames = getProjectMemberNames(db, projectId);

      Project project = new Project(projectId, name, summary, memberIds, memberNames);
      projects.add(project);
    }
    cursor.close();
    return projects;
  }

  /**
   * 檢查用戶是否為指定專案的成員
   */
  public static boolean isUserProjectMember(SQLiteDatabase db, int userId, int projectId) {
    String query = "SELECT COUNT(*) FROM UserProject WHERE user_id = ? AND project_id = ?";
    Cursor cursor = db.rawQuery(query,
        new String[]{String.valueOf(userId), String.valueOf(projectId)});

    boolean isMember = false;
    if (cursor.moveToFirst()) {
      isMember = cursor.getInt(0) > 0;
    }
    cursor.close();
    return isMember;
  }


  /**
   * 獲取專案成員數量
   */
  public static int getProjectMemberCount(SQLiteDatabase db, int projectId) {
    String query = "SELECT COUNT(*) FROM UserProject WHERE project_id = ?";
    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});

    int count = 0;
    if (cursor.moveToFirst()) {
      count = cursor.getInt(0);
    }
    cursor.close();
    return count;
  }
}