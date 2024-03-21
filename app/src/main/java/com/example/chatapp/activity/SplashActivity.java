package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.chatapp.R;
import com.example.chatapp.model.User;
import com.example.chatapp.utils.AppLifecycleListener;
import com.example.chatapp.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FirebaseUtil.isLoggedIn()) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, EmailLoginActivity.class));
                }
                finish();
            }
        }, 1000);
    }

    private void startAppFromNotification(String userId) {
        FirebaseUtil.getUserById(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            User model = task.getResult().toObject(User.class);

                            Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);

                            Intent intent = new Intent(SplashActivity.this, ChatActivity.class);
                            intent.putExtra("otherUser", model);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }
}