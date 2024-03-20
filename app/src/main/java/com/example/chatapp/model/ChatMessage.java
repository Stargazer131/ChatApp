package com.example.chatapp.model;

import android.util.Log;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String message;
    private String senderId;
    private Timestamp timestamp;
    private String chatRoomId;
    private String type;
    private String mediaFileId;

    public ChatMessage() {
    }

    public ChatMessage(String message, String senderId, Timestamp timestamp, String chatRoomId, String type, String mediaFileId) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.chatRoomId = chatRoomId;
        this.type = type;
        this.mediaFileId = mediaFileId;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaFileId() {
        return mediaFileId;
    }

    public void setMediaFileId(String mediaFileId) {
        this.mediaFileId = mediaFileId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "message='" + message + '\'' +
                ", senderId='" + senderId + '\'' +
                ", timestamp=" + timestamp +
                ", chatRoomId='" + chatRoomId + '\'' +
                ", type='" + type + '\'' +
                ", mediaFileId='" + mediaFileId + '\'' +
                '}';
    }

    public void log() {
        // Log all fields using getter methods
        Log.d("MyApp", "Message: " + getMessage());
        Log.d("MyApp", "Sender ID: " + getSenderId());
        Log.d("MyApp", "Timestamp: " + getTimestamp().toString());
        Log.d("MyApp", "Chat Room ID: " + getChatRoomId());
        Log.d("MyApp", "Type: " + getType());
        Log.d("MyApp", "Media File ID: " + getMediaFileId());
    }
}
