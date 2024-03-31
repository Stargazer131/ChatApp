package com.example.chatapp.utility;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FirebaseUtility {
    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn() {
        return getCurrentUserId() != null;
    }

    public static DocumentReference getCurrentUser() {
        return getUserById(getCurrentUserId());
    }

    public static DocumentReference getUserById(String userId) {
        return FirebaseFirestore.getInstance().collection("users").document(userId);
    }

    public static void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    public static void updateCurrentUserStatus(String userId, String status) {
        FirebaseUtility.getUserById(userId).update("status", status);
    }

    public static void updateCurrentUserStatus(String status) {
        updateCurrentUserStatus(getCurrentUserId(), status);
    }

    public static void updateUserLastActive(String userId) {
        FirebaseUtility.getUserById(userId).update("lastActive", Timestamp.now());
    }

    public static void updateCurrentUserLastActive() {
        updateUserLastActive(getCurrentUserId());
    }

    public static void updateUserStatusAndLastActive(String userId) {
        getUserById(userId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                user.setLastActive(Timestamp.now());
                user.setStatus("offline");
                getUserById(userId).set(user).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()) {
                        Log.d("UPDATE USER STATUS", "SUCCESS");
                    }
                });
            }
        });
    }

    public static void updateCurrentUserStatusAndLastActive() {
        updateUserStatusAndLastActive(getCurrentUserId());
    }

    //////

    public static CollectionReference getAllUser() {
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static DocumentReference getChatRoomById(String chatroomId) {
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }

    public static CollectionReference getAllChatMessageOfChatRoomById(String chatroomId) {
        return getChatRoomById(chatroomId).collection("chats");
    }

    public static CollectionReference getAllChatRoom() {
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static String getChatRoomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds) {
        if (userIds.get(0).equals(FirebaseUtility.getCurrentUserId())) {
            return getAllUser().document(userIds.get(1));
        } else {
            return getAllUser().document(userIds.get(0));
        }
    }

    public static StorageReference getCurrentProfilePicture() {
        return getProfilePictureByUserId(FirebaseUtility.getCurrentUserId());
    }

    public static StorageReference getProfilePictureByUserId(String userId) {
        return FirebaseStorage.getInstance().getReference()
                .child("profile_pic").child(userId);
    }

    public static StorageReference getMediaFileOfChatRoomById(String chatRoomId, String mediaFileId) {
        return FirebaseStorage.getInstance().getReference()
                .child("media_file")
                .child(chatRoomId)
                .child(mediaFileId);
    }

    public static String timestampToCustomString(Timestamp timestamp) {
        Date now = new Date();
        Date date = timestamp.toDate();
        if(now.getDate() == date.getDate()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return "Today" + " " + dateFormat.format(date);

        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return dateFormat.format(date);
        }
    }

    public static String timestampToFullString(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS", Locale.getDefault());
        return dateFormat.format(date);
    }
}
