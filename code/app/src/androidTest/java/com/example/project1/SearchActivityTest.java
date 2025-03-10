package com.example.project1;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not; // Add this import for negation

@RunWith(AndroidJUnit4.class)
public class SearchActivityTest {

    @Rule
    public ActivityScenarioRule<SearchActivity> activityRule = new ActivityScenarioRule<>(SearchActivity.class);

    @Test
    public void testSearchEditText_empty_hidesRecyclerView() {
        onView(withId(R.id.search_edit_text)).perform(clearText());
        // Use not(isDisplayed()) instead of isNotDisplayed()
        onView(withId(R.id.search_results_recycler_view)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testSearchEditText_withQuery_showsRecyclerView() {
        onView(withId(R.id.search_edit_text)).perform(typeText("Tester"));
        onView(withId(R.id.search_results_recycler_view)).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchEditText_clearQuery_hidesRecyclerView() {
        onView(withId(R.id.search_edit_text)).perform(typeText("Tester"));
        onView(withId(R.id.search_edit_text)).perform(clearText());
        // Use not(isDisplayed()) instead of isNotDisplayed()
        onView(withId(R.id.search_results_recycler_view)).check(matches(not(isDisplayed())));
    }
}