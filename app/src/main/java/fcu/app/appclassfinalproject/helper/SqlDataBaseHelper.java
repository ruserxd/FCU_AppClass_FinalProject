package fcu.app.appclassfinalproject.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;

public class SqlDataBaseHelper extends SQLiteOpenHelper {

  private static final String TAG = "SqlDataBaseHelper";
  private static final String DataBaseName = "FCU_FinalProjectDataBase";
  private static final int DataBaseVersion = 15;

  public SqlDataBaseHelper(@Nullable Context context) {
    super(context, DataBaseName, null, DataBaseVersion);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    Log.d(TAG, "Creating database version " + DataBaseVersion);

    // Users 表
    String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "account TEXT NOT NULL UNIQUE," +
        "email TEXT NOT NULL UNIQUE," +
        "firebase_uid TEXT UNIQUE" +
        ")";
    sqLiteDatabase.execSQL(createUsersTable);
    Log.d(TAG, "Users table created");

    // Projects 表 - 簡化結構，無 manager_id
    String createProjectsTable = "CREATE TABLE IF NOT EXISTS Projects (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "name TEXT NOT NULL," +
        "summary TEXT NOT NULL" +
        ")";
    sqLiteDatabase.execSQL(createProjectsTable);
    Log.d(TAG, "Projects table created");

    // Issues 表
    String createIssuesTable = "CREATE TABLE IF NOT EXISTS Issues (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "name TEXT NOT NULL," +
        "summary TEXT NOT NULL," +
        "start_time TEXT NOT NULL," +
        "end_time TEXT NOT NULL," +
        "status TEXT NOT NULL," +
        "designee TEXT NOT NULL," +
        "project_id INTEGER NOT NULL," +
        "FOREIGN KEY(project_id) REFERENCES Projects(id) ON DELETE CASCADE" +
        ")";
    sqLiteDatabase.execSQL(createIssuesTable);
    Log.d(TAG, "Issues table created");

    // UserProject 表（多對多）
    String createUserProjectTable = "CREATE TABLE IF NOT EXISTS UserProject (" +
        "user_id INTEGER NOT NULL," +
        "project_id INTEGER NOT NULL," +
        "PRIMARY KEY(user_id, project_id)," +
        "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE," +
        "FOREIGN KEY(project_id) REFERENCES Projects(id) ON DELETE CASCADE" +
        ")";
    sqLiteDatabase.execSQL(createUserProjectTable);
    Log.d(TAG, "UserProject table created");

    // UserIssue 表（多對多）
    String createUserIssueTable = "CREATE TABLE IF NOT EXISTS UserIssue (" +
        "user_id INTEGER NOT NULL," +
        "issue_id INTEGER NOT NULL," +
        "PRIMARY KEY(user_id, issue_id)," +
        "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE," +
        "FOREIGN KEY(issue_id) REFERENCES Issues(id) ON DELETE CASCADE" +
        ")";
    sqLiteDatabase.execSQL(createUserIssueTable);
    Log.d(TAG, "UserIssue table created");

    // Friends 表
    String createFriends = "CREATE TABLE IF NOT EXISTS Friends (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "user_id INTEGER NOT NULL," +
        "friend_id INTEGER NOT NULL," +
        "UNIQUE(user_id, friend_id)," +
        "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE," +
        "FOREIGN KEY(friend_id) REFERENCES Users(id) ON DELETE CASCADE" +
        ")";
    sqLiteDatabase.execSQL(createFriends);
    Log.d(TAG, "Friends table created");

    Log.d(TAG, "Database creation completed successfully");
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
    Log.w(TAG, "All existing data will be lost during upgrade");

    // 捨棄所有舊資料，重新建立資料庫
    dropAllTables(sqLiteDatabase);
    onCreate(sqLiteDatabase);

    Log.d(TAG, "Database upgrade completed");
  }

  @Override
  public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
    Log.d(TAG, "Downgrading database from version " + oldVersion + " to " + newVersion);
    Log.w(TAG, "All existing data will be lost during downgrade");

    // 捨棄所有舊資料，重新建立資料庫
    dropAllTables(sqLiteDatabase);
    onCreate(sqLiteDatabase);

    Log.d(TAG, "Database downgrade completed");
  }

  /**
   * 刪除所有表格
   */
  private void dropAllTables(SQLiteDatabase sqLiteDatabase) {
    Log.d(TAG, "Dropping all tables");

    // 禁用外鍵約束
    sqLiteDatabase.execSQL("PRAGMA foreign_keys=OFF");

    try {
      // 刪除表格
      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS UserIssue");
      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS UserProject");
      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Friends");
      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Issues");
      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Projects");
      sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Users");

      Log.d(TAG, "All tables dropped successfully");
    } catch (Exception e) {
      Log.e(TAG, "Error dropping tables: " + e.getMessage());
    } finally {
      // 重新啟用外鍵約束
      sqLiteDatabase.execSQL("PRAGMA foreign_keys=ON");
    }
  }

}