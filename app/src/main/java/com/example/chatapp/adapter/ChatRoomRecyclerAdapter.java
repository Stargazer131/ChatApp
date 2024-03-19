package com.example.chatapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activity.UserProfileActivity;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.utils.AndroidUtil;
import com.example.chatapp.utils.FirebaseUtil;
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
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatTextview.setText(model.getMessage());

            FirebaseUtil.getProfilePictureByUserId(currentUserId).getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();
                                AndroidUtil.setProfilePicture(context, uri, holder.leftChatImageView);
                            }
                        }
                    });

        } else {
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatTextview.setText(model.getMessage());

            FirebaseUtil.getProfilePictureByUserId(otherUserId).getDownloadUrl()
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri uri = task.getResult();
                                AndroidUtil.setProfilePicture(context, uri, holder.rightChatImageView);
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
        ConstraintLayout rightChatLayout;
        TextView leftChatTextview, rightChatTextview;
        ImageView leftChatImageView, rightChatImageView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);

            leftChatImageView = itemView.findViewById(R.id.left_profile_picture);
            rightChatImageView = itemView.findViewById(R.id.right_profile_picture);
        }
    }
}