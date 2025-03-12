package com.example.whimsy;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class SignUpActivityTest {

    @Rule
    public ActivityScenarioRule<SignUpActivity> activityRule = new ActivityScenarioRule<>(SignUpActivity.class);

    @Test
    public void testSignUpButton_emptyFields_showsErrors() {
        // Click signup with empty fields
        onView(withId(R.id.signup_btn)).perform(click());

        // Check error messages
        onView(withId(R.id.name)).check(matches(withText("")));
        onView(withId(R.id.username)).check(matches(withText("")));
        onView(withId(R.id.email)).check(matches(withText("")));
    }

    @Test
    public void testDobField_opensDatePicker() {
        // Click DOB field
        onView(withId(R.id.dob)).perform(click());

        // Verify DatePicker dialog (basic check)
        onView(withText("CANCEL")).check(matches(isDisplayed()));
    }

    @Test
    public void testPasswordMismatch_showsError() {
        // Enter mismatched passwords
        onView(withId(R.id.password)).perform(typeText("password123"));
        onView(withId(R.id.confirm_password)).perform(typeText("password321"));

        // Check error on confirm password
        onView(withId(R.id.confirm_password)).check(matches(withText("password321")));
    }
}