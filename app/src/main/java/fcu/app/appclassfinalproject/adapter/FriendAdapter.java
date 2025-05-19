package fcu.app.appclassfinalproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fcu.app.appclassfinalproject.R;
import fcu.app.appclassfinalproject.model.Friend;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    private List<Friend> friendList;
    private Context context;

    public FriendAdapter(Context context, List<Friend> list){
        this.context = context;
        this.friendList = list;
    }


    @NonNull
    @Override
    public FriendAdapter.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friend, parent, false);
        return new FriendAdapter.FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.FriendViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        holder.textFriendID.setText("ID: " + friend.getID());
        holder.textFriendAccount.setText("帳號: " + friend.getAccount());
        holder.textFriendEmail.setText("Email: " + friend.getEmail());

    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {

        TextView textFriendID,textFriendAccount, textFriendEmail;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            textFriendID = itemView.findViewById(R.id.tv_friend_ID);
            textFriendAccount = itemView.findViewById(R.id.tv_friend_account);
            textFriendEmail = itemView.findViewById(R.id.tv_friend_mail);
        }
    }
}
