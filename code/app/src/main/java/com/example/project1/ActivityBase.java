// ActivityBase.java
package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ActivityBase extends AppCompatActivity {
    protected FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base); // ✅ Base layout includes the navigation bar

        contentFrame = findViewById(R.id.content_frame); // ✅ Placeholder for page content

        // ✅ Ensure navigation bar is always initialized
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        findViewById(R.id.home).setOnClickListener(v -> openActivity(HomePageActivity.class));
        findViewById(R.id.search).setOnClickListener(v -> openActivity(SearchActivity.class));
        //findViewById(R.id.add).setOnClickListener(v -> openActivity(AddPostActivity.class));
        //findViewById(R.id.heart).setOnClickListener(v -> openActivity(NotificationsActivity.class));
        findViewById(R.id.profile_button).setOnClickListener(v -> openActivity(ProfileActivity.class));
    }

    private void openActivity(Class<?> targetActivity) {
        if (!this.getClass().equals(targetActivity)) {
            Intent intent = new Intent(this, targetActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}