/**
 * The {@code ActivityBase} class serves as a base activity that provides navigation and UI functionality
 * for various screens within the application. It includes a bottom navigation bar with buttons for
 * accessing key features, such as the home page, profile, settings, search, mood logging, and maps.
 *
 * Key Features:
 *
 *     Provides a consistent navigation framework across multiple activities.
 *     Loads the user's profile picture if available via Firebase Authentication.
 *     Implements efficient navigation handling to avoid unnecessary activity launches.
 *     Supports a dynamic content frame for embedding different screens.
 *     Uses Material Design Snackbars for displaying status messages.
 *     Ensures smooth user experience by managing the activity back stack.
 *     Implements color-aware Snackbars for improved UI feedback.
 *
 */

package com.example.whimsy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityBase extends AppCompatActivity {
    private ImageView profileButton, homeButton, settings, addMoodButton, searchButton, mapButton;
    protected FrameLayout contentFrame; // Container for dynamic content

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Set the base layout for the activity
        initializeNavigation(); // Initialize navigation buttons and their event handlers
    }

    /**
     * Initializes navigation buttons and assigns click listeners
     * to navigate to different activities.
     */
    protected void initializeNavigation() {
        // Finding views by their IDs
        homeButton = findViewById(R.id.home);
        profileButton = findViewById(R.id.profile_button);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile) // fallback image
                    .into(profileButton);
        }
        settings = findViewById(R.id.iconSettings);
        searchButton = findViewById(R.id.search);
        addMoodButton = findViewById(R.id.add);
        mapButton = findViewById(R.id.iconGlobe);

        ImageView heartButton = findViewById(R.id.heart);
        heartButton.setOnClickListener(v -> {
            navigateToActivity(FollowRequestsActivity.class);
        });

        // Updated click listeners using the helper method to manage the back stack
        homeButton.setOnClickListener(v -> {
            navigateToActivity(HomePageActivity.class);
        });

        profileButton.setOnClickListener(v -> {
            navigateToActivity(ProfileActivity.class);
        });

        settings.setOnClickListener(v -> {
            navigateToActivity(SettingsActivity.class);
        });

        searchButton.setOnClickListener(v -> {
            navigateToActivity(SearchActivity.class);
        });

        addMoodButton.setOnClickListener(v -> {
            navigateToActivity(AddMood.class);
        });

        mapButton.setOnClickListener(v -> {
            navigateToActivity(MapActivity.class);
        });
    }

    /**
     * Navigates to the specified activity.
     * If the current activity is already the target, no new intent is created.
     *
     * @param targetActivity The class of the activity to navigate to.
     */
    protected void navigateToActivity(Class<?> targetActivity) {
        // Prevent launching the same activity again
        if (this.getClass().equals(targetActivity)) {
            // Optionally, you could show a message or simply return.
            return;
        }
        // Create an intent for the target activity
        Intent intent = new Intent(this, targetActivity);
        // Add flags to clear intermediate activities if the target exists
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        // Optionally finish the current activity if you don't want it in the back stack.
        // finish();
    }

    /**
     * Displays a Snackbar with the specified message.
     *
     * @param message The message to display.
     */
    public void showSnackbar(String message) {
        showSnackbar(message, true);
    }

    /**
     * Helper method to display a Snackbar at the bottom of the screen with an optional error style.
     *
     * @param message The message to display.
     * @param isError If {@code true}, the Snackbar is styled as an error; otherwise, it is styled as a success message.
     */
    public void showSnackbar(String message, boolean isError) {
        View parentView = findViewById(R.id.content_frame);
        Snackbar snackbar;
        if (isError) {
            snackbar = Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .setTextColor(Color.WHITE);
        } else {
            snackbar = Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(R.color.dark_green))
                    .setTextColor(Color.WHITE);
        }
        View snackbarView = snackbar.getView();
        // Adjust the Snackbar position by moving it up by 150 pixels.
        snackbarView.setTranslationY(-150);
        snackbar.show();
    }

    /**
     * Displays a Snackbar with the specified message and background color.
     *
     * @param message         The text message to display in the Snackbar.
     * @param backgroundColor The background color to apply to the Snackbar.
     */
    public void showSnackbar(String message, int backgroundColor) {
        View parentView = findViewById(R.id.content_frame);
        double luminance = ColorUtils.calculateLuminance(backgroundColor);
        int textColor = (luminance < 0.5) ? Color.WHITE : Color.BLACK;
        Snackbar snackbar = Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(backgroundColor)
                .setTextColor(textColor);
        View snackbarView = snackbar.getView();
        snackbarView.setTranslationY(-150);
        snackbar.show();
    }
}
