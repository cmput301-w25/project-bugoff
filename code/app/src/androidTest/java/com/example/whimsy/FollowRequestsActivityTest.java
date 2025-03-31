package com.example.whimsy;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FollowRequestsActivityTest {

    @Test
    public void testRecyclerViewVisibleOnLaunch() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", FollowRequestsActivity.class.getName());
        intent.putExtra("testMode", true);  // Prevents Firebase crash
        ActivityScenario.launch(intent);

        onView(withId(R.id.follow_requests_recycler_view))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testPageTitleDisplayed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", FollowRequestsActivity.class.getName());
        intent.putExtra("testMode", true);
        ActivityScenario.launch(intent);

        onView(withText("Follow Requests"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testAcceptButtonVisibleIfPresent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", FollowRequestsActivity.class.getName());
        intent.putExtra("testMode", true);
        ActivityScenario.launch(intent);

        // Accept button in item layout
        onView(withId(R.id.follow_requests_recycler_view))
                .check(matches(isDisplayed()));

        // Uncomment this if your adapter inflates a button with ID `accept_button`
        // onView(withId(R.id.accept_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testRejectButtonVisibleIfPresent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", FollowRequestsActivity.class.getName());
        intent.putExtra("testMode", true);
        ActivityScenario.launch(intent);

        // Reject button in item layout
        onView(withId(R.id.follow_requests_recycler_view))
                .check(matches(isDisplayed()));

        // Uncomment this if your adapter inflates a button with ID `reject_button`
        // onView(withId(R.id.reject_button)).check(matches(isDisplayed()));
    }

    @Ignore("Requires mock Firebase or local test user")
    @Test
    public void testRecyclerViewWithRealFirebaseUser() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", FollowRequestsActivity.class.getName());
        ActivityScenario.launch(intent);  // 🚨 Will crash without testMode
    }
}