package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activity.UserProfileActivity;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserRelationship;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;

public class FriendRecyclerAdapter extends
        FirestoreRecyclerAdapter<UserRelationship, FriendRecyclerAdapter.FriendViewHolder> {

    private Context context;
    public FriendRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserRelationship> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendViewHolder holder, int position, @NonNull UserRelationship model) {
        FirebaseUtility.getOtherUserFromUserRelationship(model.getUserIds()).get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                User user = task.getResult().toObject(User.class);

                                holder.usernameText.setText(user.getUsername());
                                holder.emailText.setText(user.getEmail());
                                AndroidUtility.changeAvatarProfileColor(
                                        user.getStatus(), holder.profilePicture, context
                                );
                                changeStatusText(
                                        holder.lastActiveText, user.getStatus(), user.getLastActive()
                                );

                                FirebaseUtility.getProfilePictureByUserId(user.getUserId()).getDownloadUrl()
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Uri uri = task1.getResult();
                                                AndroidUtility.setProfilePicture(context, uri, holder.profilePicture);
                                            }
                                        });

                                holder.itemView.setOnClickListener(v -> {
                                    //navigate to user profile activity
                                    Intent intent = new Intent(context, UserProfileActivity.class);
                                    intent.putExtra("userId", user.getUserId());
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                });

                                if(!holder.listenerAttached) {
                                    FirebaseUtility.getUserById(user.getUserId())
                                            .addSnapshotListener((value, error) -> {
                                                String tag = "FRIEND_USER_DOCUMENT_LISTENER";
                                                if (error != null) {
                                                    Log.w(tag, "Listen failed.", error);
                                                    return;
                                                }

                                                if (value != null && value.exists()) {
                                                    Log.d(tag, "Current data: has changed");
                                                    String status = value.getString("status");
                                                    Timestamp timestamp = value.getTimestamp("lastActive");
                                                    if(status != null) {
                                                        AndroidUtility.changeAvatarProfileColor(
                                                                status, holder.profilePicture, context)
                                                        ;
                                                        changeStatusText(
                                                                holder.lastActiveText, status, timestamp
                                                        );
                                                    }
                                                } else {
                                                    Log.d(tag, "Current data: null");
                                                }
                                            });
                                    holder.listenerAttached = true;
                                }
                            }
                        });
    }

    private void changeStatusText(TextView textView, String status, Timestamp timestamp) {
        if (status.equals("online")) {
            textView.setText("Online");
            textView.setTextColor(Color.GREEN);
        } else {
            textView.setText(
                    String.format("Last active: %s", FirebaseUtility.timestampToCustomString(timestamp))
            );
            textView.setTextColor(Color.DKGRAY);
        }
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_recycler_row, parent, false);
        return new FriendRecyclerAdapter.FriendViewHolder(view);
    }

    class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView emailText;
        TextView lastActiveText;
        ImageView profilePicture;
        boolean listenerAttached = false;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.text_username);
            emailText = itemView.findViewById(R.id.text_email);
            lastActiveText = itemView.findViewById(R.id.text_last_active);
            profilePicture = itemView.findViewById(R.id.profile_picture_image_view);
        }
    }
}
