package com.example.whimsy;

import android.content.Intent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class FollowingActivityTest {

    // Launch FollowingActivity with valid intent extras
    @Rule
    public ActivityScenarioRule<FollowingActivity> activityRule =
            new ActivityScenarioRule<>(
                    new Intent()
                            .setClassName("com.example.whimsy", FollowingActivity.class.getName())
                            .putExtra("type", "following")
                            .putExtra("userId", "testUserId")
            );

    @Test
    public void testTitle_isFollowing() {
        // Verify the title TextView is displayed and has the correct text
        onView(withId(R.id.following_followers_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Following")));
    }

    @Test
    public void testRecyclerView_isDisplayed() {
        // Verify the RecyclerView is displayed
        onView(withId(R.id.recyclerViewFollowing))
                .check(matches(isDisplayed()));
    }
}