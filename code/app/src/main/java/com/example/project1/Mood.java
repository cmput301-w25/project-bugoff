package com.example.project1;

import java.io.Serializable;
import java.util.List;

public class Mood implements Serializable {
    private String userName;
    private String userId;
    private String userLocation;
    private String userTime;
    private String userGatheringStatus;
    private String moodStatus;
    private String moodTrigger;
    private String moodReason;
    private String moodImage;
    private String profileImageUrl;
    private List<String> taggedUserNames;
    private String timestamp;

    public Mood(String userName, String userId, String userLocation, String timestamp, String userTime, String userGatheringStatus, String moodStatus, String moodTrigger, String moodReason, String moodImage, String profileImageUrl, List<String> taggedUserNames) {
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
    }

    public Mood() {
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getUserTime() {
        return userTime;
    }

    public String getUserGatheringStatus() {
        return userGatheringStatus;
    }

    public void setUserGatheringStatus(String userGatheringStatus) {
        this.userGatheringStatus = userGatheringStatus;
    }

    public String getMoodStatus() {
        return moodStatus;
    }

    public void setMoodStatus(String moodStatus) {
        this.moodStatus = moodStatus;
    }


    public String getMoodTrigger() {
        return moodTrigger;
    }

    public void setMoodTrigger(String moodTrigger) {
        this.moodTrigger = moodTrigger;
    }

    public String getMoodReason() {
        return moodReason;
    }

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


}