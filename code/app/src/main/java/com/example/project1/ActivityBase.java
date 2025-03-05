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
    private ImageView profileButton;
    protected FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Load header/footer layout

        // Initialize the content frame (container for child layouts)
        contentFrame = findViewById(R.id.content_frame);

        // Set up the footer navigation for the search icon
        ImageView searchIcon = findViewById(R.id.search);
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> {
                // Only open SearchActivity if we're not already there
                if (!(ActivityBase.this instanceof SearchActivity)) {
                    Intent intent = new Intent(ActivityBase.this, SearchActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}