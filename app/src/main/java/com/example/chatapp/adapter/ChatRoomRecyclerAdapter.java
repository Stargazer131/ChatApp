package com.example.chatapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
import com.example.chatapp.utils.UniformContract;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class ChatRoomRecyclerAdapter extends
        FirestoreRecyclerAdapter<ChatMessage, ChatRoomRecyclerAdapter.ChatViewHolder> {

    private Context context;
    private String currentUserId;
    private String otherUserId;

    public ChatRoomRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessage> options, Context context, String otherUserId) {
        super(options);
        this.context = context;
        this.otherUserId = otherUserId;
        currentUserId = FirebaseUtil.getCurrentUserId();
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull ChatMessage model) {
        if (model.getSenderId().equals(currentUserId)) {
            holder.rightChatLayout.setVisibility(View.GONE);
            bindItemToView(holder.leftChatLayout, holder.leftChatTextview, holder.leftChatImageProfile,
                    holder.leftChatImageView, model, currentUserId);

        } else {
            holder.leftChatLayout.setVisibility(View.GONE);
            bindItemToView(holder.rightChatLayout, holder.rightChatTextview, holder.rightChatImageProfile,
                    holder.rightChatImageView, model, otherUserId);
        }
    }

    private void bindItemToView(ConstraintLayout chatLayout, TextView chatTextView, ImageView chatImageProfile,
                                ImageView chatImageView, ChatMessage model, String userId) {

        model.log();

        chatLayout.setVisibility(View.VISIBLE);
        FirebaseUtil.getProfilePictureByUserId(userId).getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri uri = task.getResult();
                            AndroidUtil.setProfilePicture(context, uri, chatImageProfile);
                        }
                    }
                });

        if(model.getType().equals(UniformContract.MESSAGE_TYPE_STRING)) {
            chatTextView.setText(model.getMessage());
            chatImageView.setVisibility(View.GONE);
        } else  {
            chatTextView.setVisibility(View.GONE);
            FirebaseUtil.getMediaFileOfChatRoomById(model.getChatRoomId(), model.getMediaFileId()).getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();
                                AndroidUtil.setImagePicture(context, uri, chatImageView);
                            }
                            else {
                                Log.d("ERROR", task.getException().toString());
                            }
                        }
                    });
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row, parent, false);
        return new ChatViewHolder(view);
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout leftChatLayout;
        TextView leftChatTextview;
        ImageView leftChatImageProfile;
        ImageView leftChatImageView;



        ConstraintLayout rightChatLayout;
        TextView rightChatTextview;
        ImageView rightChatImageProfile;

        ImageView rightChatImageView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            leftChatImageProfile = itemView.findViewById(R.id.left_profile_picture);
            leftChatImageView = itemView.findViewById(R.id.left_chat_imageview);

            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            rightChatImageProfile = itemView.findViewById(R.id.right_profile_picture);
            rightChatImageView = itemView.findViewById(R.id.right_chat_imageview);
        }
    }
}