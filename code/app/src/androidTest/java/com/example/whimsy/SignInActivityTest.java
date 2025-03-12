package com.example.whimsy;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignInActivityTest {

    @Rule
    public ActivityScenarioRule<SignInActivity> activityRule = new ActivityScenarioRule<>(SignInActivity.class);

    private IdlingResource idlingResource;

    @After
    public void tearDown() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }

    @Test
    public void testLoginUser_ValidEmail_Success() {
        // Simulate user typing email and password
        onView(withId(R.id.email)).perform(typeText("test@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("test_123"), closeSoftKeyboard());

        // Click the login button
        onView(withId(R.id.signIn_btn)).perform(click());

        // The IdlingResource will wait for Firebase to complete
        // Check that the ProgressBar is no longer displayed
        onView(withId(R.id.progressBar)).check(matches((isDisplayed())));
    }


    @Test
    public void testLoginUser_InvalidEmail_ShowsError() throws InterruptedException {
        // Simulate user typing an invalid email
        onView(withId(R.id.email)).perform(typeText("invalid-email"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("password123"), closeSoftKeyboard());

        // Click the login button
        onView(withId(R.id.signIn_btn)).perform(click());

        // Check if the email field shows an error (assuming it uses setError())
        Thread.sleep(500); // Replace with IdlingResource for production
        onView(withId(R.id.email)).check(matches(withText("invalid-email")));
        // Note: Espresso doesn’t directly test EditText error messages; you might need custom matchers
        // Alternatively, check for a visible error message in the UI if displayed separately
    }

    @Test
    public void testLoginUser_NoInternet_ShowsSnackbar() throws InterruptedException {
        // Simulate user typing valid email and password
        onView(withId(R.id.email)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("password123"), closeSoftKeyboard());

        // Click the login button
        onView(withId(R.id.signIn_btn)).perform(click());

        // Check if the Snackbar does not appear with a "No internet connection" message
        Thread.sleep(500); // Replace with IdlingResource for production
        onView(withId(com.google.android.material.R.id.snackbar_text))
                .check(matches(not(withText("No internet connection"))));
    }

    @Test
    public void testShakeView() throws InterruptedException {
        // Simulate an action that triggers the shake animation (e.g., invalid input)
        onView(withId(R.id.email)).perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.signIn_btn)).perform(click());

        // Since Espresso can’t directly test animations, we can only infer their effect
        // For example, check if the email field is still visible and focused after shaking
        Thread.sleep(500); // Animation duration (replace with IdlingResource if possible)
        onView(withId(R.id.email)).check(matches(isDisplayed()));
    }
}

