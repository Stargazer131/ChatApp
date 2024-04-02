package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.User;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecentChatRecyclerAdapter
        extends FirestoreRecyclerAdapter<ChatRoom, RecentChatRecyclerAdapter.RecentChatViewHolder> {
    private Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatRoom> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecentChatViewHolder holder, int position, @NonNull ChatRoom model) {
        if (model.getLastMessageSenderId().equals("")) {
            holder.parent.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

        FirebaseUtility.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtility.getCurrentUserId());
                        User otherUser = task.getResult().toObject(User.class);

                        AndroidUtility.changeAvatarProfileColor(otherUser.getStatus(), holder.profilePic, context);
                        setOtherUserAvatar(otherUser.getUserId(), holder);
                        if (lastMessageSentByMe) {
                            holder.lastMessageText.setText(String.format("You : %s", model.getLastMessage()));
                        } else {
                            holder.lastMessageText.setText(
                                    String.format("%s : %s", otherUser.getUsername(), model.getLastMessage())
                            );
                        }

                        holder.usernameText.setText(otherUser.getUsername());
                        holder.lastMessageTime.setText(FirebaseUtility.timestampToCustomString(model.getLastMessageTimestamp()));
                        holder.itemView.setOnClickListener(v -> openChatActivity(otherUser, model.getChatroomId()));
                    }
                });

        if(!holder.listenerAttached) {
            FirebaseUtility.getOtherUserFromChatroom(model.getUserIds())
                    .addSnapshotListener((value, error) -> {
                        String tag = "RECENT_CHAT_USER_DOCUMENT_LISTENER";
                        if (error != null) {
                            Log.w(tag, "Listen failed.", error);
                            return;
                        }

                        if (value != null && value.exists()) {
                            Log.d(tag, "Current data: has changed");
                            String status = value.getString("status");
                            if(status != null) {
                                AndroidUtility.changeAvatarProfileColor(status, holder.profilePic, context);
                            }
                        } else {
                            Log.d(tag, "Current data: null");
                        }
                    });
            holder.listenerAttached = true;
        }
    }

    private void openChatActivity(User otherUser, String chatRoomId) {
        //navigate to chat activity
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("otherUserId", otherUser.getUserId());
        intent.putExtra("chatRoomId", chatRoomId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void setOtherUserAvatar(String userId, @NonNull RecentChatViewHolder holder) {
        FirebaseUtility.getProfilePictureByUserId(userId).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtility.setProfilePicture(context, uri, holder.profilePic);
                    }
                });
    }

    @NonNull
    @Override
    public RecentChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new RecentChatViewHolder(view);
    }

    class RecentChatViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;
        LinearLayout parent;
        boolean listenerAttached = false;

        public RecentChatViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.text_username);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_picture_image_view);
            parent = itemView.findViewById(R.id.recent_chat_row_layout);
        }
    }
}