package com.example.chatapp.utility;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.chatapp.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FirebaseUtility {
    private static final String USER_TABLE = "users";
    private static final String CHATROOM_TABLE = "chatrooms";
    private static final String CHAT_MESSAGE_TABLE = "chats";
    private static final String FRIEND_REQUEST_TABLE = "friend_requests";
    private static final String USER_RELATIONSHIP_TABLE = "user_relationships";

    private static final String USER_PROFILE_STORAGE = "profile_pic";
    private static final String USER_MEDIA_FILE_STORAGE = "media_file";
    public static final String NOTIFICATION_TYPE_CHAT = "chat_message";
    public static final String NOTIFICATION_TYPE_REQUEST = "friend_request";

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
        return FirebaseFirestore.getInstance().collection(USER_TABLE).document(userId);
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
                user.setStatus(User.USER_OFFLINE);
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

    public static CollectionReference getAllFriendRequest() {
        return FirebaseFirestore.getInstance().collection(FRIEND_REQUEST_TABLE);
    }

    public static CollectionReference getAllUserRelationship() {
        return FirebaseFirestore.getInstance().collection(USER_RELATIONSHIP_TABLE);
    }

    public static CollectionReference getAllUser() {
        return FirebaseFirestore.getInstance().collection(USER_TABLE);
    }

    public static DocumentReference getChatRoomById(String chatroomId) {
        return FirebaseFirestore.getInstance().collection(CHATROOM_TABLE).document(chatroomId);
    }

    public static DocumentReference getFriendRequestById(String friendRequestId) {
        return FirebaseFirestore.getInstance().collection(FRIEND_REQUEST_TABLE).document(friendRequestId);
    }

    public static DocumentReference getUserRelationshipById(String userRelationshipId) {
        return FirebaseFirestore.getInstance().collection(USER_RELATIONSHIP_TABLE).document(userRelationshipId);
    }

    public static CollectionReference getAllChatMessageOfChatRoomById(String chatroomId) {
        return getChatRoomById(chatroomId).collection(CHAT_MESSAGE_TABLE);
    }

    public static CollectionReference getAllChatRoom() {
        return FirebaseFirestore.getInstance().collection(CHATROOM_TABLE);
    }

    public static String getChatRoomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    public static String getFriendRequestId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    public static String getUserRelationshipId(String userId1, String userId2) {
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

    public static DocumentReference getOtherUserFromUserRelationship(List<String> userIds) {
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
                .child(USER_PROFILE_STORAGE).child(userId);
    }

    public static StorageReference getMediaFileOfChatRoomById(String chatRoomId, String mediaFileId) {
        return FirebaseStorage.getInstance().getReference()
                .child(USER_MEDIA_FILE_STORAGE)
                .child(chatRoomId)
                .child(mediaFileId);
    }

    public static String timestampToCustomString(Timestamp timestamp) {
        Date now = new Date();
        Date date = timestamp.toDate();
        if(now.getDate() == date.getDate()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return dateFormat.format(date);

        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
            return dateFormat.format(date);
        }
    }

    public static String timestampToFullString(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss-SSS", Locale.getDefault());
        return dateFormat.format(date);
    }

    public static void callFCMApi(JSONObject jsonObject) {
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        String apiKey = "AAAAJiCJMaM:APA91bGanpXPGafcmHJRTAfaWBcMlGqx" +
                "PTSdp9OvYiI80bUz_O_dI_VDXgWU2gePx-czLUafmf_JgxQKg9gBUqjs3uhxb8PW9l" +
                "TlKRC4zgibtZWdt-DTcsv8bYqhCQrRC5egFG96OaRq";
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", "Bearer" + " " + apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
            }
        });
    }
}
