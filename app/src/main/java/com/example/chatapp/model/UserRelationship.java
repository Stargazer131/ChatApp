package com.example.chatapp.model;

import java.util.List;

public class UserRelationship {
    private String id;
    private List<String> userIds;
    private String type;
    public static final String FRIEND = "friend";
    public static final String BLOCKED = "blocked";

    public UserRelationship() {
    }

    public UserRelationship(String id, List<String> userIds, String type) {
        this.id = id;
        this.userIds = userIds;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
