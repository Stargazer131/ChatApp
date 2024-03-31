package com.example.chatapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.User;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.google.firebase.Timestamp;

import java.util.Arrays;

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
        userId = getIntent().getStringExtra("userId");

        btnBackHome.setOnClickListener(v -> startActivity(AndroidUtility.getBackHomeIntent(UserProfileActivity.this)));

        btnChat.setOnClickListener(v -> {
            getChatRoomData();
        });

        setUserData();
    }

    private void getChatRoomData() {
        String chatroomId = FirebaseUtility.getChatRoomId(FirebaseUtility.getCurrentUserId(), userId);

        FirebaseUtility.getChatRoomById(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ChatRoom chatRoom = task.getResult().toObject(ChatRoom.class);
                if (chatRoom == null) {
                    //first time chat
                    chatRoom = new ChatRoom(
                            chatroomId,
                            Arrays.asList(FirebaseUtility.getCurrentUserId(), userId),
                            Timestamp.now(),
                            ""
                    );
                    ChatRoom finalChatRoom = chatRoom;
                    FirebaseUtility.getChatRoomById(chatroomId).set(chatRoom).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            openChatActivity(finalChatRoom.getChatroomId());
                        } else {
                            AndroidUtility.showToast(UserProfileActivity.this, "Can't make new chat room");
                        }
                    });
                } else {
                    openChatActivity(chatRoom.getChatroomId());
                }
            }
        });
    }

    private void openChatActivity(String chatRoomId) {
        Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
        intent.putExtra("otherUserId", userId);
        intent.putExtra("chatRoomId", chatRoomId);
        startActivity(intent);
    }

    private void setUserData() {
        FirebaseUtility.getProfilePictureByUserId(userId).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtility.setProfilePicture(UserProfileActivity.this, uri, userProfilePicture);
                    }
                });

        FirebaseUtility.getUserById(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = task.getResult().toObject(User.class);
                txtUsername.setText(user.getUsername());
                txtEmail.setText(user.getEmail());
            }
        });
    }
}