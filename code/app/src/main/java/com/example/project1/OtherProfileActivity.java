package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class OtherProfileActivity extends ActivityBase {  // ✅ Extends ActivityBase to include header & footer

    private ImageView profileImage;
    private TextView profileName, profileEmail, profileBio;
    private Button followButton, backButton;
    private String searchedUserId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Load the base layout that includes the header and footer
        setContentView(R.layout.activity_base);

        // ✅ Inflate the profile layout inside the content frame
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_other_profile, contentFrame, true);

        db = FirebaseFirestore.getInstance();
        // ✅ Manually initialize bottom navigation buttons
        findViewById(R.id.home).setOnClickListener(v -> startActivity(new Intent(this, HomePageActivity.class)));
        findViewById(R.id.search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class))); // Redundant, but keeps it consistent
        //findViewById(R.id.add).setOnClickListener(v -> startActivity(new Intent(this, AddPostActivity.class)));
        //findViewById(R.id.heart).setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.profile_button).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();



        initUI();

        // ✅ Get the searched user ID from Intent
        searchedUserId = getIntent().getStringExtra("USER_ID");

        if (searchedUserId != null && !searchedUserId.isEmpty()) {
            loadUserData(searchedUserId);
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();  // ✅ Close activity if no user ID is found
        }
    }

    private void initUI() {
        profileImage = findViewById(R.id.other_profile_image);
        profileName = findViewById(R.id.other_profile_name);
        profileEmail = findViewById(R.id.other_profile_email);
        profileBio = findViewById(R.id.other_profile_bio);
        followButton = findViewById(R.id.follow_btn);
        backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> finish());
        followButton.setOnClickListener(v -> followUser());
        backButton.setOnClickListener(v -> finish());
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        profileName.setText(documentSnapshot.getString("name"));
                        profileEmail.setText(documentSnapshot.getString("email"));
                        profileBio.setText(documentSnapshot.getString("bio"));

                        String imageUrl = documentSnapshot.getString("profilePictureUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(profileImage);
                        }
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("OtherProfileActivity", "Error fetching user data", e));
    }

    private void followUser() {
        if (currentUserId == null || searchedUserId == null || currentUserId.equals(searchedUserId)) {
            Toast.makeText(this, "Invalid operation.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // References for subcollections
        db.collection("users").document(searchedUserId)
                .collection("followers").document(currentUserId)
                .set(new Follower(currentUserId)) // Store follower's ID
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(currentUserId)
                            .collection("following").document(searchedUserId)
                            .set(new Following(searchedUserId)) // Store following ID
                            .addOnSuccessListener(aVoid2 -> {
                                followButton.setText("Following");
                                followButton.setEnabled(false); // Disable button after following
                                Toast.makeText(this, "Followed successfully!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Log.e("Follow Error", "Error following user", e));
                })
                .addOnFailureListener(e -> Log.e("Follow Error", "Error adding follower", e));
    }

    // Data model classes
    public static class Follower {
        String userId;
        Follower(String userId) { this.userId = userId; }
    }

    public static class Following {
        String userId;
        Following(String userId) { this.userId = userId; }
    }
}
