/**
 * ActivityBase serves as the base activity for the application,
 * providing navigation controls to different parts of the app.
 *
 * This class initializes navigation buttons and defines click
 * listeners to navigate between activities.
 *
 * Outstanding Issues:
 * - Currently does not handle back navigation logic.
 * - Does not check if an activity is already running before launching a new one.
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
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityBase extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private ImageView profileButton, homeButton, settings, addMoodButton, searchButton, notificationButton;
    protected FrameLayout contentFrame; // Container for dynamic content

    /**
     * Called when the activity is first created.
     * Initializes the navigation buttons.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
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
        if(user != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile) // fallback image
                    .into(profileButton);
        }
        settings = findViewById(R.id.iconSettings);
        searchButton = findViewById(R.id.search);
        addMoodButton = findViewById(R.id.add);

        // Set click listeners to navigate to the respective activity
        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HomePageActivity.class)); // Navigate to Home Page
        });

        profileButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class)); // Navigate to Profile
        });

        settings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class)); // Navigate to Settings
        });

        searchButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class)); // Navigate to Search
        });

        addMoodButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AddMood.class)); // Navigate to Add Mood
        });
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
     * <p>
     * This method creates a Snackbar that displays the provided {@code message} and sets its background color
     * to the specified {@code backgroundColor}. It dynamically determines the optimal text color (black or white)
     * based on the luminance of the background color. If the calculated luminance is less than 0.5 (indicating a dark background),
     * white text is used; otherwise, black text is used to ensure optimal visibility.
     * </p>
     * <p>
     * The Snackbar's position is adjusted by moving it upward by 150 pixels from its default position.
     * </p>
     *
     * @param message         The text message to display in the Snackbar.
     * @param backgroundColor The background color to apply to the Snackbar.
     */
    public void showSnackbar(String message, int backgroundColor) {
        View parentView = findViewById(R.id.content_frame);

        // Calculate luminance (returns a value between 0 and 1)
        double luminance = ColorUtils.calculateLuminance(backgroundColor);

        // If luminance is less than 0.5, the background is dark -> use white text.
        // Otherwise, use black text.
        int textColor = (luminance < 0.5) ? Color.WHITE : Color.BLACK;

        Snackbar snackbar = Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(backgroundColor)
                .setTextColor(textColor);

        View snackbarView = snackbar.getView();
        // Adjust the Snackbar position by moving it up by 150 pixels.
        snackbarView.setTranslationY(-150);
        snackbar.show();
    }


}
