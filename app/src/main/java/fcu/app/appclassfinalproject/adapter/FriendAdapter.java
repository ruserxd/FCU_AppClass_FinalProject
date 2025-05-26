package fcu.app.appclassfinalproject.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fcu.app.appclassfinalproject.main_fragments.FriendFragment;
import fcu.app.appclassfinalproject.model.User;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

  private Context context;
  private List<User> friendList;
  private FriendFragment friendFragment;
  private static final String TAG = "FriendAdapter";

  public FriendAdapter(Context context, List<User> friendList) {
    this.context = context;
    this.friendList = friendList;
    Log.d(TAG, "FriendAdapter 創建，朋友數量: " + friendList.size());
  }

  // 設置 Fragment 引用，用於回調刪除操作
  public void setFriendFragment(FriendFragment fragment) {
    this.friendFragment = fragment;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Log.d(TAG, "創建 ViewHolder");

    // 用程式碼創建布局
    LinearLayout layout = new LinearLayout(context);
    layout.setOrientation(LinearLayout.HORIZONTAL);
    layout.setPadding(32, 32, 32, 32);
    layout.setBackgroundColor(Color.parseColor("#E8F5E8"));

    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.setMargins(16, 8, 16, 8);
    layout.setLayoutParams(layoutParams);

    // 創建用戶信息的容器
    LinearLayout userInfoLayout = new LinearLayout(context);
    userInfoLayout.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams userInfoParams = new LinearLayout.LayoutParams(
        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
    userInfoLayout.setLayoutParams(userInfoParams);

    // 創建朋友名 TextView
    TextView tvFriendName = new TextView(context);
    tvFriendName.setId(View.generateViewId());
    tvFriendName.setTextSize(16);
    tvFriendName.setTextColor(Color.BLACK);
    tvFriendName.setText("朋友名稱");

    // 創建郵箱 TextView
    TextView tvFriendEmail = new TextView(context);
    tvFriendEmail.setId(View.generateViewId());
    tvFriendEmail.setTextSize(14);
    tvFriendEmail.setTextColor(Color.GRAY);
    tvFriendEmail.setText("friend@example.com");

    // 添加到用戶信息容器
    userInfoLayout.addView(tvFriendName);
    userInfoLayout.addView(tvFriendEmail);

    // 創建刪除按鈕
    Button btnDelete = new Button(context);
    btnDelete.setId(View.generateViewId());
    btnDelete.setText("刪除好友");
    btnDelete.setBackgroundColor(Color.parseColor("#F44336")); // 紅色按鈕
    btnDelete.setTextColor(Color.WHITE);
    btnDelete.setTextSize(14);
    LinearLayout.LayoutParams deleteButtonParams = new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    deleteButtonParams.setMargins(16, 0, 0, 0);
    btnDelete.setLayoutParams(deleteButtonParams);

    // 組裝布局
    layout.addView(userInfoLayout);
    layout.addView(btnDelete);

    return new ViewHolder(layout, tvFriendName, tvFriendEmail, btnDelete);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    User friend = friendList.get(position);

    Log.d(TAG, "綁定朋友數據 - 位置: " + position + ", 朋友: " + friend.getAccount());

    holder.tvFriendName.setText("👤 " + friend.getAccount());
    holder.tvFriendEmail.setText("📧 " + friend.getEmail());

    // 設置刪除按鈕點擊事件
    holder.btnDelete.setOnClickListener(v -> {
      Log.d(TAG, "準備刪除朋友: " + friend.getAccount());
      showDeleteConfirmDialog(friend, position);
    });
  }

  @Override
  public int getItemCount() {
    int count = friendList.size();
    Log.d(TAG, "getItemCount: " + count);
    return count;
  }

  /**
   * 顯示刪除確認對話框
   */
  private void showDeleteConfirmDialog(User friend, int position) {
    new AlertDialog.Builder(context)
        .setTitle("刪除好友")
        .setMessage("確定要刪除好友 \"" + friend.getAccount() + "\" 嗎？")
        .setPositiveButton("刪除", (dialog, which) -> {
          if (friendFragment != null) {
            friendFragment.removeFriend(friend, position);
          } else {
            friendList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, friendList.size());
            Toast.makeText(context, "已刪除好友: " + friend.getAccount(),
                Toast.LENGTH_SHORT).show();
          }
        })
        .setNegativeButton("取消", (dialog, which) -> {
          Log.d(TAG, "取消刪除好友: " + friend.getAccount());
        })
        .show();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvFriendName;
    TextView tvFriendEmail;
    Button btnDelete;

    public ViewHolder(@NonNull View itemView, TextView tvFriendName, TextView tvFriendEmail,
        Button btnDelete) {
      super(itemView);
      this.tvFriendName = tvFriendName;
      this.tvFriendEmail = tvFriendEmail;
      this.btnDelete = btnDelete;

      Log.d("FriendAdapter", "ViewHolder 創建完成");
    }
  }

  /**
   * 更新朋友列表
   */
  public void updateFriendList(List<User> newFriendList) {
    this.friendList = newFriendList;
    notifyDataSetChanged();
    Log.d(TAG, "朋友列表已更新，新數量: " + newFriendList.size());
  }
}