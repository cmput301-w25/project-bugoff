package com.example.whimsy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MoodTest {

    private Mood mood;
    private List<String> taggedUsers;

    @Before
    public void setUp() {
        taggedUsers = Arrays.asList("alice", "bob");
        mood = new Mood(
                "John Doe", "john123", "Edmonton", "timestamp123",
                "12:00 PM", "With friends", "Feeling happy",
                "Birthday party", "Excited to celebrate", "image.jpg",
                "profile.jpg", taggedUsers, true
        );
        mood.setOwnerUid("owner456");
        mood.setMoodId("mood789");
        mood.setLocationLat(53.5461);
        mood.setLocationLng(-113.4938);
    }

    @Test public void testUserName() {
        assertEquals("John Doe", mood.getUserName());
    }

    @Test public void testUserId() {
        assertEquals("john123", mood.getUserId());
    }

    @Test public void testLocation() {
        assertEquals("Edmonton", mood.getUserLocation());
    }

    @Test public void testTime() {
        assertEquals("12:00 PM", mood.getUserTime());
    }

    @Test public void testGatheringStatus() {
        assertEquals("With friends", mood.getUserGatheringStatus());
    }

    @Test public void testMoodStatus() {
        assertEquals("Feeling happy", mood.getMoodStatus());
    }

    @Test public void testTrigger() {
        assertEquals("Birthday party", mood.getMoodTrigger());
    }

    @Test public void testReason() {
        assertEquals("Excited to celebrate", mood.getMoodReason());
    }

    @Test public void testMoodImage() {
        assertEquals("image.jpg", mood.getMoodImage());
    }

    @Test public void testProfileImageUrl() {
        assertEquals("profile.jpg", mood.getProfileImageUrl());
    }

    @Test public void testTaggedUsers() {
        assertEquals(taggedUsers, mood.getTaggedUserNames());
    }

    @Test public void testTimestamp() {
        assertEquals("timestamp123", mood.getTimestamp());
    }

    @Test public void testPrivacy() {
        assertTrue(mood.isPrivateMood());
        assertTrue(mood.isPrivate());
    }

    @Test public void testOwnerUid() {
        assertEquals("owner456", mood.getOwnerUid());
    }

    @Test public void testMoodId() {
        assertEquals("mood789", mood.getMoodId());
    }

    @Test public void testLocationLatLng() {
        assertEquals(Double.valueOf(53.5461), mood.getLocationLat());
        assertEquals(Double.valueOf(-113.4938), mood.getLocationLng());
    }

    @Test public void testSetters() {
        mood.setUserName("Jane");
        mood.setUserId("jane321");
        mood.setUserLocation("Calgary");
        mood.setUserTime("3:00 PM");
        mood.setUserGatheringStatus("Alone");
        mood.setMoodStatus("Feeling sad");
        mood.setMoodTrigger("Rainy weather");
        mood.setMoodReason("Feeling lonely");
        mood.setMoodImage("new.jpg");
        mood.setProfileImageUrl("new_profile.jpg");
        mood.setTaggedUserNames(Arrays.asList("x", "y"));
        mood.setPrivate(false);
        mood.setOwnerUid("new_owner");
        mood.setMoodId("new_mood");
        mood.setLocationLat(51.0447);
        mood.setLocationLng(-114.0719);

        assertEquals("Jane", mood.getUserName());
        assertEquals("jane321", mood.getUserId());
        assertEquals("Calgary", mood.getUserLocation());
        assertEquals("3:00 PM", mood.getUserTime());
        assertEquals("Alone", mood.getUserGatheringStatus());
        assertEquals("Feeling sad", mood.getMoodStatus());
        assertEquals("Rainy weather", mood.getMoodTrigger());
        assertEquals("Feeling lonely", mood.getMoodReason());
        assertEquals("new.jpg", mood.getMoodImage());
        assertEquals("new_profile.jpg", mood.getProfileImageUrl());
        assertEquals(Arrays.asList("x", "y"), mood.getTaggedUserNames());
        assertFalse(mood.isPrivate());
        assertEquals("new_owner", mood.getOwnerUid());
        assertEquals("new_mood", mood.getMoodId());
        assertEquals(Double.valueOf(51.0447), mood.getLocationLat());
        assertEquals(Double.valueOf(-114.0719), mood.getLocationLng());
    }
}