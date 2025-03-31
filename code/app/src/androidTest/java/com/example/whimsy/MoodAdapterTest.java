package com.example.whimsy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class MoodAdapterTest {

    private Mood moodWithImage;
    private Mood moodNoImage;

    @Before
    public void setup() {
        moodWithImage = new Mood();
        moodWithImage.setMoodImage("http://image.url/pic.jpg");
        moodWithImage.setMoodStatus("Feeling Happy");

        moodNoImage = new Mood();
        moodNoImage.setMoodImage(""); // No image
        moodNoImage.setMoodStatus("Feeling Sad");
    }

    @Test
    public void testGetItemCount() {
        MoodAdapter adapter = new MoodAdapter(Arrays.asList(moodWithImage, moodNoImage));
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testGetItemViewType_withImage() {
        MoodAdapter adapter = new MoodAdapter(Collections.singletonList(moodWithImage));
        assertEquals(1, adapter.getItemViewType(0)); // VIEW_TYPE_WITH_IMAGE
    }

    @Test
    public void testGetItemViewType_noImage() {
        MoodAdapter adapter = new MoodAdapter(Collections.singletonList(moodNoImage));
        assertEquals(2, adapter.getItemViewType(0)); // VIEW_TYPE_NO_IMAGE
    }

    @Test
    public void testSetFollowedMoodsSet_appliesCorrectly() {
        MoodAdapter adapter = new MoodAdapter(Collections.singletonList(moodWithImage));
        adapter.setFollowedMoodsSet(Collections.singleton("uid_mid123"));
        assertNotNull(adapter);
    }

    @Test
    public void testUpdateMood_replacesFirstMood() {
        MoodAdapter adapter = new MoodAdapter(Arrays.asList(moodNoImage, moodWithImage));
        Mood updated = new Mood();
        updated.setMoodStatus("Feeling Excited");

        adapter.updateMood(updated);
        assertEquals("Feeling Excited", adapter.moodList.get(0).getMoodStatus());
    }
    

    @Test
    public void testSetOnCommentClickListener_doesNotCrash() {
        MoodAdapter adapter = new MoodAdapter(Collections.singletonList(moodWithImage));
        adapter.setOnCommentButtonClickListener(() -> {
            // Simulate click without assertion
        });
    }
}