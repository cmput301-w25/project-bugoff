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
package com.example.project1;

public class Mood {

    private String userName;          // The name of the user
    private String userId;            // The ID of the user
    private String userLocation;      // The user's location
    private String userTime;          // The time the mood was recorded
    private String userGatheringStatus; // The user's gathering status (e.g., alone, with friends)
    private String moodStatus;        // The user's mood (e.g., happy, sad, angry)
    private String moodTrigger;       // The trigger for the mood (e.g., event, circumstance)
    private String moodReason;        // The reason behind the mood (e.g., feeling lonely)
    private Integer moodImage;        // An image representing the mood (can be null)

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
     */
    public Mood(String userName, String userId, String userLocation, String userTime,
                String userGatheringStatus, String moodStatus, String moodTrigger,
                String moodReason, Integer moodImage) {
        this.userName = userName;
        this.userId = userId;
        this.userLocation = userLocation;
        this.userTime = userTime;
        this.userGatheringStatus = userGatheringStatus;
        this.moodStatus = moodStatus;
        this.moodTrigger = moodTrigger;
        this.moodReason = moodReason;
        this.moodImage = moodImage;
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
    public String getUserId() {
        return userId;
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
     * Returns the time when the mood was recorded.
     *
     * @return The time the mood was recorded.
     */
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
    public String getMoodStatus() {
        return moodStatus;
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
     * Returns the reason behind the user's mood.
     *
     * @return The reason behind the user's mood.
     */
    public String getMoodReason() {
        return moodReason;
    }

    /**
     * Returns the image associated with the user's mood.
     *
     * @return The image representing the mood (may be null).
     */
    public Integer getMoodImage() {
        return moodImage;
    }
}
