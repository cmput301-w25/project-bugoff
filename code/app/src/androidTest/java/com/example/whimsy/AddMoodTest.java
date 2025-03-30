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
    public void testPrivacyToggle_changesSnackbarMessage() {
        // Click the privacy icon to toggle private.
        onView(withId(R.id.visibilityIcon)).perform(click());
        // Verify Snackbar message.
        onView(withText("Mood set to Private"))
                .inRoot(withDecorView(currentActivity))
                .check(matches(withText("Mood set to Private")));

        // Click the icon again to toggle public.
//        onView(withId(R.id.visibilityIcon)).perform(click());
//        onView(withText("Mood set to Public"))
//                .inRoot(withDecorView(currentActivity))
//                .check(matches(withText("Mood set to Public")));
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

    }

    @Test
    public void testImportImageIcon_opensGallery() {
        // This requires intent mocking or manual interaction in a real test
        onView(withId(R.id.importImageIcon)).perform(click());
    }
}
