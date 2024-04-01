package com.example.chatapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.model.FriendRequest;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserRelationship;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;


public class FriendRequestRecyclerAdapter extends
        FirestoreRecyclerAdapter<FriendRequest, FriendRequestRecyclerAdapter.FriendRequestViewHolder> {

    private Context context;
    public FriendRequestRecyclerAdapter(@NonNull FirestoreRecyclerOptions<FriendRequest> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position, @NonNull FriendRequest model) {
        String otherUserId = model.getFromUserId();
        setOtherUserAvatar(otherUserId, holder);
        FirebaseUtility.getUserById(otherUserId).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        User otherUser = task.getResult().toObject(User.class);
                        holder.usernameText.setText(otherUser.getUsername());
                    }
                });

        holder.yesBtn.setOnClickListener(v -> acceptRequest(model));

        holder.noBtn.setOnClickListener(v -> refuseRequest(model));
    }

    private void acceptRequest(FriendRequest model) {
        String id = model.getId();
        UserRelationship userRelationship = new UserRelationship(
                id, Arrays.asList(model.getFromUserId(), model.getToUserId()), "friend"
        );

        FirebaseUtility.getFriendRequestById(id).delete()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        FirebaseUtility.getUserRelationshipById(id).set(userRelationship)
                                .addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()) {
                                        AndroidUtility.showToast(context, "Accept friend request");
                                    }
                                });
                    }
                });
    }

    private void refuseRequest(FriendRequest model) {
        String id = model.getId();
        FirebaseUtility.getFriendRequestById(id).delete()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        AndroidUtility.showToast(context, "Refuse friend request");
                    }
                });
    }

    private void setOtherUserAvatar(String userId, @NonNull FriendRequestViewHolder holder) {
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
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_request_recycler_row, parent, false);
        return new FriendRequestRecyclerAdapter.FriendRequestViewHolder(view);
    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        ImageView profilePic;
        ImageView yesBtn, noBtn;
        LinearLayout parent;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.text_username);
            profilePic = itemView.findViewById(R.id.profile_picture_image_view);
            parent = itemView.findViewById(R.id.recent_chat_row_layout);
            yesBtn = itemView.findViewById(R.id.yes_btn);
            noBtn = itemView.findViewById(R.id.no_btn);
        }
    }
}
