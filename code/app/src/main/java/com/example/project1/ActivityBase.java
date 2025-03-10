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

package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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
}
