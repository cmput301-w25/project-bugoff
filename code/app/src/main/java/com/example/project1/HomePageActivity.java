// HomePageActivity.java
package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends ActivityBase {
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private ImageView profileButton, homeButton, addMoodButton, searchButton, notificationButton;
    protected FrameLayout contentFrame;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Set the base layout
        initializeNavigation();

        // Inflate the home page layout into the content frame
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_home_page, contentFrame, true);

        recyclerView = findViewById(R.id.moods_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moodList = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
//          databaseReference = FirebaseDatabase.getInstance().getReference("moods").child(user.getUid());
            loadMoods();
        }
    }

    private void loadMoods() {
        // Adding two sample Mood objects to the moodList
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", R.drawable.angry_photo));
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", null));

        // Notify the adapter that the data has changed
        moodAdapter.notifyDataSetChanged();
    }
}