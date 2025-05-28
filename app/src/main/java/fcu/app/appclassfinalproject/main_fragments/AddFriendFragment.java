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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.adapter.AddfriendAdapter;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.User;
import java.util.ArrayList;
import java.util.List;

public class AddFriendFragment extends Fragment {

  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";

  private String mParam1;
  private String mParam2;
  private RecyclerView addFriendList;
  private List<User> availableUsersList;
  private AddfriendAdapter addfriendAdapter;

  public AddFriendFragment() {
    // Required empty public constructor
  }

  public static AddFriendFragment newInstance(String param1, String param2) {
    AddFriendFragment fragment = new AddFriendFragment();
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
    View view = inflater.inflate(R.layout.fragment_add_friend, container, false);

    // 初始化 RecyclerView
    addFriendList = view.findViewById(R.id.rcy_friend_list);
    availableUsersList = new ArrayList<>();
    addFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

    // 獲取當前用戶的 Firebase UID
    SharedPreferences prefs = requireContext().getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    String currentUserUid = prefs.getString("uid", "");

    Log.d("AddFriendFragment", "從 SharedPreferences 獲取的 UID: " + currentUserUid);

    if (currentUserUid.isEmpty()) {
      Toast.makeText(requireContext(), "請先登入", Toast.LENGTH_SHORT).show();
      return view;
    }

    // 載入可加入的用戶列表
    loadAvailableUsers(currentUserUid);

    return view;
  }

  private void loadAvailableUsers(String currentUserUid) {
    SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(requireContext());
    SQLiteDatabase db = sqlDataBaseHelper.getReadableDatabase();
    Cursor cursor = null;

    try {
      // 首先根據 firebase_uid 找到當前用戶的數據庫 ID
      Cursor userCursor = db.rawQuery("SELECT id FROM Users WHERE firebase_uid = ?",
          new String[]{currentUserUid});

      int currentUserId = -1;
      if (userCursor.moveToFirst()) {
        currentUserId = userCursor.getInt(0);
        userCursor.close();
        Log.d("AddFriendFragment", "找到當前用戶 ID: " + currentUserId);
      } else {
        Log.e("AddFriendFragment", "找不到當前用戶的數據庫記錄");
        Toast.makeText(requireContext(), "用戶資料同步錯誤，請重新登入", Toast.LENGTH_SHORT).show();
        userCursor.close();
        return;
      }

      // 查詢所有用戶，排除當前用戶和已經是好友的用戶
      String query = "SELECT u.id, u.account, u.email " +
          "FROM Users u " +
          "WHERE u.id != ? " +
          "AND u.id NOT IN (" +
          "  SELECT f.friend_id FROM Friends f WHERE f.user_id = ? " +
          "  UNION " +
          "  SELECT f.user_id FROM Friends f WHERE f.friend_id = ?" +
          ")";

      cursor = db.rawQuery(query, new String[]{
          String.valueOf(currentUserId),
          String.valueOf(currentUserId),
          String.valueOf(currentUserId)
      });

      availableUsersList.clear();

      if (cursor.moveToFirst()) {
        do {
          int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
          String account = cursor.getString(cursor.getColumnIndexOrThrow("account"));
          String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));

          User user = new User(id, account, email);
          availableUsersList.add(user);

        } while (cursor.moveToNext());

        Log.i("AddFriendFragment", "找到 " + availableUsersList.size() + " 個可加入的用戶");
      } else {
        Log.i("AddFriendFragment", "沒有找到可加入的用戶");
        Log.d("AddFriendFragment", "添加了測試數據");
      }

    } catch (Exception e) {
      Log.e("AddFriendFragment", "載入用戶列表時發生錯誤: " + e.getMessage(), e);
      Toast.makeText(requireContext(), "載入用戶列表失敗: " + e.getMessage(), Toast.LENGTH_SHORT)
          .show();
      availableUsersList.clear();
      Log.d("AddFriendFragment", "添加了測試數據用於調試");

    } finally {
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }

    // 設置 Adapter
    Log.d("AddFriendFragment", "設置 Adapter，項目數量: " + availableUsersList.size());
    addfriendAdapter = new AddfriendAdapter(getContext(), availableUsersList);
    addFriendList.setAdapter(addfriendAdapter);
  }
}