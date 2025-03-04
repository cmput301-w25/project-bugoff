// ActivityBase.java
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
    private ImageView profileButton, homeButton, addMoodButton, searchButton, notificationButton;
    protected FrameLayout contentFrame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Set the base layout

        homeButton = findViewById(R.id.home);
        profileButton = findViewById(R.id.profile_button);

        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HomePageActivity.class));
        });
        profileButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

}