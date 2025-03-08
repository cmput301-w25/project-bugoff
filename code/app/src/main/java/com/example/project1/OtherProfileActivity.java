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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;

public class OtherProfileActivity extends ActivityBase {

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

        // Load the base layout with header and footer
        setContentView(R.layout.activity_base);

        // Inflate the profile layout inside the content frame
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_other_profile, contentFrame, true);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Set up bottom navigation
        findViewById(R.id.home).setOnClickListener(v -> startActivity(new Intent(this, HomePageActivity.class)));
        findViewById(R.id.search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.profile_button).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        initUI();

        // Get the searched user ID from Intent
        searchedUserId = getIntent().getStringExtra("USER_ID");

        if (searchedUserId != null && !searchedUserId.isEmpty()) {
            loadUserData(searchedUserId);
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initUI() {
        profileImage = findViewById(R.id.other_profile_image);
        profileName = findViewById(R.id.other_profile_name);
        profileEmail = findViewById(R.id.other_profile_email);
        profileBio = findViewById(R.id.other_profile_bio);
        followButton = findViewById(R.id.follow_btn);
        backButton = findViewById(R.id.back_button);

        // Set click listeners
        backButton.setOnClickListener(v -> finish());
        followButton.setOnClickListener(v -> followUser());
    }

    private void loadUserData(String userId) {
        // Fetch user profile data
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

                        // Check if current user is already following this user
                        db.collection("users").document(searchedUserId)
                                .collection("followers")
                                .document(currentUserId)
                                .get()
                                .addOnSuccessListener(followSnapshot -> {
                                    if (followSnapshot.exists()) {
                                        followButton.setText("Following");
                                        followButton.setEnabled(false);
                                    } else {
                                        followButton.setText("Follow");
                                        followButton.setEnabled(true);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("OtherProfileActivity", "Error checking follow status", e));
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("OtherProfileActivity", "Error fetching user data", e));
    }

    private void followUser() {
        // Validate user IDs
        if (currentUserId == null || searchedUserId == null || currentUserId.equals(searchedUserId)) {
            Toast.makeText(this, "Invalid operation.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a batch write for atomic updates
        WriteBatch batch = db.batch();

        // Reference to the follower's document in searched user's followers subcollection
        DocumentReference followerRef = db.collection("users")
                .document(searchedUserId)
                .collection("followers")
                .document(currentUserId);
        batch.set(followerRef, new HashMap<String, Object>());

        // Reference to the following document in current user's following subcollection
        DocumentReference followingRef = db.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(searchedUserId);
        batch.set(followingRef, new HashMap<String, Object>());

        // Commit the batch write
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    followButton.setText("Following");
                    followButton.setEnabled(false);
                    Toast.makeText(this, "Followed successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowUser", "Error following user", e);
                    Toast.makeText(this, "Failed to follow.", Toast.LENGTH_SHORT).show();
                });
    }
}