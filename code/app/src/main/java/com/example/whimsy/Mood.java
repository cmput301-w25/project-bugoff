/**
 * The Mood class represents the mood of a user, storing information about
 * the user's mood status, location, time, and other related attributes.
 *
 * This class is used to encapsulate a user's mood data, including their
 * name, ID, gathering status, mood trigger, reason for mood, and an optional image.
 * It provides getter methods for accessing these attributes.
 *
 * Outstanding Issues:
 * - None identified.
 */
package com.example.whimsy;
import java.io.Serializable;
import java.util.List;

public class Mood implements Serializable {
    private String userName;          // The name of the user
    private String userId;            // The ID of the user
    private String userLocation;      // The user's location
    private String userTime;          // The time the mood was recorded
    private String userGatheringStatus; // The user's gathering status (e.g., alone, with friends)
    private String moodStatus;        // The user's mood (e.g., happy, sad, angry)
    private String moodTrigger;       // The trigger for the mood (e.g., event, circumstance)
    private String moodReason;        // The reason behind the mood (e.g., feeling lonely)
    private String moodImage;        // An image representing the mood (can be null)
    private String profileImageUrl;
    private List<String> taggedUserNames;
    private String timestamp;
    private boolean isPrivate = false;
    private String ownerUid;          // Firebase UID of the mood's owner
    private String moodId;

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

    public List<String> getTaggedUserNames() {
        return taggedUserNames;
    }
    public void setTaggedUserNames(List<String> taggedUserNames) {
        this.taggedUserNames = taggedUserNames;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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
     * Returns the ID of the user.
     *
     * @return The ID of the user.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    /**
     * Returns the location of the user.
     *
     * @return The location of the user.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserLocation() {
        return userLocation;
    }

    /**
     * Returns the time when the mood was recorded.
     *
     * @return The time the mood was recorded.
     */
    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getUserTime() {
        return userTime;
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
     * Returns the user's mood status (e.g., happy, sad, angry).
     *
     * @return The user's mood status.
     */
    public void setUserGatheringStatus(String userGatheringStatus) {
        this.userGatheringStatus = userGatheringStatus;
    }

    public String getMoodStatus() {
        return moodStatus;
    }

    /**
     * Returns the trigger that caused the user's mood.
     *
     * @return The trigger for the mood.
     */
    public void setMoodStatus(String moodStatus) {
        this.moodStatus = moodStatus;
    }


    public String getMoodTrigger() {
        return moodTrigger;
    }

    /**
     * Returns the reason behind the user's mood.
     *
     * @return The reason behind the user's mood.
     */
    public void setMoodTrigger(String moodTrigger) {
        this.moodTrigger = moodTrigger;
    }

    public String getMoodReason() {
        return moodReason;
    }

    /**
     * Returns the image associated with the user's mood.
     *
     * @return The image representing the mood (may be null).
     */
    public void setMoodReason(String moodReason) {
        this.moodReason = moodReason;
    }

    public String getMoodImage() {
        return moodImage;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setMoodImage(String moodImage) {
        this.moodImage = moodImage;
    }
    public boolean isPrivateMood() {
        return isPrivate;
    }

    public void setPrivate(boolean privateMood) {
        this.isPrivate = privateMood;
    }
    public String getOwnerUid() { return ownerUid; }
    public void setOwnerUid(String ownerUid) { this.ownerUid = ownerUid; }
    public String getMoodId() { return moodId; }
    public void setMoodId(String moodId) { this.moodId = moodId; }
}