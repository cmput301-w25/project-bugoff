package com.example.whimsy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

public class MoodAdapterTest {

    @Test
    public void testGetItemCount() {
        List<Mood> moodList = new ArrayList<>();
        moodList.add(new Mood("John", "JohnDoe@example.com", "Edmonton", "timestamp1", "time1", "Alone",
                "Feeling Lonely", "trigger1", "reason1", "http://example.com/image1.jpg", "profileImageUrl1", new ArrayList<>()));
        moodList.add(new Mood("Jane", "JaneDoe@example.com", "Lost City", "timestamp2", "time2", "With Friends",
                "Feeling Sad", "trigger2", "reason2", "", "profileImageUrl2", new ArrayList<>()));

        MoodAdapter adapter = new MoodAdapter(moodList);
        assertEquals("Item count should match the number of moods", 2, adapter.getItemCount());
    }

    @Test
    public void testGetItemViewTypeWithImage() {
        List<Mood> moodList = new ArrayList<>();
        Mood moodWithImage = new Mood("John", "JohnDoe@example.com", "Edmonton", "timestamp", "time", "Alone",
                "Feeling Lonely", "trigger", "reason", "http://example.com/image.jpg", "profileImageUrl", new ArrayList<>());
        moodList.add(moodWithImage);

        MoodAdapter adapter = new MoodAdapter(moodList);
        int viewType = adapter.getItemViewType(0);
        assertEquals("View type should be 1 for moods with image", 1, viewType);
    }

    @Test
    public void testGetItemViewTypeNoImage() {
        List<Mood> moodList = new ArrayList<>();
        Mood moodNoImage = new Mood("Jane", "JaneDoe@example.com", "Lost City", "timestamp", "time", "With Friends",
                "Feeling Sad", "trigger", "reason", "", "profileImageUrl", new ArrayList<>());
        moodList.add(moodNoImage);

        MoodAdapter adapter = new MoodAdapter(moodList);
        int viewType = adapter.getItemViewType(0);
        assertEquals("View type should be 2 for moods without image", 2, viewType);
    }
}
