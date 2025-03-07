package com.example.project1;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Typeface;
import android.widget.TextView;

public class NotificationsActivity extends ActivityBase {

    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList;
    private TextView followedMoodsTab, activityTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // Load base layout

        // Inflate `activity_notifications.xml` inside `content_frame`
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_notifications, contentFrame, true);

        // ✅ Find tab elements inside contentFrame
        followedMoodsTab = contentFrame.findViewById(R.id.tab_followed_moods);
        activityTab = contentFrame.findViewById(R.id.tab_activity);
        recyclerView = contentFrame.findViewById(R.id.moods_recycler_view);

        // Bottom Nav Bar Click Listeners
        findViewById(R.id.home).setOnClickListener(v -> startActivity(new Intent(this, HomePageActivity.class)));
        findViewById(R.id.search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.heart).setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.profile_button).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Initialize RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodList = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);

        // ✅ Show Followed Moods by Default
        setTabSelected(followedMoodsTab, activityTab);
        loadFollowedMoods();

        // Click listener for "Followed Moods"
        followedMoodsTab.setOnClickListener(v -> {
            setTabSelected(followedMoodsTab, activityTab);
            loadFollowedMoods(); // ✅ Load Followed Moods instead of redirecting
        });

        // Click listener for "Activity"
        activityTab.setOnClickListener(v -> {
            setTabSelected(activityTab, followedMoodsTab);
            loadNotifications(); // ✅ Load Notifications instead of redirecting
        });
    }

    /**
     * Utility method to apply bold styling to selected tab and gray out the other.
     */
    private void setTabSelected(TextView selectedTab, TextView unselectedTab) {
        selectedTab.setTypeface(null, Typeface.BOLD);
        selectedTab.setTextColor(getResources().getColor(android.R.color.black));

        unselectedTab.setTypeface(null, Typeface.NORMAL);
        unselectedTab.setTextColor(getResources().getColor(android.R.color.darker_gray));
    }

    private void loadFollowedMoods() {
        // Clear previous data and add sample Followed Moods (fetch from Firebase in real case)
        moodList.clear();
        moodList.add(new Mood("Followed User", "@followed_user", "Toronto, Canada", "5:00 AM, 2025-02-11", "With Friends", "Feeling Excited", "Success", "Got my grade back!", R.drawable.happy_photo));
        moodAdapter.notifyDataSetChanged();
    }

    private void loadNotifications() {
        moodList.clear(); // Clear previous data

        // Example: Follow Request Notification
        moodList.add(new Mood(
                "John Doe", // User Name
                "@johndoe", // Username
                "", // Location (not needed for follow request)
                "2025-02-11 5:05 AM", // Timestamp
                "", // Situation (not needed)
                "", // Mood (not needed)
                "", // Emotion (not needed)
                "John Doe requested to follow you.", // Notification Text
                R.drawable.ic_profile // Profile picture
        ));

        // Example: Mood Interaction Notification
        moodList.add(new Mood(
                "Jane Doe",
                "@janedoe",
                "Vancouver, Canada",
                "2025-02-11 4:39 AM",
                "",
                "Feeling Happy", // Mood (displayed in color)
                "",
                "Jane liked your mood post.",
                R.drawable.happy_photo // Mood icon
        ));

        moodAdapter.notifyDataSetChanged();
    }

}