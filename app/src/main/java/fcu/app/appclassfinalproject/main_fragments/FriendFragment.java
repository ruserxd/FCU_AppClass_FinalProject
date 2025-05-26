package fcu.app.appclassfinalproject.main_fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.adapter.FriendAdapter;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass. Use the {@link FriendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FriendFragment extends Fragment {

  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  private static final String TAG = "FriendFragment";

  private RecyclerView recyclerView;
  private List<User> friendList;
  private FriendAdapter adapter;
  private String mParam1;
  private String mParam2;

  public FriendFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of this fragment using the provided
   * parameters.
   *
   * @param param1 Parameter 1.
   * @param param2 Parameter 2.
   * @return A new instance of fragment friend.
   */
  public static FriendFragment newInstance(String param1, String param2) {
    FriendFragment fragment = new FriendFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_friend, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // 初始化 RecyclerView
    recyclerView = view.findViewById(R.id.rcy_friends);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    friendList = new ArrayList<>();

    // 獲取當前用戶的 Firebase UID
    SharedPreferences prefs = requireContext().getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    String currentUserUid = prefs.getString("uid", "");

    Log.d(TAG, "當前用戶 UID: " + currentUserUid);

    if (currentUserUid.isEmpty()) {
      Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show();
      // 顯示測試數據
      loadTestData();
    } else {
      // 載入真實的朋友列表
      loadFriendsList(currentUserUid);
    }

    // 配置 Adapter
    adapter = new FriendAdapter(getContext(), friendList);
    adapter.setFriendFragment(this); // 設置 Fragment 引用
    recyclerView.setAdapter(adapter);
  }

  private void loadFriendsList(String currentUserUid) {
    SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(requireContext());
    SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase();
    Cursor cursor = null;

    try {
      Log.d(TAG, "開始查詢朋友列表...");

      // 首先根據 firebase_uid 找到當前用戶的數據庫 ID
      Cursor userCursor = db.rawQuery("SELECT id FROM Users WHERE firebase_uid = ?",
          new String[]{currentUserUid});

      int currentUserId = -1;
      if (userCursor.moveToFirst()) {
        currentUserId = userCursor.getInt(0);
        userCursor.close();
        Log.d(TAG, "找到當前用戶 ID: " + currentUserId);
      } else {
        Log.e(TAG, "找不到當前用戶的數據庫記錄");
        Toast.makeText(requireContext(), "用戶資料同步錯誤", Toast.LENGTH_SHORT).show();
        loadTestData();
        userCursor.close();
        return;
      }

      // 查詢朋友列表
      String query = "SELECT u.id, u.account, u.email, u.firebase_uid " +
          "FROM Users u " +
          "INNER JOIN Friends f ON u.id = f.friend_id " +
          "WHERE f.user_id = ?";

      cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});

      friendList.clear();

      if (cursor != null && cursor.moveToFirst()) {
        Log.d(TAG, "找到 " + cursor.getCount() + " 個朋友");

        do {
          int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
          String account = cursor.getString(cursor.getColumnIndexOrThrow("account"));
          String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));

          // 檢查是否有 firebase_uid 欄位
          int firebaseUidIndex = cursor.getColumnIndex("firebase_uid");
          String firebaseUid = firebaseUidIndex >= 0 ? cursor.getString(firebaseUidIndex) : "無";

          Log.d(TAG, "朋友 - ID: " + id + ", 帳號: " + account +
              ", 郵箱: " + email + ", Firebase UID: " + firebaseUid);

          User friend = new User(id, account, email);
          friendList.add(friend);

        } while (cursor.moveToNext());

        Toast.makeText(requireContext(), "找到 " + friendList.size() + " 個朋友",
            Toast.LENGTH_SHORT).show();
      } else {
        Log.d(TAG, "沒有找到朋友");
        Toast.makeText(requireContext(), "你還沒有添加任何朋友", Toast.LENGTH_SHORT).show();
        loadTestData(); // 如果沒有朋友，顯示測試數據用於調試
      }

    } catch (Exception e) {
      Log.e(TAG, "查詢朋友列表時發生錯誤: " + e.getMessage(), e);
      Toast.makeText(requireContext(), "載入朋友列表失敗: " + e.getMessage(),
          Toast.LENGTH_LONG).show();
      loadTestData();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
  }

  private void loadTestData() {
    Log.d(TAG, "載入測試數據");
    friendList.clear();
    friendList.add(new User(1, "測試朋友1", "friend1@example.com"));
    friendList.add(new User(2, "測試朋友2", "friend2@example.com"));
    friendList.add(new User(3, "測試朋友3", "friend3@example.com"));

    if (adapter != null) {
      adapter.notifyDataSetChanged();
    }
  }

  /**
   * 刪除好友
   */
  public void removeFriend(User friend, int position) {
    SharedPreferences prefs = requireContext().getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    String currentUserUid = prefs.getString("uid", "");

    if (currentUserUid.isEmpty()) {
      Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show();
      return;
    }

    SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(requireContext());
    SQLiteDatabase db = sqlDataBaseHelper.getWritableDatabase();
    Cursor cursor = null;

    try {
      Log.d(TAG, "開始刪除好友: " + friend.getAccount());

      // 首先根據 firebase_uid 找到當前用戶的數據庫 ID
      cursor = db.rawQuery("SELECT id FROM Users WHERE firebase_uid = ?",
          new String[]{currentUserUid});

      int currentUserId = -1;
      if (cursor != null && cursor.moveToFirst()) {
        currentUserId = cursor.getInt(0);
        cursor.close();
        cursor = null;
      } else {
        Toast.makeText(requireContext(), "找不到當前用戶資料", Toast.LENGTH_SHORT).show();
        return;
      }

      // 刪除雙向好友關係
      int deletedRows1 = db.delete("Friends",
          "user_id = ? AND friend_id = ?",
          new String[]{String.valueOf(currentUserId), String.valueOf(friend.getID())});

      int deletedRows2 = db.delete("Friends",
          "user_id = ? AND friend_id = ?",
          new String[]{String.valueOf(friend.getID()), String.valueOf(currentUserId)});

      if (deletedRows1 > 0 || deletedRows2 > 0) {
        Log.d(TAG, "成功刪除好友關係 - 刪除記錄數: " + (deletedRows1 + deletedRows2));
        Toast.makeText(requireContext(), "已刪除好友: " + friend.getAccount(),
            Toast.LENGTH_SHORT).show();

        // 從列表中移除並更新 UI
        friendList.remove(position);
        if (adapter != null) {
          adapter.notifyItemRemoved(position);
          adapter.notifyItemRangeChanged(position, friendList.size());
        }
      } else {
        Log.w(TAG, "沒有找到要刪除的好友關係");
        Toast.makeText(requireContext(), "刪除失敗：找不到好友關係", Toast.LENGTH_SHORT).show();
      }

    } catch (Exception e) {
      Log.e(TAG, "刪除好友時發生錯誤: " + e.getMessage(), e);
      Toast.makeText(requireContext(), "刪除好友失敗: " + e.getMessage(),
          Toast.LENGTH_SHORT).show();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
  }
}