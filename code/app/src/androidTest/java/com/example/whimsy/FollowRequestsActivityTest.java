package com.example.whimsy;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.action.ViewActions.click;

@RunWith(AndroidJUnit4.class)
public class FollowRequestsActivityTest {

    private FirebaseUser mockUser;

    @Before
    public void setUp() {
        // Mock FirebaseAuth if you're using a fake Auth system or test rule
        mockUser = Mockito.mock(FirebaseUser.class);
        Mockito.when(mockUser.getUid()).thenReturn("testUserId");

        FirebaseAuth mockAuth = Mockito.mock(FirebaseAuth.class);
        Mockito.when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        FirebaseAuth.getInstance(); // Requires firebase-auth-testing
    }

    @Test
    public void testRecyclerViewIsDisplayed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", FollowRequestsActivity.class.getName());

        ActivityScenario.launch(intent);

        onView(withId(R.id.follow_requests_recycler_view))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testFollowRequestTitleDisplayed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", FollowRequestsActivity.class.getName());

        ActivityScenario.launch(intent);

        onView(withText("Follow Requests")).check(matches(isDisplayed()));
    }

    // Optional: if you mock Firebase and preload items, test button behavior:
    @Test
    public void testAcceptButtonClick() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", FollowRequestsActivity.class.getName());

        ActivityScenario.launch(intent);

        // Simulate button click (requires you have a button with specific ID/text in item layout)
        onView(withId(R.id.follow_requests_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Could assert a Toast or UI change here if desired
    }
}