package fcu.app.appclassfinalproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.model.User;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

  private List<User> userList;
  private Context context;

  public FriendAdapter(Context context, List<User> list) {
    this.context = context;
    this.userList = list;
  }


  @NonNull
  @Override
  public FriendAdapter.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
      int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_friend, parent, false);
    return new FriendAdapter.FriendViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull FriendAdapter.FriendViewHolder holder, int position) {
    User user = userList.get(position);
    holder.textFriendID.setText("ID: " + user.getID());
    holder.textFriendAccount.setText("帳號: " + user.getAccount());
    holder.textFriendEmail.setText("Email: " + user.getEmail());
  }

  @Override
  public int getItemCount() {
    return userList.size();
  }

  public static class FriendViewHolder extends RecyclerView.ViewHolder {

    TextView textFriendID, textFriendAccount, textFriendEmail;

    public FriendViewHolder(@NonNull View itemView) {
      super(itemView);
      textFriendID = itemView.findViewById(R.id.tv_friend_ID);
      textFriendAccount = itemView.findViewById(R.id.tv_friend_account);
      textFriendEmail = itemView.findViewById(R.id.tv_friend_mail);
    }
  }
}
