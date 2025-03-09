package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityBase extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private ImageView profileButton, homeButton, settings, addMoodButton, searchButton, notificationButton;
    protected FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Set the base layout

        initializeNavigation();
    }

    protected void initializeNavigation() {
        homeButton = findViewById(R.id.home);
        profileButton = findViewById(R.id.profile_button);
        settings = findViewById(R.id.iconSettings);
        searchButton = findViewById(R.id.search);
        addMoodButton = findViewById(R.id.add);

        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HomePageActivity.class));
        });
        profileButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
        settings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
        searchButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });
        addMoodButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AddMood.class));
        });


    }
}