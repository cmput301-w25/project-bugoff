/**
 * User represents a user in the application.
 * This class is used to store the user details, such as name, username,
 * profile picture URL, and Gmail, as well as their corresponding getter and setter methods.
 * The class also includes constructors for Firestore deserialization and user creation.
 */
package com.example.whimsy;

public class User {
    private String id; // Firestore document ID
    private String name; // The full name of the user
    private String username; // The username chosen by the user
    private String profilePictureUrl; // URL of the user's profile picture (optional)
    private String gmail; // The Gmail address of the user

    /**
     * Default constructor required for Firestore deserialization.
     * This constructor is necessary for Firestore to convert document data into a User object.
     */
    public User() {
    }

    /**
     * Constructor to create a User object with an id, name, username, and profile picture URL.
     *
     * @param id The unique identifier for the user (Firestore document ID).
     * @param name The full name of the user.
     * @param username The username chosen by the user.
     * @param profilePictureUrl The URL of the user's profile picture (optional).
     */
    public User(String id, String name, String username, String profilePictureUrl) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.profilePictureUrl = profilePictureUrl;
    }

    /**
     * Constructor to create a User object with an id, username, and display name (without profile picture).
     *
     * @param id The unique identifier for the user.
     * @param username The username chosen by the user.
     * @param displayName The full name of the user.
     */
    public User(String id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.name = displayName;
    }

    // Getter and Setter methods

    /**
     * Gets the Firestore document ID of the user.
     *
     * @return The user's document ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the Firestore document ID of the user.
     *
     * @param id The user's document ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the full name of the user.
     *
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the full name of the user.
     *
     * @param name The user's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the username of the user.
     *
     * @return The user's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The user's username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the profile picture URL of the user.
     *
     * @return The user's profile picture URL.
     */
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    /**
     * Sets the profile picture URL of the user.
     *
     * @param profilePictureUrl The user's profile picture URL.
     */
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    /**
     * Gets the Gmail address of the user.
     *
     * @return The user's Gmail address.
     */
    public String getGmail() {
        return gmail;
    }

    /**
     * Sets the Gmail address of the user.
     *
     * @param gmail The user's Gmail address.
     */
    public void setGmail(String gmail) {
        this.gmail = gmail;
    }
}
