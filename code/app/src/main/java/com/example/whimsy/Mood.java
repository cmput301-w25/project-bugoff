package com.example.whimsy;

import java.io.Serializable;
import java.util.List;

/**
 * <h1>Mood Class</h1>
 * <p>
 * The {@code Mood} class represents a user's mood by encapsulating detailed mood information such as
 * mood status, location, time, gathering status, mood trigger, reason for the mood, and associated images.
 * This class is designed to be serializable and provides accessor methods for all its attributes.
 * </p>
 * <p>
 * <strong>Usage:</strong> Create instances of {@code Mood} to store and retrieve mood-related data.
 * The class includes several constructors for different instantiation scenarios.
 * </p>
 * <p>
 * <strong>Outstanding Issues:</strong>
 * <ul>
 *     <li>The overloaded constructor {@code Mood(String, String, String, String, String, String, String, String, String, String, List<String>, boolean)}
 *         is currently not implemented. It should be either removed or properly implemented.</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Note:</strong> The {@code Mood} class is part of the <em>Project Whimsy</em> and may be subject to further enhancements.
 * </p>
 *
 * @author
 * @version 1.0
 */
public class Mood implements Serializable {
    // Instance variables
    private String userName;          // The name of the user
    private String userId;            // The unique identifier of the user
    private String userLocation;      // The geographical location of the user
    private String userTime;          // The time the mood was recorded
    private String userGatheringStatus; // The user's gathering status (e.g., alone, with friends)
    private String moodStatus;        // The mood of the user (e.g., happy, sad, angry)
    private String moodTrigger;       // The event or circumstance that triggered the mood
    private String moodReason;        // The reason behind the mood
    private String moodImage;         // An optional image representing the mood; may be null
    private String profileImageUrl;   // The URL of the user's profile image
    private List<String> taggedUserNames; // A list of tagged usernames
    private String timestamp;         // The timestamp when the mood was recorded
    private boolean isPrivate = false; // Privacy flag indicating if the mood is private
    private String ownerUid;          // Firebase UID of the mood's owner
    private String moodId;            // Unique identifier for the mood
    private Double locationLat;       // Latitude of the user's location
    private Double locationLng;       // Longitude of the user's location

    /**
     * Constructs a {@code Mood} instance with all attributes specified.
     *
     * @param userName            The name of the user.
     * @param userId              The unique identifier of the user.
     * @param userLocation        The geographical location of the user.
     * @param timestamp           The timestamp when the mood was recorded.
     * @param userTime            The time the mood was recorded.
     * @param userGatheringStatus The gathering status of the user (e.g., alone, with friends).
     * @param moodStatus          The mood of the user (e.g., happy, sad, angry).
     * @param moodTrigger         The trigger event or circumstance for the mood.
     * @param moodReason          The reason behind the mood.
     * @param moodImage           An optional image representing the mood.
     * @param profileImageUrl     The URL of the user's profile image.
     * @param taggedUserNames     A list of tagged usernames.
     * @param isPrivate           The privacy status of the mood.
     */
    public Mood(String userName, String userId, String userLocation, String timestamp, String userTime, String userGatheringStatus, String moodStatus, String moodTrigger, String moodReason, String moodImage, String profileImageUrl, List<String> taggedUserNames, boolean isPrivate) {
        this.userName = userName;
        this.userId = userId;
        this.userLocation = userLocation;
        this.userTime = userTime;
        this.userGatheringStatus = userGatheringStatus;
        this.moodStatus = moodStatus;
        this.moodTrigger = moodTrigger;
        this.moodReason = moodReason;
        this.moodImage = moodImage;
        this.profileImageUrl = profileImageUrl;
        this.taggedUserNames = taggedUserNames;
        this.timestamp = timestamp;
        this.isPrivate = isPrivate;
    }

    /**
     * Default no-argument constructor.
     * <p>
     * Creates an uninitialized {@code Mood} instance. This constructor is useful for frameworks that require
     * a no-argument constructor for object creation and subsequent field initialization.
     * </p>
     */
    public Mood() {
    }

    /**
     * Overloaded constructor with generic parameter names.
     * <p>
     * <strong>Outstanding Issue:</strong> This constructor is not implemented.
     * It is recommended to either remove this constructor or implement it properly.
     * </p>
     *
     * @param s                A string representing the user's name.
     * @param s1               A string representing the user's ID.
     * @param s2               A string representing the user's location.
     * @param timestampStr     A string representing the timestamp.
     * @param gatheringStatus  A string representing the user's gathering status.
     * @param s3               A string representing the mood status.
     * @param trigger          A string representing the mood trigger.
     * @param reason           A string representing the reason behind the mood.
     * @param imageUrl         A string representing the URL of the mood image.
     * @param profileImageUrl  A string representing the URL of the user's profile image.
     * @param taggedUserNames  A list of tagged usernames.
     * @param isPrivate        A boolean indicating whether the mood is private.
     */
    public Mood(String s, String s1, String s2, String timestampStr, String gatheringStatus, String s3, String trigger, String reason, String imageUrl, String profileImageUrl, List<String> taggedUserNames, boolean isPrivate) {
        // TODO: Implement this constructor or remove if not required.
    }

    /**
     * Retrieves the list of tagged user names.
     *
     * @return A {@code List<String>} containing the tagged user names.
     */
    public List<String> getTaggedUserNames() {
        return taggedUserNames;
    }

    /**
     * Sets the list of tagged user names.
     *
     * @param taggedUserNames A {@code List<String>} containing the tagged user names.
     */
    public void setTaggedUserNames(List<String> taggedUserNames) {
        this.taggedUserNames = taggedUserNames;
    }

    /**
     * Retrieves the URL of the user's profile image.
     *
     * @return A {@code String} representing the URL of the profile image.
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the URL of the user's profile image.
     *
     * @param profileImageUrl A {@code String} containing the URL of the profile image.
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Retrieves the latitude of the user's location.
     *
     * @return A {@code Double} representing the latitude.
     */
    public Double getLocationLat() {
        return locationLat;
    }

    /**
     * Sets the latitude of the user's location.
     *
     * @param locationLat A {@code Double} containing the latitude.
     */
    public void setLocationLat(Double locationLat) {
        this.locationLat = locationLat;
    }

    /**
     * Retrieves the longitude of the user's location.
     *
     * @return A {@code Double} representing the longitude.
     */
    public Double getLocationLng() {
        return locationLng;
    }

    /**
     * Sets the longitude of the user's location.
     *
     * @param locationLng A {@code Double} containing the longitude.
     */
    public void setLocationLng(Double locationLng) {
        this.locationLng = locationLng;
    }

    /**
     * Retrieves the name of the user.
     *
     * @return A {@code String} containing the user's name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the name of the user.
     *
     * @param userName A {@code String} containing the user's name.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Retrieves the unique identifier of the user.
     *
     * @return A {@code String} containing the user's ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique identifier of the user.
     *
     * @param userId A {@code String} containing the user's ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the geographical location of the user.
     *
     * @return A {@code String} representing the user's location.
     */
    public String getUserLocation() {
        return userLocation;
    }

    /**
     * Sets the geographical location of the user.
     *
     * @param userLocation A {@code String} representing the user's location.
     */
    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    /**
     * Retrieves the time when the mood was recorded.
     *
     * @return A {@code String} representing the time the mood was recorded.
     */
    public String getUserTime() {
        return userTime;
    }

    /**
     * Sets the time when the mood was recorded.
     *
     * @param userTime A {@code String} containing the time the mood was recorded.
     */
    public void setUserTime(String userTime) {
        this.userTime = userTime;
    }

    /**
     * Retrieves the gathering status of the user.
     *
     * @return A {@code String} representing the gathering status (e.g., alone, with friends).
     */
    public String getUserGatheringStatus() {
        return userGatheringStatus;
    }

    /**
     * Sets the gathering status of the user.
     *
     * @param userGatheringStatus A {@code String} representing the gathering status.
     */
    public void setUserGatheringStatus(String userGatheringStatus) {
        this.userGatheringStatus = userGatheringStatus;
    }

    /**
     * Retrieves the mood status of the user.
     *
     * @return A {@code String} representing the mood status (e.g., happy, sad, angry).
     */
    public String getMoodStatus() {
        return moodStatus;
    }

    /**
     * Sets the mood status of the user.
     *
     * @param moodStatus A {@code String} representing the mood status.
     */
    public void setMoodStatus(String moodStatus) {
        this.moodStatus = moodStatus;
    }

    /**
     * Retrieves the mood trigger event or circumstance.
     *
     * @return A {@code String} representing the mood trigger.
     */
    public String getMoodTrigger() {
        return moodTrigger;
    }

    /**
     * Sets the mood trigger event or circumstance.
     *
     * @param moodTrigger A {@code String} representing the mood trigger.
     */
    public void setMoodTrigger(String moodTrigger) {
        this.moodTrigger = moodTrigger;
    }

    /**
     * Retrieves the reason behind the mood.
     *
     * @return A {@code String} representing the reason behind the mood.
     */
    public String getMoodReason() {
        return moodReason;
    }

    /**
     * Sets the reason behind the mood.
     *
     * @param moodReason A {@code String} containing the reason behind the mood.
     */
    public void setMoodReason(String moodReason) {
        this.moodReason = moodReason;
    }

    /**
     * Retrieves the image associated with the mood.
     *
     * @return A {@code String} representing the mood image, or {@code null} if not set.
     */
    public String getMoodImage() {
        return moodImage;
    }

    /**
     * Sets the image associated with the mood.
     *
     * @param moodImage A {@code String} representing the mood image.
     */
    public void setMoodImage(String moodImage) {
        this.moodImage = moodImage;
    }

    /**
     * Retrieves the timestamp of the mood.
     *
     * @return A {@code String} representing the timestamp when the mood was recorded.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if the mood is marked as private.
     *
     * @return {@code true} if the mood is private; {@code false} otherwise.
     */
    public boolean isPrivateMood() {
        return isPrivate;
    }

    /**
     * Sets the privacy status of the mood.
     *
     * @param privateMood {@code true} to mark the mood as private; {@code false} otherwise.
     */
    public void setPrivate(boolean privateMood) {
        this.isPrivate = privateMood;
    }

    /**
     * Retrieves the Firebase UID of the mood's owner.
     *
     * @return A {@code String} representing the Firebase UID of the mood's owner.
     */
    public String getOwnerUid() {
        return ownerUid;
    }

    /**
     * Sets the Firebase UID of the mood's owner.
     *
     * @param ownerUid A {@code String} containing the Firebase UID of the mood's owner.
     */
    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    /**
     * Retrieves the unique identifier of the mood.
     *
     * @return A {@code String} representing the unique mood identifier.
     */
    public String getMoodId() {
        return moodId;
    }

    /**
     * Sets the unique identifier of the mood.
     *
     * @param moodId A {@code String} containing the unique mood identifier.
     */
    public void setMoodId(String moodId) {
        this.moodId = moodId;
    }

    /**
     * Checks if the mood is private.
     *
     * @return {@code true} if the mood is private; {@code false} otherwise.
     */
    public boolean isPrivate() {
        return isPrivate;
    }
}
