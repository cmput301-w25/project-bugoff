package com.example.project1;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;

@RunWith(AndroidJUnit4.class)
public class AddMoodTest {

    @Rule
    public ActivityScenarioRule<AddMood> activityRule = new ActivityScenarioRule<>(AddMood.class);

    @Test
    public void testMoodSpinnerSelection() {
        // Open spinner and select "Happy"
        onView(withId(R.id.moodSpinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Happy"))).perform(click());

        // Verify selection
        onView(withId(R.id.moodSpinner)).check(matches(withSpinnerText(containsString("Happy"))));
    }

    @Test
    public void testAddMoodButton_withoutEmotion_showsError() {
        // Leave spinner at "Select an Emotion" and click add
        onView(withId(R.id.addMoodButton)).perform(click());

        // Check for Snackbar with error message
        onView(withText("Please select an emotion.")).check(matches(isDisplayed()));
    }

    @Test
    public void testReasonInput_characterLimit() {
        // Type more than 20 characters
        onView(withId(R.id.reasonInput)).perform(typeText("This is a very long reason exceeding 20 chars"));

        // Verify text is truncated to 20 chars
        onView(withId(R.id.reasonInput)).check(matches(withText("This is a very long ")));
        onView(withId(R.id.reasonCharCountText)).check(matches(withText("0")));
    }

    @Test
    public void testLocationIcon_opensDialog() {
        // Click location icon
        onView(withId(R.id.locationIcon)).perform(click());

        // Verify dialog buttons are displayed
        onView(withId(R.id.btn_use_current)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_cancel_location)).check(matches(isDisplayed()));
    }

    @Test
    public void testImportImageIcon_opensGallery() {
        // This requires intent mocking or manual interaction in a real test
        onView(withId(R.id.importImageIcon)).perform(click());
    }
}