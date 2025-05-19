package fcu.app.appclassfinalproject.dataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class SqlDataBaseHelper extends SQLiteOpenHelper {

  private static final String DataBaseName = "FCU_FinalProjectDataBase";
  private static final int DataBaseVersion = 7;

  public SqlDataBaseHelper(@Nullable Context context) {
    super(context, DataBaseName, null, DataBaseVersion);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    String createUsersTable = "CREATE TABLE IF NOT EXISTS Users (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "account TEXT NOT NULL UNIQUE," +
        "password TEXT NOT NULL," +
        "email TEXT NOT NULL UNIQUE" +
        ")";
    sqLiteDatabase.execSQL(createUsersTable);

    // Projects 表
    String createProjectsTable = "CREATE TABLE IF NOT EXISTS Projects (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "name TEXT NOT NULL," +
        "summary TEXT NOT NULL," +
        "manager_id INTEGER NOT NULL," +
        "FOREIGN KEY(manager_id) REFERENCES Users(id) ON DELETE CASCADE" +
        ")";
    sqLiteDatabase.execSQL(createProjectsTable);

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

    // UserProject 表（多對多）
    String createUserProjectTable = "CREATE TABLE IF NOT EXISTS UserProject (" +
        "user_id INTEGER NOT NULL," +
        "project_id INTEGER NOT NULL," +
        "PRIMARY KEY(user_id, project_id)," +
        "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE," +
        "FOREIGN KEY(project_id) REFERENCES Projects(id) ON DELETE CASCADE" +
        ")";
    sqLiteDatabase.execSQL(createUserProjectTable);

    // UserIssue 表（多對多）
    String createUserIssueTable = "CREATE TABLE IF NOT EXISTS UserIssue (" +
        "user_id INTEGER NOT NULL," +
        "issue_id INTEGER NOT NULL," +
        "PRIMARY KEY(user_id, issue_id)," +
        "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE," +
        "FOREIGN KEY(issue_id) REFERENCES Issues(id) ON DELETE CASCADE" +
        ")";
    sqLiteDatabase.execSQL(createUserIssueTable);

    // UserFriends
    String createFriends = "CREATE TABLE IF NOT EXISTS Friends (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "user_id INTEGER NOT NULL," +
        "friend_id INTEGER NOT NULL," +
        "UNIQUE(user_id, friend_id)" +
        ")";
    sqLiteDatabase.execSQL(createFriends);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS UserIssue");
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS UserProject");
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Issues");
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Projects");
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Users");
    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Friends");
    onCreate(sqLiteDatabase);
  }

}