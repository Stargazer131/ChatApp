package com.example.chatapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.fragment.FriendFragment;
import com.example.chatapp.fragment.NotificationFragment;
import com.example.chatapp.fragment.ProfileFragment;
import com.example.chatapp.fragment.RecentChatFragment;
import com.example.chatapp.utility.FirebaseUtility;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private RecentChatFragment chatFragment;
    private ProfileFragment profileFragment;
    private FriendFragment friendFragment;
    private NotificationFragment notificationFragment;
    private ImageButton searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatFragment = new RecentChatFragment();
        profileFragment = new ProfileFragment();
        friendFragment = new FriendFragment();
        notificationFragment = new NotificationFragment();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        searchButton = findViewById(R.id.main_search_btn);

        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchUserActivity.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_chat) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame_layout, chatFragment).commit();
            }
            if (item.getItemId() == R.id.menu_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame_layout, profileFragment).commit();
            }
//            if (item.getItemId() == R.id.menu_notification) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.main_frame_layout, notificationFragment).commit();
//            }
//            if (item.getItemId() == R.id.menu_friend) {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.main_frame_layout, friendFragment).commit();
//            }
            return true;
        });
        bottomNavigationView.setSelectedItemId(R.id.menu_chat);
        updateFCMToken();
    }

    private void updateFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        FirebaseUtility.getCurrentUser().update("fcmToken", token);
                    }
                });
    }
}