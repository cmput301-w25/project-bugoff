package com.example.whimsy;

import java.io.Serializable;
import java.util.List;

/**
 * The Mood class represents the mood of a user, storing information about
 * the user's mood status, location, time, and other related attributes.
 * This class is used to encapsulate a user's mood data, including their
 * name, ID, gathering status, mood trigger, reason for mood, and an optional image.
 * It provides getter methods for accessing these attributes.
 * Outstanding Issues:
 * - None identified.
 */
public class Mood implements Serializable {
    private String userName;          // The name of the user
    private String userId;            // The ID of the user
    private String userLocation;      // The user's location
    private String userTime;          // The time the mood was recorded
    private String userGatheringStatus; // The user's gathering status (e.g., alone, with friends)
    private String moodStatus;        // The user's mood (e.g., happy, sad, angry)
    private String moodTrigger;       // The trigger for the mood (e.g., event, circumstance)
    private String moodReason;        // The reason behind the mood (e.g., feeling lonely)
    private String moodImage;         // An image representing the mood (can be null)
    private String profileImageUrl;
    private List<String> taggedUserNames;
    private String timestamp;
    private boolean isPrivate = false;
    private String ownerUid;          // Firebase UID of the mood's owner
    private String moodId;
    private Double locationLat;
    private Double locationLng;

    /**
     * Constructor for initializing the Mood object with all its attributes.
     *
     * @param userName          The name of the user
     * @param userId            The ID of the user
     * @param userLocation      The user's location
     * @param userTime          The time the mood was recorded
     * @param userGatheringStatus The gathering status of the user
     * @param moodStatus        The user's mood
     * @param moodTrigger       The trigger for the mood
     * @param moodReason        The reason behind the mood
     * @param moodImage         An optional image representing the mood
     * @param profileImageUrl   The URL of the user's profile image
     * @param taggedUserNames   The list of tagged user names
     * @param isPrivate         The privacy status of the mood
     * @param timestamp         The timestamp of the mood
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

    public Mood() {
    }

    public Mood(String s, String s1, String s2, String timestampStr, String gatheringStatus, String s3, String trigger, String reason, String imageUrl, String profileImageUrl, List<String> taggedUserNames, boolean isPrivate) {
    }

    /**
     * Returns the list of tagged user names.
     *
     * @return The list of tagged user names.
     */
    public List<String> getTaggedUserNames() {
        return taggedUserNames;
    }

    /**
     * Sets the list of tagged user names.
     *
     * @param taggedUserNames The list of tagged user names.
     */
    public void setTaggedUserNames(List<String> taggedUserNames) {
        this.taggedUserNames = taggedUserNames;
    }

    /**
     * Returns the URL of the user's profile image.
     *
     * @return The URL of the user's profile image.
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the URL of the user's profile image.
     *
     * @param profileImageUrl The URL of the user's profile image.
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Returns the latitude of the location.
     *
     * @return The latitude of the location.
     */
    public Double getLocationLat() {
        return locationLat;
    }

    /**
     * Sets the latitude of the location.
     *
     * @param locationLat The latitude of the location.
     */
    public void setLocationLat(Double locationLat) {
        this.locationLat = locationLat;
    }

    /**
     * Returns the longitude of the location.
     *
     * @return The longitude of the location.
     */
    public Double getLocationLng() {
        return locationLng;
    }

    /**
     * Sets the longitude of the location.
     *
     * @param locationLng The longitude of the location.
     */
    public void setLocationLng(Double locationLng) {
        this.locationLng = locationLng;
    }

    /**
     * Returns the name of the user.
     *
     * @return The name of the user.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the name of the user.
     *
     * @param userName The name of the user.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the ID of the user.
     *
     * @return The ID of the user.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user.
     *
     * @param userId The ID of the user.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the location of the user.
     *
     * @return The location of the user.
     */
    public String getUserLocation() {
        return userLocation;
    }

    /**
     * Sets the location of the user.
     *
     * @param userLocation The location of the user.
     */
    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    /**
     * Returns the time when the mood was recorded.
     *
     * @return The time the mood was recorded.
     */
    public String getUserTime() {
        return userTime;
    }

    /**
     * Sets the time when the mood was recorded.
     *
     * @param userTime The time the mood was recorded.
     */
    public void setUserTime(String userTime) {
        this.userTime = userTime;
    }

    /**
     * Returns the gathering status of the user (e.g., alone, with friends).
     *
     * @return The gathering status of the user.
     */
    public String getUserGatheringStatus() {
        return userGatheringStatus;
    }

    /**
     * Sets the gathering status of the user.
     *
     * @param userGatheringStatus The gathering status of the user.
     */
    public void setUserGatheringStatus(String userGatheringStatus) {
        this.userGatheringStatus = userGatheringStatus;
    }

    /**
     * Returns the user's mood status (e.g., happy, sad, angry).
     *
     * @return The user's mood status.
     */
    public String getMoodStatus() {
        return moodStatus;
    }

    /**
     * Sets the user's mood status.
     *
     * @param moodStatus The user's mood status.
     */
    public void setMoodStatus(String moodStatus) {
        this.moodStatus = moodStatus;
    }

    /**
     * Returns the trigger that caused the user's mood.
     *
     * @return The trigger for the mood.
     */
    public String getMoodTrigger() {
        return moodTrigger;
    }

    /**
     * Sets the trigger that caused the user's mood.
     *
     * @param moodTrigger The trigger for the mood.
     */
    public void setMoodTrigger(String moodTrigger) {
        this.moodTrigger = moodTrigger;
    }

    /**
     * Returns the reason behind the user's mood.
     *
     * @return The reason behind the user's mood.
     */
    public String getMoodReason() {
        return moodReason;
    }

    /**
     * Sets the reason behind the user's mood.
     *
     * @param moodReason The reason behind the user's mood.
     */
    public void setMoodReason(String moodReason) {
        this.moodReason = moodReason;
    }

    /**
     * Returns the image associated with the user's mood.
     *
     * @return The image representing the mood (may be null).
     */
    public String getMoodImage() {
        return moodImage;
    }

    /**
     * Sets the image associated with the user's mood.
     *
     * @param moodImage The image representing the mood.
     */
    public void setMoodImage(String moodImage) {
        this.moodImage = moodImage;
    }

    /**
     * Returns the timestamp of the mood.
     *
     * @return The timestamp of the mood.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Returns whether the mood is private.
     *
     * @return True if the mood is private, false otherwise.
     */
    public boolean isPrivateMood() {
        return isPrivate;
    }

    /**
     * Sets the privacy status of the mood.
     *
     * @param privateMood The privacy status of the mood.
     */
    public void setPrivate(boolean privateMood) {
        this.isPrivate = privateMood;
    }

    /**
     * Returns the Firebase UID of the mood's owner.
     *
     * @return The Firebase UID of the mood's owner.
     */
    public String getOwnerUid() {
        return ownerUid;
    }

    /**
     * Sets the Firebase UID of the mood's owner.
     *
     * @param ownerUid The Firebase UID of the mood's owner.
     */
    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    /**
     * Returns the unique identifier of the mood.
     *
     * @return The unique identifier of the mood.
     */
    public String getMoodId() {
        return moodId;
    }

    /**
     * Sets the unique identifier of the mood.
     *
     * @param moodId The unique identifier of the mood.
     */
    public void setMoodId(String moodId) {
        this.moodId = moodId;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}