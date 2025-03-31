/**
 * The {@code Comment} class represents a user-generated comment in the application.
 * Each comment is associated with a user and includes metadata such as the commenter's ID,
 * name, text, timestamp, and profile image URL. Comments are stored in Firebase Firestore.
 *
 * Key Features:
 *
 *     Stores user-generated comments with associated metadata.
 *     Includes Firestore-compatible timestamp for chronological sorting.
 *     Provides getters and setters for flexible data manipulation.
 *     Supports serialization and deserialization for Firestore compatibility.
 *     Enables retrieval of the commenter's profile image for UI display.
 */

package com.example.whimsy;

import com.google.firebase.Timestamp;

/**
 * Represents a comment made by a user.
 */
public class Comment {
    private String commenterId;
    private String commenterName;
    private String commentText;
    private Timestamp timestamp;
    private String profileImageUrl;

    /**
     * Default constructor required for Firestore.
     */
    public Comment() {}

    /**
     * Constructs a new Comment with the specified details.
     *
     * @param commenterId The ID of the commenter.
     * @param commenterName The name of the commenter.
     * @param commentText The text of the comment.
     * @param timestamp The timestamp of when the comment was made.
     * @param profileImageUrl The URL of the commenter's profile image.
     */
    public Comment(String commenterId, String commenterName, String commentText, Timestamp timestamp, String profileImageUrl) {
        this.commenterId = commenterId;
        this.commenterName = commenterName;
        this.commentText = commentText;
        this.timestamp = timestamp;
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Gets the ID of the commenter.
     *
     * @return The ID of the commenter.
     */
    public String getCommenterId() {
        return commenterId;
    }

    /**
     * Sets the ID of the commenter.
     *
     * @param commenterId The ID of the commenter.
     */
    public void setCommenterId(String commenterId) {
        this.commenterId = commenterId;
    }

    /**
     * Gets the name of the commenter.
     *
     * @return The name of the commenter.
     */
    public String getCommenterName() {
        return commenterName;
    }

    /**
     * Sets the name of the commenter.
     *
     * @param commenterName The name of the commenter.
     */
    public void setCommenterName(String commenterName) {
        this.commenterName = commenterName;
    }

    /**
     * Gets the text of the comment.
     *
     * @return The text of the comment.
     */
    public String getCommentText() {
        return commentText;
    }

    /**
     * Sets the text of the comment.
     *
     * @param commentText The text of the comment.
     */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * Gets the timestamp of when the comment was made.
     *
     * @return The timestamp of the comment.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of when the comment was made.
     *
     * @param timestamp The timestamp of the comment.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the URL of the commenter's profile image.
     *
     * @return The URL of the commenter's profile image.
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the URL of the commenter's profile image.
     *
     * @param profileImageUrl The URL of the commenter's profile image.
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}