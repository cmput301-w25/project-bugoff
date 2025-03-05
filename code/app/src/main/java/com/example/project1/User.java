package com.example.project1;

public class User {
    private String id;
    private String username;
    private String displayName;

    // Required empty constructor for Firestore
    public User() {}

    public User(String id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getDisplayName() {
        return displayName;
    }
}