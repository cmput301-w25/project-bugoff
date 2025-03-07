// HomePageActivity.java
package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends ActivityBase {

    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList;
    private FirebaseAuth mAuth;
    private ImageView profileButton, homeButton, searchButton, addButton, heartButton;
    protected FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // ✅ Ensure we load the base layout

        // ✅ Inflate the home page layout inside the base layout
        contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_home_page, contentFrame, true);

        // ✅ Initialize RecyclerView for moods
        recyclerView = findViewById(R.id.moods_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moodList = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);

        // ✅ Load moods if the user is logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadMoods();
        }

        // ✅ Initialize Bottom Navigation Buttons
        profileButton = findViewById(R.id.profile_button);
        homeButton = findViewById(R.id.home);
        searchButton = findViewById(R.id.search);
        addButton = findViewById(R.id.add);
        heartButton = findViewById(R.id.heart);

        // ✅ Set Up Navigation Click Listeners
        profileButton.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        homeButton.setOnClickListener(v -> navigateTo(HomePageActivity.class));
        searchButton.setOnClickListener(v -> navigateTo(SearchActivity.class));
        //addButton.setOnClickListener(v -> navigateTo(AddPostActivity.class));
        heartButton.setOnClickListener(v -> navigateTo(NotificationsActivity.class));
    }

    private void navigateTo(Class<?> targetActivity) {
        if (!this.getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private void loadMoods() {
        // ✅ Sample moods (Replace this with actual Firebase data fetching)
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", R.drawable.angry_photo));
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", null));

        // ✅ Notify adapter that data has changed
        moodAdapter.notifyDataSetChanged();
    }
}