package com.example.chatapp.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.ChatRoomRecyclerAdapter;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.User;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private User otherUser;
    private String otherUserId;
    private ChatRoom chatRoom;
    private String chatroomId;
    private ChatRoomRecyclerAdapter adapter;

    private EditText messageInput;
    private ImageButton sendMessageBtn, btnAddOtherFile;
    private ImageButton btnBackHome;
    private TextView otherUsername;
    private TextView otherUserStatusTxt;
    private RecyclerView recyclerView;
    private ImageView otherUserProfileImageView;
    private PopupMenu popupMenu;
    // for sending image
    private ActivityResultLauncher<PickVisualMediaRequest> imagePickerLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> videoPickerLauncher;
    private ActivityResultLauncher<Intent> filePickerLauncher;
    public static final String MESSAGE_TYPE_STRING = "String";
    public static final String MESSAGE_TYPE_IMAGE = "Image";
    public static final String MESSAGE_TYPE_VIDEO = "Video";
    public static final String MESSAGE_TYPE_AUDIO = "Audio";
    public static final String MESSAGE_TYPE_FILE = "File";
    public static final long MB_THRESHOLD = 20;
    public static final long MEDIA_SIZE_THRESHOLD = 1024 * 1024 * MB_THRESHOLD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get UserModel
        otherUserId = getIntent().getStringExtra("otherUserId");
        chatroomId = getIntent().getStringExtra("chatRoomId");

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        btnBackHome = findViewById(R.id.btn_back_home);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        otherUserProfileImageView = findViewById(R.id.profile_pic_layout);
        btnAddOtherFile = findViewById(R.id.btn_send_other_file);
        otherUserStatusTxt = findViewById(R.id.other_user_status_txt);

        setUpFilePicker();
        setUpImagePicker();
        setUpVideoPicker();
        setOtherUserData();
        setUpUserStatusListener();
        getChatRoom();
        setupChatRecyclerView();

        btnBackHome.setOnClickListener(v -> startActivity(AndroidUtility.getBackHomeIntent(ChatActivity.this)));
        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {
                return;
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);
            sendMessageToUser(message);
        });
        btnAddOtherFile.setOnClickListener(v -> setUpPopupMenu());
        otherUserProfileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, UserProfileActivity.class);
            intent.putExtra("userId", otherUser.getUserId());
            startActivity(intent);
        });
    }

    private void setUpFilePicker() {
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData().getData();
                        if(uri == null) {
                            return;
                        }

                        long size = getMediaFileSize(ChatActivity.this, uri);
                        if (size > MEDIA_SIZE_THRESHOLD) {
                            AndroidUtility.showToast(
                                    ChatActivity.this,
                                    String.format(Locale.getDefault(), "File can't be over %dMB", MB_THRESHOLD)
                            );
                            return;
                        }

                        sendFileToUser(uri);
                    }
        });
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "Unknown";
        if (uri != null) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex);
                    }
                }
            } catch (Exception e) {
                // Handle any potential exceptions here
                e.printStackTrace();
            }
        }
        return fileName;
    }


    private void setUpVideoPicker() {
        videoPickerLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), o -> {
            if (o != null) {
                long size = getMediaFileSize(ChatActivity.this, o);
                if (size > MEDIA_SIZE_THRESHOLD) {
                    AndroidUtility.showToast(
                            ChatActivity.this,
                            String.format(Locale.getDefault(), "Media File can't be over %dMB", MB_THRESHOLD)
                    );
                    return;
                }
                sendMediaFileToUser(o, MESSAGE_TYPE_VIDEO);
            }
        });
    }

    private void setUpImagePicker() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), o -> {
            if (o != null) {
                long size = getMediaFileSize(ChatActivity.this, o);
                if (size > MEDIA_SIZE_THRESHOLD) {
                    AndroidUtility.showToast(
                            ChatActivity.this,
                            String.format(Locale.getDefault(), "Media File can't be over %dMB", MB_THRESHOLD)
                    );
                    return;
                }
                sendMediaFileToUser(o, MESSAGE_TYPE_IMAGE);
            }
        });
    }

    private void setUpPopupMenu() {
        popupMenu = new PopupMenu(ChatActivity.this, btnAddOtherFile);
        popupMenu.getMenuInflater().inflate(R.menu.pop_up_other_file_menu, popupMenu.getMenu());
        popupMenu.setForceShowIcon(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_item_image) {
                imagePickerLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
            if (item.getItemId() == R.id.menu_item_video) {
                videoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                        .build());
            }
            if (item.getItemId() == R.id.menu_item_file) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*"); // You can set specific MIME types here, e.g., "application/pdf" for PDF files
                filePickerLauncher.launch(intent);
            }

            return true;
        });

        popupMenu.show();
    }

    private void setUpUserStatusListener() {
        FirebaseUtility.getUserById(otherUserId)
                .addSnapshotListener((value, error) -> {
                    String tag = "CHAT_USER_DOCUMENT_LISTENER";
                    if (error != null) {
                        Log.w(tag, "Listen failed.", error);
                        return;
                    }

                    if (value != null && value.exists()) {
                        Log.d(tag, "Current data: has changed");
                        String status = value.getString("status");
                        Timestamp lastActive = value.getTimestamp("lastActive");
                        if (status != null) {
                            AndroidUtility.changeAvatarProfileColor(status, otherUserProfileImageView, ChatActivity.this);
                            changeStatusText(status, lastActive);
                        }
                    } else {
                        Log.d(tag, "Current data: null");
                    }
                });
    }

    private void changeStatusText(String status, Timestamp timestamp) {
        if (status.equals("online")) {
            otherUserStatusTxt.setText("Online");
            otherUserStatusTxt.setTextColor(Color.GREEN);
        } else {
            otherUserStatusTxt.setText(
                    String.format("Last active: %s", FirebaseUtility.timestampToCustomString(timestamp))
            );
            otherUserStatusTxt.setTextColor(Color.DKGRAY);
        }
    }

    private void setOtherUserData() {
        FirebaseUtility.getUserById(otherUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                otherUser = task.getResult().toObject(User.class);
                otherUsername.setText(otherUser.getUsername());
                AndroidUtility.changeAvatarProfileColor(otherUser.getStatus(), otherUserProfileImageView, ChatActivity.this);
                changeStatusText(otherUser.getStatus(), otherUser.getLastActive());
            }
        });

        FirebaseUtility.getProfilePictureByUserId(otherUserId).getDownloadUrl()
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Uri uri = task1.getResult();
                        AndroidUtility.setProfilePicture(ChatActivity.this, uri, otherUserProfileImageView);
                    }
                });
    }

    private void setupChatRecyclerView() {
        Query query = FirebaseUtility.getAllChatMessageOfChatRoomById(chatroomId)
                .orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class).build();

        adapter = new ChatRoomRecyclerAdapter(options, ChatActivity.this, otherUserId);
        LinearLayoutManager manager = new LinearLayoutManager(ChatActivity.this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int lastInsertedPosition = positionStart + itemCount - 1;
                recyclerView.smoothScrollToPosition(lastInsertedPosition);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter.stopListening();
        }
        super.onDestroy();
    }

    private void sendMessageToUser(String message) {
        chatRoom.setLastMessageTimestamp(Timestamp.now());
        chatRoom.setLastMessageSenderId(FirebaseUtility.getCurrentUserId());
        chatRoom.setLastMessage(message);
        FirebaseUtility.getChatRoomById(chatroomId).set(chatRoom);

        ChatMessage chatMessage = new ChatMessage(
                message, FirebaseUtility.getCurrentUserId(), Timestamp.now(),
                chatroomId, MESSAGE_TYPE_STRING, ""
        );
        FirebaseUtility.getAllChatMessageOfChatRoomById(chatroomId).add(chatMessage)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        messageInput.setText("");
                        boolean isNotificationSuccessful = sendNotification(message);
                        Log.d("SEND_Notification", String.valueOf(isNotificationSuccessful));
                    }
                });
    }

    private void sendFileToUser(Uri uri) {
        // update chat room
        String message = getFileNameFromUri(uri);
        Timestamp timestampNow = Timestamp.now();

        chatRoom.setLastMessageTimestamp(timestampNow);
        chatRoom.setLastMessageSenderId(FirebaseUtility.getCurrentUserId());
        chatRoom.setLastMessage(message);
        FirebaseUtility.getChatRoomById(chatroomId).set(chatRoom);

        // set message type
        // generate unique media file key
        String mediaFieldId = String.format("%s_%s",
                FirebaseUtility.getCurrentUserId(), FirebaseUtility.timestampToFullString(timestampNow)
        );
        ChatMessage chatMessage = new ChatMessage(
                message, FirebaseUtility.getCurrentUserId(), timestampNow,
                chatroomId, MESSAGE_TYPE_FILE, mediaFieldId
        );

        // update media file to firebase
        updateMediaFileToFirebase(uri, chatroomId, mediaFieldId, chatMessage);
    }

    private void sendMediaFileToUser(Uri uri, String type) {
        // update chat room
        String message = type;
        Timestamp timestampNow = Timestamp.now();

        chatRoom.setLastMessageTimestamp(timestampNow);
        chatRoom.setLastMessageSenderId(FirebaseUtility.getCurrentUserId());
        chatRoom.setLastMessage(message);
        FirebaseUtility.getChatRoomById(chatroomId).set(chatRoom);

        // set message type
        // generate unique media file key
        String mediaFieldId = String.format("%s_%s",
                FirebaseUtility.getCurrentUserId(), FirebaseUtility.timestampToFullString(timestampNow)
        );
        ChatMessage chatMessage = new ChatMessage(
                message, FirebaseUtility.getCurrentUserId(), timestampNow,
                chatroomId, type, mediaFieldId
        );

        // update media file to firebase
        updateMediaFileToFirebase(uri, chatroomId, mediaFieldId, chatMessage);
    }

    private void updateMediaFileToFirebase(Uri uri, String chatroomId, String mediaFileId, ChatMessage chatMessage) {
        FirebaseUtility.getMediaFileOfChatRoomById(chatroomId, mediaFileId).putFile(uri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUtility.getAllChatMessageOfChatRoomById(chatroomId).add(chatMessage)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        boolean isNotificationSuccessful = sendNotification(chatMessage.getMessage());
                                        Log.d("SEND_Notification", String.valueOf(isNotificationSuccessful));

                                    } else {
                                        AndroidUtility.showToast(ChatActivity.this, "Can't add message");
                                    }
                                });
                    } else {
                        AndroidUtility.showToast(ChatActivity.this, "Can't add MEDIA FILE");
                    }
                });
    }


    private void getChatRoom() {
        FirebaseUtility.getChatRoomById(chatroomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatRoom = task.getResult().toObject(ChatRoom.class);
            }
        });
    }

    private boolean sendNotification(String message) {
        AtomicBoolean successful = new AtomicBoolean(false);
        FirebaseUtility.getCurrentUser().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User currentUser = task.getResult().toObject(User.class);
                try {
                    // notification
                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title", currentUser.getUsername());
                    notificationObj.put("body", message);

                    // data
                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId", currentUser.getUserId());

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("data", dataObj);
                    jsonObject.put("notification", notificationObj);
                    jsonObject.put("to", otherUser.getFcmToken());

                    successful.set(callApi(jsonObject));

                } catch (Exception ignored) {
                    successful.set(false);
                }
            }
        });

        return successful.get();
    }

    private boolean callApi(JSONObject jsonObject) {
        final boolean[] successful = {false};

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
                successful[0] = false;
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                successful[0] = true;
            }
        });
        return successful[0];
    }

    private static long getMediaFileSize(Context context, Uri mediaUri) {
        long fileSize = 0;
        ContentResolver contentResolver = context.getContentResolver();
        try (Cursor cursor = contentResolver.query(mediaUri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            Log.e("MediaSizeHelper", "Error retrieving file size", e);
        }
        return fileSize;
    }

}