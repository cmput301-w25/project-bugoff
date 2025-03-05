package com.example.project1;

public class Mood {
    private String userName;
    private String userId;
    private String userLocation;
    private String userTime;
    private String userGatheringStatus;
    private String moodStatus;
    private String moodTrigger;
    private String moodReason;
    private Integer moodImage;

    public Mood(String userName, String userId, String userLocation, String userTime, String userGatheringStatus, String moodStatus, String moodTrigger, String moodReason, Integer moodImage) {
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

    public Integer getMoodImage() {
        return moodImage;
    }
}