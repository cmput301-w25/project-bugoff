package com.example.project1;

public class User {
    private String id; // Firestore document ID
    private String name;
    private String username;
    private String profilePictureUrl; // Optional, can be removed if not needed

    // Default constructor required for Firestore deserialization
    public User() {
    }

    public User(String id, String name, String username, String profilePictureUrl) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}