package com.example.project1;

import java.util.List;

public class Mood {
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

    public Mood(String userName, String userId, String userLocation, String userTime, String userGatheringStatus, String moodStatus, String moodTrigger, String moodReason, String moodImage, String profileImageUrl, List<String> taggedUserNames) {
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
    }

    public List<String> getTaggedUserNames() {
        return taggedUserNames;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public String getUserTime() {
        return userTime;
    }

    public String getUserGatheringStatus() {
        return userGatheringStatus;
    }

    public String getMoodStatus() {
        return moodStatus;
    }

    public String getMoodTrigger() {
        return moodTrigger;
    }

    public String getMoodReason() {
        return moodReason;
    }

    public String getMoodImage() {
        return moodImage;
    }


}