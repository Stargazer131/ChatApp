package com.example.chatapp.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class AppLifecycleListener implements DefaultLifecycleObserver {
    private final String TAG = "AppLifecycleListener";

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.i(TAG, "Moved to foreground");
        if(FirebaseUtil.isLoggedIn()) {
            FirebaseUtil.updateUserStatus("online");
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.i(TAG, "Moved to background");
        if(FirebaseUtil.isLoggedIn()) {
            FirebaseUtil.updateUserStatus("offline");
        }
    }
}
