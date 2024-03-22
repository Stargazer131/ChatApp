package com.example.chatapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.example.chatapp.utility.UrlString;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;

public class ChatRoomRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessage, RecyclerView.ViewHolder> {
    private static final int TYPE_USER_MESSAGE = 0;
    private static final int TYPE_USER_IMAGE = 1;
    private static final int TYPE_OTHER_USER_MESSAGE = 2;
    private static final int TYPE_OTHER_USER_IMAGE = 3;

    private Context context;
    private String currentUserId;
    private String otherUserId;

    public ChatRoomRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessage> options, Context context, String otherUserId) {
        super(options);
        this.context = context;
        this.otherUserId = otherUserId;
        currentUserId = FirebaseUtility.getCurrentUserId();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_USER_MESSAGE) {
            View userMessageView = inflater.inflate(R.layout.user_message_recycler_row, parent, false);
            return new UserMessageViewHolder(userMessageView);
        } else if (viewType == TYPE_USER_IMAGE) {
            View userImageView = inflater.inflate(R.layout.user_image_recycler_row, parent, false);
            return new UserImageViewHolder(userImageView);
        } else if (viewType == TYPE_OTHER_USER_MESSAGE) {
            View otherUserMessageView = inflater.inflate(R.layout.other_user_message_recycler_row, parent, false);
            return new OtherUserMessageViewHolder(otherUserMessageView);
        } else {
            View otherUserImageView = inflater.inflate(R.layout.other_user_image_recycler_row, parent, false);
            return new OtherUserImageViewHolder(otherUserImageView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage model = getItem(position);

        if (model.getSenderId().equals(currentUserId)) {
            if (model.getType().equals(ChatActivity.MESSAGE_TYPE_STRING)) {
                return TYPE_USER_MESSAGE;
            } else {
                return TYPE_USER_IMAGE;
            }
        } else {
            if (model.getType().equals(ChatActivity.MESSAGE_TYPE_STRING)) {
                return TYPE_OTHER_USER_MESSAGE;
            } else {
                return TYPE_OTHER_USER_IMAGE;
            }
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull ChatMessage model) {
        int viewType = holder.getItemViewType();
        if (viewType == TYPE_USER_MESSAGE) {
            UserMessageViewHolder userMessageHolder = (UserMessageViewHolder) holder;
            setProfileImage(currentUserId, userMessageHolder.leftChatImageProfile);
            handleTextAndLink(userMessageHolder.leftChatTextview, model.getMessage());

        } else if (viewType == TYPE_USER_IMAGE) {
            UserImageViewHolder userImageHolder = (UserImageViewHolder) holder;
            setProfileImage(currentUserId, userImageHolder.leftChatImageProfile);
            setImage(model, userImageHolder.leftChatImageView);

        } else if (viewType == TYPE_OTHER_USER_MESSAGE) {
            OtherUserMessageViewHolder otherUserMessageHolder = (OtherUserMessageViewHolder) holder;
            setProfileImage(otherUserId, otherUserMessageHolder.rightChatImageProfile);
            handleTextAndLink(otherUserMessageHolder.rightChatTextview, model.getMessage());

        } else {
            OtherUserImageViewHolder otherUserImageHolder = (OtherUserImageViewHolder) holder;
            setProfileImage(otherUserId, otherUserImageHolder.rightChatImageProfile);
            setImage(model, otherUserImageHolder.rightChatImageView);

        }
    }

    private void handleTextAndLink(TextView textView, String text) {
        SpannableString spannableString = new SpannableString(text);
        ArrayList<UrlString> urls = AndroidUtility.findUrlPatterns(text);
        for(UrlString urlString : urls) {
            final String url = urlString.getUrl();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            };

            spannableString.setSpan(
                    clickableSpan, urlString.getStart(), urlString.getEnd(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setProfileImage(String userId, ImageView chatImageProfile) {
        FirebaseUtility.getProfilePictureByUserId(userId).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtility.setProfilePicture(context, uri, chatImageProfile);
                    }
                });
    }

    private void setImage(ChatMessage model, ImageView chatImageView) {
        FirebaseUtility.getMediaFileOfChatRoomById(model.getChatRoomId(), model.getMediaFileId()).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri uri = task.getResult();
                        AndroidUtility.setImagePicture(context, uri, chatImageView);
                    }
                    else {
                        Log.d("ERROR", task.getException().toString());
                    }
                });
    }

    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout leftChatLayout;
        ImageView leftChatImageProfile;

        TextView leftChatTextview;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            leftChatImageProfile = itemView.findViewById(R.id.left_profile_picture);
        }
    }

    class UserImageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout leftChatLayout;
        ImageView leftChatImageProfile;
        ImageView leftChatImageView;

        public UserImageViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            leftChatImageProfile = itemView.findViewById(R.id.left_profile_picture);
            leftChatImageView = itemView.findViewById(R.id.left_chat_imageview);
        }

    }

    class OtherUserMessageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout rightChatLayout;
        TextView rightChatTextview;
        ImageView rightChatImageProfile;

        public OtherUserMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            rightChatImageProfile = itemView.findViewById(R.id.right_profile_picture);
        }
    }

    class OtherUserImageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout rightChatLayout;
        ImageView rightChatImageProfile;
        ImageView rightChatImageView;

        public OtherUserImageViewHolder(@NonNull View itemView) {
            super(itemView);

            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            rightChatImageProfile = itemView.findViewById(R.id.right_profile_picture);
            rightChatImageView = itemView.findViewById(R.id.right_chat_imageview);
        }
    }
}