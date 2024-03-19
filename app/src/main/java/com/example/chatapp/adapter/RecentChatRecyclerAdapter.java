package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.R;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.User;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class RecentChatRecyclerAdapter
        extends FirestoreRecyclerAdapter<ChatRoom, RecentChatRecyclerAdapter.RecentChatModelViewHolder> {
    private Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatRoom> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecentChatModelViewHolder holder, int position, @NonNull ChatRoom model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.getCurrentUserId());
                        User otherUser = task.getResult().toObject(User.class);

                        FirebaseUtil.getProfilePictureByUserId(otherUser.getUserId()).getDownloadUrl()
                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri uri = task.getResult();
                                            AndroidUtil.setProfilePicture(context, uri, holder.profilePic);
                                        }
                                    }
                                });

                            if (lastMessageSentByMe) {
                                holder.lastMessageText.setText(String.format("You : %s", model.getLastMessage()));
                            } else {
                                holder.lastMessageText.setText(
                                        String.format("%s : %s", otherUser.getUsername(),model.getLastMessage())
                                );
                            }

                            holder.usernameText.setText(otherUser.getUsername());
                            holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //navigate to chat activity
                                    Intent intent = new Intent(context, ChatActivity.class);

                                    intent.putExtra("otherUser", otherUser);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    context.startActivity(intent);
                                }
                            });
                        }
                    }
                });
    }

    @NonNull
    @Override
    public RecentChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false);
        return new RecentChatModelViewHolder(view);
    }

    class RecentChatModelViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;

        public RecentChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.text_username);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_picture_image_view);
        }
    }
}