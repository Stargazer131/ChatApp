package com.example.chatapp.adapter;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.R;
import com.example.chatapp.activity.UserProfileActivity;
import com.example.chatapp.model.User;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SearchUserRecyclerAdapter extends
        FirestoreRecyclerAdapter<User, SearchUserRecyclerAdapter.SearchUserViewHolder> {

    private Context context;

    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<User> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull SearchUserViewHolder holder, int position, @NonNull User model) {
        holder.usernameText.setText(model.getUsername());
        holder.emailText.setText(model.getEmail());
        if (model.getUserId().equals(FirebaseUtil.getCurrentUserId())) {
            holder.usernameText.setText(String.format("%s (Me)", model.getUsername()));
            holder.itemView.setEnabled(false);
        }

        FirebaseUtil.getProfilePictureByUserId(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri uri = task.getResult();
                            AndroidUtil.setProfilePicture(context, uri, holder.profilePicture);
                        }
                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //navigate to user profile activity
                Intent intent = new Intent(context, UserProfileActivity.class);
                intent.putExtra("userId", model.getUserId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public SearchUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false);
        return new SearchUserViewHolder(view);
    }

    class SearchUserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView emailText;
        ImageView profilePicture;

        public SearchUserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.text_username);
            emailText = itemView.findViewById(R.id.text_email);
            profilePicture = itemView.findViewById(R.id.profile_picture_image_view);
        }
    }
}