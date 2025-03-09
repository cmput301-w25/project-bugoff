/**
 * HomePageActivity is responsible for displaying the home page of the app,
 * showing a list of moods using a RecyclerView and managing user navigation
 * within the app.
 *
 * This activity extends ActivityBase to inherit the navigation functionality
 * and adds its own specific layout and logic for displaying moods.
 *
 * Outstanding Issues:
 * - Firebase integration for fetching live data is currently disabled (commented-out code).
 * - The loadMoods function is hardcoded with sample data; dynamic data retrieval needs to be implemented.
 */

package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends ActivityBase {

    // UI elements
    private RecyclerView recyclerView; // RecyclerView to display a list of moods
    private MoodAdapter moodAdapter; // Adapter for binding mood data to RecyclerView
    private List<Mood> moodList; // List to hold the mood objects
    private FirebaseAuth mAuth; // FirebaseAuth instance for managing user authentication
    private FirebaseDatabase database; // FirebaseDatabase instance to interact with Firebase
    private DatabaseReference databaseReference; // Database reference to access moods in Firebase
    private ImageView profileButton, homeButton, addMoodButton, searchButton, notificationButton; // Navigation buttons
    protected FrameLayout contentFrame; // Container for dynamic content in the activity

    /**
     * Called when the activity is first created.
     * Initializes the navigation buttons and sets up the RecyclerView to display moods.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Set the base layout
        initializeNavigation(); // Initialize navigation buttons

        // Inflate the home page layout into the content frame
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_home_page, contentFrame, true);

        // Set up the RecyclerView to display a list of moods
        recyclerView = findViewById(R.id.moods_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the mood list and adapter
        moodList = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);

        // Initialize Firebase authentication instance
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // If the user is authenticated, load moods from the database
        if (user != null) {
            // databaseReference = FirebaseDatabase.getInstance().getReference("moods").child(user.getUid());
            loadMoods(); // Load sample moods
        }
    }

    /**
     * Loads sample mood data into the mood list.
     * This function simulates retrieving data from a database (e.g., Firebase).
     * In a real implementation, this should be replaced with actual data retrieval logic.
     */
    private void loadMoods() {
        // Adding two sample Mood objects to the moodList
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", R.drawable.angry_photo));
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", null));

        // Notify the adapter that the data has changed so the UI can be updated
        moodAdapter.notifyDataSetChanged();
    }
}
