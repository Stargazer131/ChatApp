package com.example.chatapp.adapter;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.ChatRoom;
import com.example.chatapp.utility.AndroidUtility;
import com.example.chatapp.utility.FirebaseUtility;
import com.example.chatapp.utility.UrlString;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class ChatRoomRecyclerAdapter extends
        FirestoreRecyclerAdapter<ChatMessage, RecyclerView.ViewHolder> {
    private static final int TYPE_USER_MESSAGE = 0;
    private static final int TYPE_USER_IMAGE = 2;
    private static final int TYPE_USER_VIDEO = 4;
    private static final int TYPE_USER_FILE = 6;

    private static final int TYPE_OTHER_USER_MESSAGE = 1;
    private static final int TYPE_OTHER_USER_IMAGE = 3;
    private static final int TYPE_OTHER_USER_VIDEO = 5;
    private static final int TYPE_OTHER_USER_FILE = 7;

    private Context context;
    private String currentUserId;
    private String otherUserId;
    private String chatRoomId;


    public ChatRoomRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessage> options, Context context, String otherUserId) {
        super(options);
        this.context = context;
        this.otherUserId = otherUserId;
        this.currentUserId = FirebaseUtility.getCurrentUserId();
        this.chatRoomId = FirebaseUtility.getChatRoomId(this.currentUserId, this.otherUserId);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_USER_MESSAGE) {
            View userMessageView = inflater.inflate(R.layout.user_message_recycler_row, parent, false);
            return new UserMessageViewHolder(userMessageView);

        } else if (viewType == TYPE_OTHER_USER_MESSAGE) {
            View otherUserMessageView = inflater.inflate(R.layout.other_user_message_recycler_row, parent, false);
            return new OtherUserMessageViewHolder(otherUserMessageView);

        } else if (viewType == TYPE_USER_IMAGE) {
            View userImageView = inflater.inflate(R.layout.user_image_recycler_row, parent, false);
            return new UserImageViewHolder(userImageView);

        } else if (viewType == TYPE_OTHER_USER_IMAGE) {
            View otherUserImageView = inflater.inflate(R.layout.other_user_image_recycler_row, parent, false);
            return new OtherUserImageViewHolder(otherUserImageView);

        } else if (viewType == TYPE_USER_VIDEO) {
            View userVideoView = inflater.inflate(R.layout.user_video_recycler_row, parent, false);
            return new UserVideoViewHolder(userVideoView);

        } else if (viewType == TYPE_OTHER_USER_VIDEO) {
            View otherUserVideoView = inflater.inflate(R.layout.other_user_video_recycler_row, parent, false);
            return new OtherUserVideoViewHolder(otherUserVideoView);

        } else if (viewType == TYPE_USER_FILE) { // USE FOR BOTH FILE AND MESSAGE
            View userMessageView = inflater.inflate(R.layout.user_message_recycler_row, parent, false);
            return new UserMessageViewHolder(userMessageView);

        } else { // USE FOR BOTH FILE AND MESSAGE
            View otherUserMessageView = inflater.inflate(R.layout.other_user_message_recycler_row, parent, false);
            return new OtherUserMessageViewHolder(otherUserMessageView);

        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage model = getItem(position);

        if (model.getSenderId().equals(currentUserId)) {
            switch (model.getType()) {
                case ChatActivity.MESSAGE_TYPE_STRING:
                    return TYPE_USER_MESSAGE;
                case ChatActivity.MESSAGE_TYPE_IMAGE:
                    return TYPE_USER_IMAGE;
                case ChatActivity.MESSAGE_TYPE_VIDEO:
                    return TYPE_USER_VIDEO;
                default:
                    return TYPE_USER_FILE;
            }
        } else {
            switch (model.getType()) {
                case ChatActivity.MESSAGE_TYPE_STRING:
                    return TYPE_OTHER_USER_MESSAGE;
                case ChatActivity.MESSAGE_TYPE_IMAGE:
                    return TYPE_OTHER_USER_IMAGE;
                case ChatActivity.MESSAGE_TYPE_VIDEO:
                    return TYPE_OTHER_USER_VIDEO;
                default:
                    return TYPE_OTHER_USER_FILE;
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

        } else if (viewType == TYPE_OTHER_USER_MESSAGE) {
            OtherUserMessageViewHolder otherUserMessageHolder = (OtherUserMessageViewHolder) holder;
            setProfileImage(otherUserId, otherUserMessageHolder.rightChatImageProfile);
            handleTextAndLink(otherUserMessageHolder.rightChatTextview, model.getMessage());

        } else if (viewType == TYPE_USER_IMAGE) {
            UserImageViewHolder userImageHolder = (UserImageViewHolder) holder;
            setProfileImage(currentUserId, userImageHolder.leftChatImageProfile);
            setImage(model, userImageHolder.leftChatImageView);
            userImageHolder.leftChatImageView.setOnLongClickListener(v -> {
                setUpChatMessageItemPopupMenu(userImageHolder.leftChatImageView, model);
                return true;
            });

        } else if (viewType == TYPE_OTHER_USER_IMAGE) {
            OtherUserImageViewHolder otherUserImageHolder = (OtherUserImageViewHolder) holder;
            setProfileImage(otherUserId, otherUserImageHolder.rightChatImageProfile);
            setImage(model, otherUserImageHolder.rightChatImageView);
            otherUserImageHolder.rightChatImageView.setOnLongClickListener(v -> {
                setUpChatMessageItemPopupMenu(otherUserImageHolder.rightChatImageView, model);
                return true;
            });

        } else if (viewType == TYPE_USER_VIDEO) {
            UserVideoViewHolder userVideoHolder = (UserVideoViewHolder) holder;
            setProfileImage(currentUserId, userVideoHolder.leftChatImageProfile);
            setVideoResource(model, userVideoHolder.leftChatVideoView);
            userVideoHolder.leftChatVideoView.setOnLongClickListener(v -> {
                setUpChatMessageItemPopupMenu(userVideoHolder.leftChatVideoView, model);
                return true;
            });

        } else if (viewType == TYPE_OTHER_USER_VIDEO) {
            OtherUserVideoViewHolder otherUserVideoHolder = (OtherUserVideoViewHolder) holder;
            setProfileImage(otherUserId, otherUserVideoHolder.rightChatImageProfile);
            setVideoResource(model, otherUserVideoHolder.rightChatVideoView);
            otherUserVideoHolder.rightChatVideoView.setOnLongClickListener(v -> {
                setUpChatMessageItemPopupMenu(otherUserVideoHolder.rightChatVideoView, model);
                return true;
            });

        } else if (viewType == TYPE_USER_FILE) {
            UserMessageViewHolder userMessageHolder = (UserMessageViewHolder) holder;
            setProfileImage(currentUserId, userMessageHolder.leftChatImageProfile);
            handleTextDownloadFile(userMessageHolder.leftChatTextview, model);

        } else {
            OtherUserMessageViewHolder otherUserMessageHolder = (OtherUserMessageViewHolder) holder;
            setProfileImage(otherUserId, otherUserMessageHolder.rightChatImageProfile);
            handleTextDownloadFile(otherUserMessageHolder.rightChatTextview, model);

        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof UserVideoViewHolder) {
            UserVideoViewHolder userVideoViewHolder = (UserVideoViewHolder) holder;
            releasePlayer(userVideoViewHolder.leftChatVideoView);

        } else if (holder instanceof OtherUserVideoViewHolder) {
            OtherUserVideoViewHolder otherUserImageViewHolder = (OtherUserVideoViewHolder) holder;
            releasePlayer(otherUserImageViewHolder.rightChatVideoView);
        }
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        Log.d("CHAT_ROOM_ADAPTER", "DATA HAS CHANGED");
        FirebaseUtility.getChatRoomById(chatRoomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ChatRoom chatRoom = task.getResult().toObject(ChatRoom.class);
                String senderId = chatRoom.getLastMessageSenderId();
                if (senderId != null && !senderId.equals(currentUserId)) {
                    chatRoom.setLastMessageStatus(ChatRoom.STATUS_SEEN);
                    FirebaseUtility.getChatRoomById(chatRoomId).set(chatRoom);
                }
            }
        });
    }

    private void setUpChatMessageItemPopupMenu(View view, ChatMessage model) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.pop_up_chat_message_item_menu, popupMenu.getMenu());
        popupMenu.setForceShowIcon(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_item_download) {
                downloadFile(model);
            }
            return true;
        });

        popupMenu.show();
    }

    private void handleTextDownloadFile(TextView textView, ChatMessage model) {
        String text = model.getMessage();
        SpannableString spannableString = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                try {
                    downloadFile(model);
                } catch (Exception ignored) {

                }
            }
        };

        spannableString.setSpan(
                clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void downloadFile(ChatMessage model) {
        FirebaseUtility.getMediaFileOfChatRoomById(model.getChatRoomId(), model.getMediaFileId())
                .getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri fileUri = task.getResult();
                        DownloadManager.Request request = new DownloadManager.Request(fileUri);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, model.getMessage());
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        long downloadId = downloadManager.enqueue(request);

                    } else {
                        AndroidUtility.showToast(context, "Download failed");
                    }
                });
    }

    private void setVideoResource(ChatMessage model, PlayerView videoView) {
        FirebaseUtility.getMediaFileOfChatRoomById(model.getChatRoomId(), model.getMediaFileId()).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri videoUri = task.getResult();
                        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, "user-agent");
                        MediaItem mediaItem = MediaItem.fromUri(videoUri);
                        final MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(mediaItem);
                        initializePlayer(videoView, mediaSource);

                    } else {
                        Log.d("SET_VIDEO_RESOURCE_ERROR", task.getException().toString());
                    }
                });
    }


    private void initializePlayer(PlayerView videoView, MediaSource mediaSource) {
        TrackSelector trackSelector = new DefaultTrackSelector(context);
        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(context, renderersFactory).setTrackSelector(trackSelector).build();
        videoView.setPlayer(player);
        player.prepare(mediaSource);
    }

    private void releasePlayer(PlayerView videoView) {
        SimpleExoPlayer player = (SimpleExoPlayer) videoView.getPlayer();
        if (player != null) {
            player.release();
            videoView.setPlayer(null);
        }
    }

    private void handleTextAndLink(TextView textView, String text) {
        SpannableString spannableString = new SpannableString(text);
        ArrayList<UrlString> urls = AndroidUtility.findUrlPatterns(text);
        for (UrlString urlString : urls) {
            final String url = urlString.getUrl();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    try {
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception ignored) {
                        AndroidUtility.showToast(context, "Invalid link");
                    }
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
                    } else {
                        Log.d("ERROR", task.getException().toString());
                    }
                });
    }

    // USE FOR BOTH FILE AND MESSAGE
    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView leftChatImageProfile;

        TextView leftChatTextview;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            leftChatImageProfile = itemView.findViewById(R.id.left_profile_picture);
        }
    }

    // USE FOR BOTH FILE AND MESSAGE
    class OtherUserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView rightChatTextview;
        ImageView rightChatImageProfile;

        public OtherUserMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            rightChatImageProfile = itemView.findViewById(R.id.right_profile_picture);
        }
    }

    class UserImageViewHolder extends RecyclerView.ViewHolder {
        ImageView leftChatImageProfile;
        ImageView leftChatImageView;

        public UserImageViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatImageProfile = itemView.findViewById(R.id.left_profile_picture);
            leftChatImageView = itemView.findViewById(R.id.left_chat_imageview);
        }

    }


    class OtherUserImageViewHolder extends RecyclerView.ViewHolder {
        ImageView rightChatImageProfile;
        ImageView rightChatImageView;

        public OtherUserImageViewHolder(@NonNull View itemView) {
            super(itemView);

            rightChatImageProfile = itemView.findViewById(R.id.right_profile_picture);
            rightChatImageView = itemView.findViewById(R.id.right_chat_imageview);
        }
    }

    class OtherUserVideoViewHolder extends RecyclerView.ViewHolder {
        ImageView rightChatImageProfile;
        PlayerView rightChatVideoView;

        public OtherUserVideoViewHolder(@NonNull View itemView) {
            super(itemView);

            rightChatImageProfile = itemView.findViewById(R.id.right_profile_picture);
            rightChatVideoView = itemView.findViewById(R.id.right_chat_videoview);
        }

    }

    class UserVideoViewHolder extends RecyclerView.ViewHolder {
        ImageView leftChatImageProfile;
        PlayerView leftChatVideoView;

        public UserVideoViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatImageProfile = itemView.findViewById(R.id.left_profile_picture);
            leftChatVideoView = itemView.findViewById(R.id.left_chat_videoview);
        }

    }
}