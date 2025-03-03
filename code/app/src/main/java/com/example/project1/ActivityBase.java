// ActivityBase.java
package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActivityBase extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private ImageView profileButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Base layout

        profileButton = findViewById(R.id.profile_button);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityBase.this, ProfileActivity.class);
                startActivity(intent);
            }
        });


        List<Mood> moodList = new ArrayList<>();
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", R.drawable.angry_photo));
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", null));

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);



    }
}