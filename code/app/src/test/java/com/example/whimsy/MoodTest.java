package com.example.whimsy;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;

public class MoodTest {

    @Test
    public void testMoodGetters() {
        Mood mood = new Mood("John", "uid123", "Home", "12:00 PM", "Alone",
                "Happy", "Good news", "Feeling great", "image.jpg", "profile.jpg",
                Arrays.asList("Alice", "Bob"), false);

        assertEquals("John", mood.getUserName());
        assertEquals("uid123", mood.getUserId());
        assertEquals("Home", mood.getUserLocation());
        assertEquals("Feeling great", mood.getMoodReason());
        assertEquals(Arrays.asList("Alice", "Bob"), mood.getTaggedUserNames());
    }
}