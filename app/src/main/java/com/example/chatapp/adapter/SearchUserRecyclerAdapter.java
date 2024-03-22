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

import com.example.chatapp.R;
import com.example.chatapp.activity.UserProfileActivity;
import com.example.chatapp.model.User;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

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
        if (model.getUserId().equals(FirebaseUtility.getCurrentUserId())) {
            holder.usernameText.setText(String.format("%s (Me)", model.getUsername()));
            holder.itemView.setEnabled(false);
        }

        FirebaseUtility.getProfilePictureByUserId(model.getUserId()).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtility.setProfilePicture(context, uri, holder.profilePicture);
                    }
                });

        holder.itemView.setOnClickListener(v -> {
            //navigate to user profile activity
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("userId", model.getUserId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
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