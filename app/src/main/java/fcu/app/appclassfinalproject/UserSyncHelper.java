package fcu.app.appclassfinalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import fcu.app.appclassfinalproject.helper.SqlDataBaseHelper;

public class UserSyncHelper {

  private static final String TAG = "UserSyncHelper";

  /**
   * 將 Firebase 用戶與本地數據庫用戶關聯
   *
   * @param context     上下文
   * @param firebaseUid Firebase UID
   * @param email       用戶電子郵件
   * @param account     用戶帳號（可選）
   * @return 本地數據庫中的用戶 ID，如果失敗返回 -1
   */
  public static int syncFirebaseUserWithLocalDB(Context context, String firebaseUid, String email,
      String account) {
    SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(context);

    try (SQLiteDatabase db = sqlDataBaseHelper.getWritableDatabase()) {
      // 首先檢查是否已經存在這個 firebase_uid 的用戶
      Cursor cursor = db.rawQuery("SELECT id FROM Users WHERE firebase_uid = ?",
          new String[]{firebaseUid});

      if (cursor.moveToFirst()) {
        int userId = cursor.getInt(0);
        cursor.close();
        Log.i(TAG, "找到現有用戶，ID: " + userId);
        return userId;
      }

      cursor.close();

      // 檢查是否有相同 email 的用戶（可能是舊數據）
      cursor = db.rawQuery("SELECT id FROM Users WHERE email = ?",
          new String[]{email});

      if (cursor.moveToFirst()) {
        // 更新現有用戶的 firebase_uid
        int userId = cursor.getInt(0);
        cursor.close();

        ContentValues updateValues = new ContentValues();
        updateValues.put("firebase_uid", firebaseUid);

        int rowsUpdated = db.update("Users", updateValues, "id = ?",
            new String[]{String.valueOf(userId)});

        if (rowsUpdated > 0) {
          Log.i(TAG, "更新現有用戶的 Firebase UID，用戶 ID: " + userId);
          return userId;
        }
      }

      cursor.close();

      // 創建新用戶
      ContentValues values = new ContentValues();
      values.put("account", account != null ? account : email.split("@")[0]);
      values.put("email", email);
      values.put("firebase_uid", firebaseUid);

      long newUserId = db.insert("Users", null, values);

      if (newUserId != -1) {
        Log.i(TAG, "創建新用戶，ID: " + newUserId);
        return (int) newUserId;
      } else {
        Log.e(TAG, "創建用戶失敗");
        return -1;
      }

    } catch (Exception e) {
      Log.e(TAG, "同步用戶時發生錯誤: " + e.getMessage(), e);
      return -1;
    }
  }
}