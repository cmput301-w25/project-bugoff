package com.example.whimsy;

import static org.junit.Assert.*;

import org.junit.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivityTest {

    @Test
    public void testDetermineGatheringStatus() {
        // Case 1: No tags
        List<Map<String, Object>> tagsNull = null;
        assertEquals("Alone", determineGatheringStatus(tagsNull));

        // Case 2: Empty list
        List<Map<String, Object>> tagsEmpty = new ArrayList<>();
        assertEquals("Alone", determineGatheringStatus(tagsEmpty));

        // Case 3: One person
        List<Map<String, Object>> tags1 = createTags(1);
        assertEquals("With 1 other", determineGatheringStatus(tags1));

        // Case 4: Three people
        List<Map<String, Object>> tags3 = createTags(3);
        assertEquals("With 3 others", determineGatheringStatus(tags3));

        // Case 5: Six people
        List<Map<String, Object>> tags6 = createTags(6);
        assertEquals("With a crowd", determineGatheringStatus(tags6));
    }

    private String determineGatheringStatus(List<Map<String, Object>> tags) {
        if (tags == null || tags.isEmpty()) {
            return "Alone";
        } else {
            int tagCount = tags.size();
            if (tagCount == 1) {
                return "With 1 other";
            } else if (tagCount <= 5) {
                return "With " + tagCount + " others";
            } else {
                return "With a crowd";
            }
        }
    }

    private List<Map<String, Object>> createTags(int count) {
        List<Map<String, Object>> tags = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> tag = new HashMap<>();
            tag.put("name", "user" + i);
            tags.add(tag);
        }
        return tags;
    }
}
