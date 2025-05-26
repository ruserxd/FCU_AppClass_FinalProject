package fcu.app.appclassfinalproject.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.dataBase.SqlDataBaseHelper;
import fcu.app.appclassfinalproject.model.User;
import java.util.List;

public class AddfriendAdapter extends RecyclerView.Adapter<AddfriendAdapter.ViewHolder> {

  private Context context;
  private List<User> userList;

  public AddfriendAdapter(Context context, List<User> userList) {
    this.context = context;
    this.userList = userList;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.item_addfriend, parent, false);
    Log.d("AddfriendAdapter", "創建 ViewHolder");
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    User user = userList.get(position);

    Log.d("AddfriendAdapter", "綁定數據 - 位置: " + position + ", 用戶: " + user.getAccount());

    holder.tvUserAccount.setText(user.getAccount());
    holder.tvUserEmail.setText(user.getEmail());

    // 確保按鈕可見和可用
    holder.btnAddFriend.setVisibility(View.VISIBLE);
    holder.btnAddFriend.setEnabled(true);

    Log.d("AddfriendAdapter", "按鈕狀態 - 可見性: " + holder.btnAddFriend.getVisibility() +
        ", 是否啟用: " + holder.btnAddFriend.isEnabled());

    holder.btnAddFriend.setOnClickListener(v -> {
      Log.d("AddfriendAdapter", "按鈕被點擊: " + user.getAccount());
      addFriend(user.getID(), position);
    });
  }

  @Override
  public int getItemCount() {
    return userList.size();
  }

  private void addFriend(int friendId, int position) {
    SharedPreferences prefs = context.getSharedPreferences("FCUPrefs", MODE_PRIVATE);
    int currentUserId = prefs.getInt("user_id", 0);

    if (currentUserId == 0) {
      Toast.makeText(context, "請先登入", Toast.LENGTH_SHORT).show();
      return;
    }

    SqlDataBaseHelper sqlDataBaseHelper = new SqlDataBaseHelper(context);
    SQLiteDatabase db = sqlDataBaseHelper.getWritableDatabase();

    try {
      // 添加雙向好友關係
      ContentValues values1 = new ContentValues();
      values1.put("user_id", currentUserId);
      values1.put("friend_id", friendId);

      ContentValues values2 = new ContentValues();
      values2.put("user_id", friendId);
      values2.put("friend_id", currentUserId);

      long result1 = db.insert("Friends", null, values1);
      long result2 = db.insert("Friends", null, values2);

      if (result1 != -1 && result2 != -1) {
        Toast.makeText(context, "成功添加好友！", Toast.LENGTH_SHORT).show();

        // 從列表中移除已添加的用戶
        userList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, userList.size());

        Log.i("AddFriendAdapter", "成功添加好友 ID: " + friendId);
      } else {
        Toast.makeText(context, "添加好友失敗", Toast.LENGTH_SHORT).show();
        Log.e("AddFriendAdapter", "添加好友失敗");
      }

    } catch (Exception e) {
      Toast.makeText(context, "添加好友時發生錯誤: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    } finally {
      db.close();
    }
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    TextView tvUserAccount;
    TextView tvUserEmail;
    Button btnAddFriend;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      tvUserAccount = itemView.findViewById(R.id.tv_user_account);
      tvUserEmail = itemView.findViewById(R.id.tv_user_email);
      btnAddFriend = itemView.findViewById(R.id.btn_add_friend);

      Log.d("AddFriendAdapter", "ViewHolder 創建:");
      Log.d("AddFriendAdapter", "  tvUserAccount: " + (tvUserAccount != null ? "找到" : "未找到"));
      Log.d("AddFriendAdapter", "  tvUserEmail: " + (tvUserEmail != null ? "找到" : "未找到"));
      Log.d("AddFriendAdapter", "  btnAddFriend: " + (btnAddFriend != null ? "找到" : "未找到"));

      if (btnAddFriend != null) {
        btnAddFriend.setBackgroundColor(0xFF2196F3);
        btnAddFriend.setTextColor(0xFFFFFFFF);
        Log.d("AddFriendAdapter", "按鈕樣式已設置");
      }
    }
  }
}