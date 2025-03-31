package com.example.whimsy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MoodPageUtil {

    public static boolean shouldShowEditFab(Mood selectedMood, String currentUserEmail) {
        if (selectedMood == null || currentUserEmail == null) {
            return false;
        }
        return selectedMood.getUserId().equals(currentUserEmail);
    }
    /**
     * Returns background and foreground colors based on the mood status.
     * @return An array where [0] is background color and [1] is foreground color.
     */
    public static int[] getMoodColors(String moodStatus) {
        int colorBg;
        int colorFg;
        switch (moodStatus.toLowerCase()) {
            case "feeling happy":
                colorBg = R.color.happy_background;
                colorFg = R.color.happy_text;
                break;
            case "feeling sad":
                colorBg = R.color.sad_background;
                colorFg = R.color.sad_text;
                break;
            case "feeling angry":
                colorBg = R.color.anger_background;
                colorFg = R.color.anger_text;
                break;
            case "feeling scared":
                colorBg = R.color.scared_background;
                colorFg = R.color.scared_text;
                break;
            case "feeling confused":
                colorBg = R.color.confused_background;
                colorFg = R.color.confused_text;
                break;
            case "feeling disgusted":
                colorBg = R.color.disgust_background;
                colorFg = R.color.disgust_text;
                break;
            case "feeling excited":
                colorBg = R.color.excited_background;
                colorFg = R.color.excited_text;
                break;
            case "feeling ashamed":
                colorBg = R.color.ashamed_background;
                colorFg = R.color.ashamed_text;
                break;
            default:
                colorBg = R.color.white;
                colorFg = R.color.black;
                break;
        }
        return new int[]{colorBg, colorFg};
    }

    /**
     * Generates a string representation of tagged friends.
     */
    public static String getTaggedFriendsString(List<String> taggedFriends) {
        return taggedFriends.isEmpty() ? "No friends tagged" : String.join(", ", taggedFriends);
    }
}


public class MoodPageTest {

    // Tests for shouldShowEditFab

    @Test
    public void testShouldShowEditFab_true() {
        Mood mood = new Mood(
                "Test User",           // userName
                "testuser",            // userId (username in this context)
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
        boolean result = MoodPageUtil.shouldShowEditFab(mood, "testuser");
        assertTrue("Edit FAB should be visible when mood userId matches current user username", result);
    }

    @Test
    public void testShouldShowEditFab_false_nullMood() {
        boolean result = MoodPageUtil.shouldShowEditFab(null, "testuser");
        assertFalse("Edit FAB should not be visible when mood is null", result);
    }

    @Test
    public void testShouldShowEditFab_false_nullUser() {
        Mood mood = new Mood(
                "Test User",
                "testuser",
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
        assertFalse("Edit FAB should not be visible when current user username is null", result);
    }

    @Test
    public void testShouldShowEditFab_false_mismatch() {
        Mood mood = new Mood(
                "Test User",
                "testuser",
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
        boolean result = MoodPageUtil.shouldShowEditFab(mood, "otheruser");
        assertFalse("Edit FAB should not be visible when mood userId does not match current user username", result);
    }

    // Tests for getMoodColors

    @Test
    public void testGetMoodColors_happy() {
        int[] colors = MoodPageUtil.getMoodColors("Feeling Happy");
        assertEquals("Background color should match happy_background", R.color.happy_background, colors[0]);
        assertEquals("Foreground color should match happy_text", R.color.happy_text, colors[1]);
    }

    @Test
    public void testGetMoodColors_sad() {
        int[] colors = MoodPageUtil.getMoodColors("Feeling Sad");
        assertEquals("Background color should match sad_background", R.color.sad_background, colors[0]);
        assertEquals("Foreground color should match sad_text", R.color.sad_text, colors[1]);
    }

    @Test
    public void testGetMoodColors_unknown() {
        int[] colors = MoodPageUtil.getMoodColors("Unknown Mood");
        assertEquals("Background color should default to white", R.color.white, colors[0]);
        assertEquals("Foreground color should default to black", R.color.black, colors[1]);
    }

    // Tests for getTaggedFriendsString

    @Test
    public void testGetTaggedFriendsString_empty() {
        List<String> friends = new ArrayList<>();
        String result = MoodPageUtil.getTaggedFriendsString(friends);
        assertEquals("Should return 'No friends tagged' for empty list", "No friends tagged", result);
    }

    @Test
    public void testGetTaggedFriendsString_withFriends() {
        List<String> friends = Arrays.asList("Alice", "Bob");
        String result = MoodPageUtil.getTaggedFriendsString(friends);
        assertEquals("Should join friends with comma and space", "Alice, Bob", result);
    }
}