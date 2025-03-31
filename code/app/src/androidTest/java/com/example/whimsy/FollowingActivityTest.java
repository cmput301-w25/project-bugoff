package com.example.whimsy;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FollowingActivityTest {

    private void launchTestActivity() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", FollowingActivity.class.getName());
        intent.putExtra("userId", "testUserId");
        intent.putExtra("type", "following");
        intent.putExtra("testMode", true); // 👈 avoid Firebase in onCreate()
        ActivityScenario.launch(intent);
    }

    @Test
    public void testTitle_isFollowing() {
        launchTestActivity();

        onView(withId(R.id.following_followers_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("Following")));
    }

    @Test
    public void testRecyclerView_isDisplayed() {
        launchTestActivity();

        onView(withId(R.id.recyclerViewFollowing))
                .check(matches(isDisplayed()));
    }
}