package com.example.chatapp.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import com.example.chatapp.R;
import com.example.chatapp.fragment.FriendFragment;
import com.example.chatapp.fragment.FriendRequestFragment;
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
    private FriendRequestFragment friendRequestFragment;
    private ImageButton searchButton;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    public static final int RECENT_CHAT_FRAGMENT = 0;
    public static final int PROFILE_FRAGMENT = 3;
    public static final int FRIEND_FRAGMENT = 1;
    public static final int FRIEND_REQUEST_FRAGMENT = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatFragment = new RecentChatFragment();
        profileFragment = new ProfileFragment();
        friendFragment = new FriendFragment();
        friendRequestFragment = new FriendRequestFragment();
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
            if (item.getItemId() == R.id.menu_friend) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame_layout, friendFragment).commit();
            }
            if (item.getItemId() == R.id.menu_friend_request) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame_layout, friendRequestFragment).commit();
            }
            return true;
        });

        int fragmentType = getIntent().getIntExtra("fragmentType", 0);
        switch (fragmentType) {
            case FRIEND_FRAGMENT:
                bottomNavigationView.setSelectedItemId(R.id.menu_friend);
                break;
            case FRIEND_REQUEST_FRAGMENT:
                bottomNavigationView.setSelectedItemId(R.id.menu_friend_request);
                break;
            case PROFILE_FRAGMENT:
                bottomNavigationView.setSelectedItemId(R.id.menu_profile);
                break;
            default:
                bottomNavigationView.setSelectedItemId(R.id.menu_chat);
        }

        updateFCMToken();

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

        });

        checkNotificationPermission();
    }

    private void checkNotificationPermission() {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                if (!notificationManager.areNotificationsEnabled()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Log.d("NOTIFICATION_REQUEST", "ASK PERMISSION");
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                }
            }
        } catch (Exception e) {
            Log.d("NOTIFICATION_ERROR", e.getMessage());
        }
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