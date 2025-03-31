package com.example.whimsy;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Root;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AddMoodTest {

    private Activity currentActivity;

    // A simple helper to obtain the activity's decor view for matching Snackbars.
    private static Matcher<Root> withDecorView(final Activity activity) {
        return new TypeSafeMatcher<Root>() {
            @Override
            protected boolean matchesSafely(Root root) {
                return root.getDecorView() == activity.getWindow().getDecorView();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("with decor view");
            }
        };
    }

    @Before
    public void setUp() {
        ActivityScenario<AddMood> scenario = ActivityScenario.launch(AddMood.class);
        scenario.onActivity(activity -> currentActivity = activity);
    }

    @Test
    public void testAddMoodWithoutSelectingEmotion_showsSnackbar() {
        // Click the add mood button.
        onView(withId(R.id.addMoodButton)).perform(click());
        // Verify that a Snackbar with the text "Please select an emotion." is displayed.
        onView(withText("Please select an emotion."))
                .inRoot(new TypeSafeMatcher<Root>() {
                    @Override
                    protected boolean matchesSafely(Root root) {
                        return root.getDecorView() == currentActivity.getWindow().getDecorView();
                    }
                    @Override
                    public void describeTo(Description description) {
                        description.appendText("is in the decor view");
                    }
                })
                .check(matches(withText("Please select an emotion.")));
    }

    @Test
    public void testPrivacyToggle_changesSnackbarMessage() throws InterruptedException {
        // Click the privacy icon to toggle to private.\
        Thread.sleep(2000);
        onView(withId(R.id.visibilityIcon)).perform(click());
        Thread.sleep(2000);
        // Verify that the Snackbar text view displays "Mood set to Private"
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Mood set to Private")));

        // Delay before toggling again (e.g., 2 seconds).
        Thread.sleep(2000);

        // Click the privacy icon again to toggle back to public.
        onView(withId(R.id.visibilityIcon)).perform(click());
        Thread.sleep(2000);
        // Verify that the Snackbar text view displays "Mood set to Public"
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(withText("Mood set to Public")));
    }


    @Test
    public void testReasonInputCharacterCounter_updates() {
        // Check initial character count ("200").
        onView(withId(R.id.reasonCharCountText)).check(matches(withText("200")));
        // Replace text in reason input.
        String inputText = "Testing 123 123";
        int expectedCount = 200 - inputText.length();
        onView(withId(R.id.reasonInput)).perform(replaceText(inputText));
        // Verify that the character counter updates.
        onView(withId(R.id.reasonCharCountText)).check(matches(withText(String.valueOf(expectedCount))));
    }

    @Test
    public void testLocationPopupAppearsAndCanBeCancelled() {
        // Click the location icon to open the location popup.
        onView(withId(R.id.locationIcon)).perform(click());
        // In the popup, click the cancel button (btn_cancel_location).
        onView(withId(R.id.btn_cancel_location)).perform(click());
        // (If the dialog is dismissed, then the cancel button should no longer be displayed.)
        // We simply check that a view with that id is not found.
        onView(withId(R.id.btn_cancel_location)).check((view, noViewFoundException) -> {
            if (view != null) {
                throw new AssertionError("Location popup should be dismissed");
            }
        });
    }

    @Test
    public void testTagUsersPopupAppears() {
        // Click the tag icon to open the tag users dialog.
        onView(withId(R.id.tagIcon)).perform(click());
        // Verify that the search field (id: tag_search_edit_text) is displayed.
        onView(withId(R.id.tag_search_edit_text)).check(matches(withText("")));
    }
}
