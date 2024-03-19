package com.example.chatapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.ChatRoomRecyclerAdapter;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.model.User;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private User otherUser;
    private String chatroomId;
    private ChatRoom chatRoom;
    private ChatRoomRecyclerAdapter adapter;

    private EditText messageInput;
    private ImageButton sendMessageBtn;
    private ImageButton btnBackHome;
    private TextView otherUsername;
    private RecyclerView recyclerView;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get UserModel
        otherUser = (User)getIntent().getSerializableExtra("otherUser");
        chatroomId = FirebaseUtil.getChatRoomId(FirebaseUtil.getCurrentUserId(), otherUser.getUserId());

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        btnBackHome = findViewById(R.id.btn_back_home);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_layout);


        otherUsername.setText(otherUser.getUsername());
        FirebaseUtil.getProfilePictureByUserId(otherUser.getUserId()).getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri uri = task.getResult();
                            AndroidUtil.setProfilePicture(ChatActivity.this, uri, imageView);
                        }
                    }
                });

        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AndroidUtil.getBackHomeIntent(ChatActivity.this));
            }
        });

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString().trim();
                if (message.isEmpty()) {
                    return;
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(messageInput.getWindowToken(), 0);
                sendMessageToUser(message);
            }
        });

        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }

    private void setupChatRecyclerView() {
        Query query = FirebaseUtil.getAllChatMessageOfChatRoomById(chatroomId)
                .orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class).build();

        adapter = new ChatRoomRecyclerAdapter(options, getApplicationContext(), otherUser.getUserId());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int lastInsertedPosition = positionStart + itemCount - 1;
                recyclerView.smoothScrollToPosition(lastInsertedPosition);
            }
        });
    }

    private void sendMessageToUser(String message) {
        chatRoom.setLastMessageTimestamp(Timestamp.now());
        chatRoom.setLastMessageSenderId(FirebaseUtil.getCurrentUserId());
        chatRoom.setLastMessage(message);
        FirebaseUtil.getChatRoomById(chatroomId).set(chatRoom);

        ChatMessage chatMessage = new ChatMessage(message, FirebaseUtil.getCurrentUserId(), Timestamp.now());
        FirebaseUtil.getAllChatMessageOfChatRoomById(chatroomId).add(chatMessage)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                            sendNotification(message);
                        }
                    }
                });
    }

    private void getOrCreateChatroomModel() {
        FirebaseUtil.getChatRoomById(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    chatRoom = task.getResult().toObject(ChatRoom.class);
                    if (chatRoom == null) {
                        //first time chat
                        chatRoom = new ChatRoom(
                                chatroomId,
                                Arrays.asList(FirebaseUtil.getCurrentUserId(), otherUser.getUserId()),
                                Timestamp.now(),
                                ""
                        );
                        FirebaseUtil.getChatRoomById(chatroomId).set(chatRoom);
                    }
                }
            }
        });
    }

    private void sendNotification(String message) {

        FirebaseUtil.getCurrentUser().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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

                        callApi(jsonObject);

                    } catch (Exception e) {

                    }

                }
            }
        });

    }

    private void callApi(JSONObject jsonObject) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        String apiKey = "AAAAJiCJMaM:APA91bGanpXPGafcmHJRTAfaWBcMlGqxPTSdp9OvYiI80bUz_O_dI_VDXgWU2gePx-czLUafmf_JgxQKg9gBUqjs3uhxb8PW9lTlKRC4zgibtZWdt-DTcsv8bYqhCQrRC5egFG96OaRq";
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
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }

}