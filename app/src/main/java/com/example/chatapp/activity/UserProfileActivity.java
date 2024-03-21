package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.model.User;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserProfileActivity extends AppCompatActivity {
    private User user;
    private String userId;
    private ImageView userProfilePicture;
    private TextView txtUsername, txtEmail, txtFriend, txtUsernameUserProfile;
    private ImageButton btnBackHome;
    private ImageView btnChat, btnFriend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userProfilePicture = findViewById(R.id.image_view_user_profile_picture);
        txtUsername = findViewById(R.id.text_user_profile_username);
        txtEmail = findViewById(R.id.text_user_profile_email);
        txtFriend = findViewById(R.id.text_user_profile_friend);
        btnChat = findViewById(R.id.btn_user_profile_chat);
        btnFriend = findViewById(R.id.btn_user_profile_friend);
        btnBackHome = findViewById(R.id.btn_user_profile_back_home);
        txtUsernameUserProfile = findViewById(R.id.text_username_user_profile);
        userId = getIntent().getExtras().getString("userId");

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AndroidUtil.getBackHomeIntent(UserProfileActivity.this));
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("otherUser", user);
                startActivity(intent);
            }
        });

        setUserData();
    }

    private void setUserData() {
        FirebaseUtil.getProfilePictureByUserId(userId).getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri uri = task.getResult();
                            AndroidUtil.setProfilePicture(UserProfileActivity.this, uri, userProfilePicture);
                        }
                    }
                });

        FirebaseUtil.getUserById(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    user = task.getResult().toObject(User.class);
                    txtUsername.setText(user.getUsername());
                    txtEmail.setText(user.getEmail());
                }
            }
        });
    }
}