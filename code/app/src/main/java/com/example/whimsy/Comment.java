package com.example.whimsy;

import com.google.firebase.Timestamp;

public class Comment {
    private String commenterId;
    private String commenterName;
    private String commentText;
    private Timestamp timestamp;
    private String profileImageUrl;

    // Default constructor required for Firestore
    public Comment() {}

    public Comment(String commenterId, String commenterName, String commentText, Timestamp timestamp,String profileImageUrl) {
        this.commenterId = commenterId;
        this.commenterName = commenterName;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters
    public String getCommenterId() {
        return commenterId;
    }

    public void setCommenterId(String commenterId) {
        this.commenterId = commenterId;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getProfileImageUrl() {
        return profileImageUrl; }
}