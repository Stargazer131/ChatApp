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

        Bundle extras = getIntent().getExtras();
        String userId = (extras != null) ? extras.getString("userId") : null;
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
        FirebaseUtility.getUserById(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User model = task.getResult().toObject(User.class);

                        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(mainIntent);

                        String chatRoomId = FirebaseUtility.getChatRoomId(userId, FirebaseUtility.getCurrentUserId());
                        Intent intent = new Intent(SplashActivity.this, ChatActivity.class);
                        intent.putExtra("otherUser", model);
                        intent.putExtra("chatRoomId", chatRoomId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}