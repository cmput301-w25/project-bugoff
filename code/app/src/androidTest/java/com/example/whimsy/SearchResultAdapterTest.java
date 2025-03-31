package com.example.whimsy;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SearchResultAdapterTest {

    @Before
    public void launchSearchActivity() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.example.whimsy", SearchActivity.class.getName());
        ActivityScenario.launch(intent);
    }

    @Test
    public void testRecyclerViewIsVisibleWhenSearchResultsPresent() throws InterruptedException {
        // Simulate typing something to make RecyclerView visible
        onView(withId(R.id.search_edit_text)).perform(androidx.test.espresso.action.ViewActions.typeText("test"));
        Thread.sleep(2000); // wait for Firebase query to finish

        onView(withId(R.id.search_results_recycler_view)).check(matches(isDisplayed()));
    }

}