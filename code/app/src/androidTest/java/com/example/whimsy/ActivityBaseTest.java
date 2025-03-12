package com.example.whimsy;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class ActivityBaseTest {

    @Rule
    public ActivityScenarioRule<ActivityBase> activityRule = new ActivityScenarioRule<>(ActivityBase.class);

    @Before
    public void setUp() {
        // Initialize Intents before each test
        Intents.init();
    }

    @After
    public void tearDown() {
        // Release Intents after each test
        Intents.release();
    }

    @Test
    public void testHomeButtonClick_opensHomePageActivity() {
        // Check home button is displayed
        onView(withId(R.id.home)).check(matches(isDisplayed()));

        // Click home button and verify intent
        onView(withId(R.id.home)).perform(click());
        intended(hasComponent(HomePageActivity.class.getName()));
    }

    @Test
    public void testProfileButtonClick_opensProfileActivity() {
        // Check profile button is displayed
        onView(withId(R.id.profile_button)).check(matches(isDisplayed()));

        // Click profile button and verify intent
        onView(withId(R.id.profile_button)).perform(click());
        intended(hasComponent(ProfileActivity.class.getName()));
    }

    @Test
    public void testSettingsButtonClick_opensSettingsActivity() {
        // Check settings button is displayed
        onView(withId(R.id.iconSettings)).check(matches(isDisplayed()));

        // Click settings button and verify intent
        onView(withId(R.id.iconSettings)).perform(click());
        intended(hasComponent(SettingsActivity.class.getName()));
    }

    @Test
    public void testSearchButtonClick_opensSearchActivity() {
        // Check search button is displayed
        onView(withId(R.id.search)).check(matches(isDisplayed()));

        // Click search button and verify intent
        onView(withId(R.id.search)).perform(click());
        intended(hasComponent(SearchActivity.class.getName()));
    }

    @Test
    public void testAddMoodButtonClick_opensAddMoodActivity() {
        // Check add mood button is displayed
        onView(withId(R.id.add)).check(matches(isDisplayed()));

        // Click add mood button and verify intent
        onView(withId(R.id.add)).perform(click());
        intended(hasComponent(AddMood.class.getName()));
    }
}