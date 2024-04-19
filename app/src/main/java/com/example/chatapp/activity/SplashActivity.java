package com.example.chatapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.example.chatapp.R;
import com.example.chatapp.model.User;
import com.example.chatapp.utility.AppLifecycleListener;
import com.example.chatapp.utility.FirebaseUtility;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());

        String userId = null;
        try {
            userId = getIntent().getStringExtra("userId");
        } catch (Exception ignored) {

        }

        if (userId != null) {
            startAppFromNotification(userId);
        } else {
            startApp();
        }
    }

    private void startApp() {
        new Handler().postDelayed(() -> {
            if (FirebaseUtility.isLoggedIn()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, EmailLoginActivity.class));
            }
            finish();
        }, 1000);
    }

    private void startAppFromNotification(String userId) {
        String notificationType = getIntent().getStringExtra("notificationType");
        if(notificationType.equals(FirebaseUtility.NOTIFICATION_TYPE_CHAT)) {
            String chatRoomId = FirebaseUtility.getChatRoomId(userId, FirebaseUtility.getCurrentUserId());
            Intent intent = new Intent(SplashActivity.this, ChatActivity.class);
            intent.putExtra("otherUserId", userId);
            intent.putExtra("chatRoomId", chatRoomId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mainIntent.putExtra("fragmentType", MainActivity.FRIEND_REQUEST_FRAGMENT);
            startActivity(mainIntent);
            finish();
        }
    }
}