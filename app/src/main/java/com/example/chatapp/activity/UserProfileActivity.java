package com.example.chatapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.FriendRequest;
import com.example.chatapp.model.User;
import com.example.chatapp.model.UserRelationship;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {
    private User user;
    private String userId;
    private String userRelationshipId;
    private ImageView userProfilePicture;
    private TextView txtUsername, txtEmail, txtFriend, txtUsernameUserProfile;
    private ImageButton btnBackHome;
    private ImageView btnChat, btnFriend;
    private UserRelationship userRelationship;


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
        userRelationshipId = FirebaseUtility.getUserRelationshipId(userId, FirebaseUtility.getCurrentUserId());

        setUserData();
        setUserRelationshipData();
        setUpUserRelationshipListener();

        btnBackHome.setOnClickListener(v -> startActivity(AndroidUtility.getBackHomeIntent(UserProfileActivity.this)));
        btnChat.setOnClickListener(v -> {
            getChatRoomData();
        });
        btnFriend.setOnClickListener(v -> checkUserRel());
    }

    private void setUpUserRelationshipListener() {
        FirebaseUtility.getUserRelationshipById(userRelationshipId)
                .addSnapshotListener((value, error) -> {
                    String tag = "USER_RELATIONSHIP_DOCUMENT_LISTENER";
                    if (error != null) {
                        Log.w(tag, "Listen failed.", error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        Log.d(tag, "Current data: has changed");
                        String relationshipType = value.getString("type");
                        String id = value.getString("id");

                        if(id != null) {
                            List<String> userIds = Arrays.asList(userId, FirebaseUtility.getCurrentUserId());
                            userRelationship = new UserRelationship(id, userIds, relationshipType);
                            return;
                        }

                        if (relationshipType != null) {
                            userRelationship.setType(relationshipType);
                        }
                    } else {
                        Log.d(tag, "Current data: null");
                    }
                });
    }

    private void checkUserRel() {
        String userRelationshipId = FirebaseUtility.getUserRelationshipId(FirebaseUtility.getCurrentUserId(), userId);
        FirebaseUtility.getUserRelationshipById(userRelationshipId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserRelationship userRelationshipData = task.getResult().toObject(UserRelationship.class);
                        if (userRelationshipData == null) {
                            sendFriendRequest();
                        }
                    }
                });
    }

    private void sendFriendRequest() {
        btnFriend.setEnabled(false);
        String fromUserId = FirebaseUtility.getCurrentUserId();
        String toUserId = userId;
        String friendRequestId = FirebaseUtility.getFriendRequestId(fromUserId, toUserId);
        Timestamp timestamp = Timestamp.now();
        FriendRequest friendRequest = new FriendRequest(friendRequestId, fromUserId, toUserId, timestamp);

        FirebaseUtility.getFriendRequestById(friendRequestId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FriendRequest friendRequestData = task.getResult().toObject(FriendRequest.class);
                        if (friendRequestData != null) {
                            if (friendRequestData.getFromUserId().equals(FirebaseUtility.getCurrentUserId())) {
                                AndroidUtility.showToast(
                                        UserProfileActivity.this, "You have already sent the request"
                                );
                            } else {
                                AndroidUtility.showToast(
                                        UserProfileActivity.this, "Other user have already sent the request"
                                );
                            }
                            btnFriend.setEnabled(true);

                        } else {
                            FirebaseUtility.getFriendRequestById(friendRequestId).set(friendRequest)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            AndroidUtility.showToast(
                                                    UserProfileActivity.this, "Friend request sent successfully"
                                            );
                                            sendNotification();

                                        } else {
                                            AndroidUtility.showToast(
                                                    UserProfileActivity.this, "Friend request sent failed"
                                            );

                                        }
                                        btnFriend.setEnabled(true);
                                    });
                        }
                    }
                });
    }

    private void sendNotification() {
        FirebaseUtility.getCurrentUser().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User currentUser = task.getResult().toObject(User.class);
                try {
                    // notification
                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title", "New friend request");
                    notificationObj.put("body", currentUser.getUsername() + " " + "has sent you a friend request");

                    // data
                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId", currentUser.getUserId());
                    dataObj.put("notificationType", FirebaseUtility.NOTIFICATION_TYPE_REQUEST);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("data", dataObj);
                    jsonObject.put("notification", notificationObj);
                    jsonObject.put("to", user.getFcmToken());

                    FirebaseUtility.callFCMApi(jsonObject);

                } catch (Exception ignored) {
                }
            }
        });
    }

    private void setUserRelationshipData() {
        btnFriend.setEnabled(false);
        btnChat.setEnabled(false);
        String userRelationshipId = FirebaseUtility.getUserRelationshipId(FirebaseUtility.getCurrentUserId(), userId);
        FirebaseUtility.getUserRelationshipById(userRelationshipId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userRelationship = task.getResult().toObject(UserRelationship.class);
                        changeFriendButtonBasedOnRelationship();
                    }
                    btnFriend.setEnabled(true);
                    btnChat.setEnabled(true);
                });
    }

    private void changeFriendButtonBasedOnRelationship() {
        if (userRelationship == null) {
            txtFriend.setText("Add friend");
            btnFriend.setImageResource(R.drawable.ic_friend_request);
            return;
        }

        if (userRelationship.getType().equals("friend")) {
            txtFriend.setText("Friend");
            btnFriend.setImageResource(R.drawable.ic_friendship);
            return;
        }
    }

    private void getChatRoomData() {
        if (userRelationship == null || !userRelationship.getType().equals(UserRelationship.FRIEND)) {
            AndroidUtility.showToast(UserProfileActivity.this, "You must be friend before chatting");
            return;
        }

        String chatroomId = FirebaseUtility.getChatRoomId(FirebaseUtility.getCurrentUserId(), userId);
        btnChat.setEnabled(false);
        FirebaseUtility.getChatRoomById(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ChatRoom chatRoom = task.getResult().toObject(ChatRoom.class);
                if (chatRoom == null) {
                    //first time chat
                    chatRoom = new ChatRoom(
                            chatroomId,
                            Arrays.asList(FirebaseUtility.getCurrentUserId(), userId),
                            Timestamp.now(),
                            null,
                            null,
                            null
                    );
                    ChatRoom finalChatRoom = chatRoom;
                    FirebaseUtility.getChatRoomById(chatroomId).set(chatRoom).addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            openChatActivity(finalChatRoom.getChatroomId());
                        } else {
                            AndroidUtility.showToast(UserProfileActivity.this, "Can't make new chat room");
                        }
                    });
                } else {
                    openChatActivity(chatRoom.getChatroomId());
                }
            }
            btnChat.setEnabled(true);
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