package com.example.chatapp.model;

import com.google.firebase.Timestamp;

public class User {
    private String username;
    private String usernameLowercase;
    private String userId;
    private String email;
    private String status;
    private String fcmToken;
    private Timestamp lastActive;
    public static final String USER_ONLINE = "online";
    public static final String USER_OFFLINE = "offline";

    public User() {
    }

    public User(String username, String userId, String email) {
        this.username = username;
        this.userId = userId;
        this.email = email;
        this.usernameLowercase = username.toLowerCase();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.usernameLowercase = username.toLowerCase();
    }

    public Timestamp getLastActive() {
        return lastActive;
    }

    public void setLastActive(Timestamp lastActive) {
        this.lastActive = lastActive;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsernameLowercase() {
        return usernameLowercase;
    }

    public void setUsernameLowercase(String usernameLowercase) {
        this.usernameLowercase = usernameLowercase;
    }
}
