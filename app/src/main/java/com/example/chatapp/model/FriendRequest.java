package com.example.chatapp.model;

import com.google.firebase.Timestamp;

public class FriendRequest {
    private String id;
    private String fromUserId;
    private String toUserId;
    private Timestamp createdTimestamp;

    public FriendRequest() {
    }

    public FriendRequest(String id, String fromUserId, String toUserId, Timestamp createdTimestamp) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.createdTimestamp = createdTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }
}
