package com.example.whimsy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.util.ArrayList;

class MoodPageUtil {

    public static boolean shouldShowEditFab(Mood selectedMood, String currentUserEmail) {
        if (selectedMood == null || currentUserEmail == null) {
            return false;
        }
        return selectedMood.getUserId().equals(currentUserEmail);
    }
}


public class MoodPageTest {

    @Test
    public void testShouldShowEditFab_true() {
       
        Mood mood = new Mood(
                "Test User",
                "test@example.com",
                "Test Location",
                "timestamp",
                "timestamp",
                "Alone",
                "Feeling Happy",
                "trigger",
                "reason",
                "imageUrl",
                "profileImageUrl",
                new ArrayList<>(),
                false
        );
        boolean result = MoodPageUtil.shouldShowEditFab(mood, "test@example.com");
        assertTrue("Edit FAB should be visible when mood userId matches current user email", result);
    }

    @Test
    public void testShouldShowEditFab_false_nullMood() {
        boolean result = MoodPageUtil.shouldShowEditFab(null, "test@example.com");
        assertEquals("Edit FAB should not be visible when mood is null", false, result);
    }

    @Test
    public void testShouldShowEditFab_false_nullUser() {
        Mood mood = new Mood(
                "Test User",
                "test@example.com",
                "Test Location",
                "timestamp",
                "timestamp",
                "Alone",
                "Feeling Happy",
                "trigger",
                "reason",
                "imageUrl",
                "profileImageUrl",
                new ArrayList<>(),
                false
        );
        boolean result = MoodPageUtil.shouldShowEditFab(mood, null);
        assertEquals("Edit FAB should not be visible when current user email is null", false, result);
    }

    @Test
    public void testShouldShowEditFab_false_mismatch() {
        Mood mood = new Mood(
                "Test User",
                "test@example.com",
                "Test Location",
                "timestamp",
                "timestamp",
                "Alone",
                "Feeling Happy",
                "trigger",
                "reason",
                "imageUrl",
                "profileImageUrl",
                new ArrayList<>(),
                false
        );
        boolean result = MoodPageUtil.shouldShowEditFab(mood, "other@example.com");
        assertEquals("Edit FAB should not be visible when mood userId does not match current user email", false, result);
    }
}
