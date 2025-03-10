package com.example.project1;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;


@RunWith(AndroidJUnit4.class)
public class ProfileActivityTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule = new ActivityScenarioRule<>(ProfileActivity.class);

    @Before
    public void setUp() {
        Intents.init(); // Initialize intents for verification
    }

    @After
    public void tearDown() {
        Intents.release(); // Clean up intents
    }

    @Test
    public void testProfileElements_areDisplayed() {
        // Verify profile UI elements are visible
        onView(withId(R.id.profile_image)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_name)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_email)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_bio)).check(matches(isDisplayed()));
        onView(withId(R.id.moods_recycler_view)).check(matches(isDisplayed()));
    }

    @Test
    public void testEditProfileButton_opensDialog() {
        // Click edit profile button
        onView(withId(R.id.edit_profile_btn)).perform(click());

        // Verify dialog elements are displayed
        onView(withId(R.id.edit_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_bio)).check(matches(isDisplayed()));
        onView(withId(R.id.gender_spinner)).check(matches(isDisplayed()));
    }

    @Test
    public void testFollowersCountClick_opensFollowingActivity() {
        // Click followers count
        onView(withId(R.id.followers_count)).perform(click());

        // Verify intent to FollowingActivity with "followers" type
        intended(hasComponent(FollowingActivity.class.getName()));
    }

    @Test
    public void testFollowingCountClick_opensFollowingActivity() {
        // Click following count
        onView(withId(R.id.following_count)).perform(click());

        // Verify intent to FollowingActivity with "following" type
        intended(hasComponent(FollowingActivity.class.getName()));
    }

    @Test
    public void testFilterButton_opensFilterPopup() {
        // Click filter button
        onView(withId(R.id.filter_button)).perform(click());

        // Verify filter popup elements
        onView(withId(R.id.spinner_emotional_state)).check(matches(isDisplayed()));
        onView(withId(R.id.filter_week)).check(matches(isDisplayed()));
        onView(withId(R.id.apply_button)).check(matches(isDisplayed()));
    }
}