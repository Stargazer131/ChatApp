package com.example.chatapp.utility;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.example.chatapp.model.User;

public class AppLifecycleListener implements DefaultLifecycleObserver {
    private final String TAG = "AppLifecycleListener";

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.i(TAG, "Moved to foreground");
        if(FirebaseUtility.isLoggedIn()) {
            // online
            FirebaseUtility.updateCurrentUserStatus(User.USER_ONLINE);
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.i(TAG, "Moved to background");
        if(FirebaseUtility.isLoggedIn()) {
            // offline
            FirebaseUtility.updateCurrentUserStatusAndLastActive();
        }
    }
}
