package com.example.whimsy;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class FollowRequestsActivityTest {

    @Test
    public void testRecyclerViewIsVisible() {
        Intent intent = new Intent();
        intent.putExtra("userId", "testUserId");
        ActivityScenario.launch(FollowRequestsActivity.class, intent);

        onView(withId(R.id.recyclerViewFollowRequests)).check(matches(isDisplayed()));
    }

    @Test
    public void testTitleIsCorrect() {
        onView(withId(R.id.follow_requests_title)).check(matches(withText("Follow Requests")));
    }
}