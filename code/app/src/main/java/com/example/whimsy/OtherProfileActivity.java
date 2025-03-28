/**
 * OtherProfileActivity handles the display of another user's profile and allows
 * the current user to follow or unfollow that user.
 *
 * This activity loads the profile information of the searched user, including
 * their name, email, bio, and profile picture, and shows the follow button's
 * state based on whether the current user is already following the searched user.
 *
 * Outstanding Issues:
 * - There is no handling for network or Firebase connection issues during the follow/unfollow process.
 * - The current user may accidentally try to follow themselves, which is blocked but needs clearer messaging.
 */

package com.example.whimsy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OtherProfileActivity extends ActivityBase {

    private ImageView profileImage;
    private TextView profileName, profileUsername, profileBio;
    private Button followButton, backButton;
    private String searchedUserId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    /**
     * Called when the activity is first created.
     * Initializes Firebase, sets up navigation buttons, and loads user data.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
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

    /**
     * Initializes the user interface components like profile image, text fields,
     * and buttons for following and navigating back.
     */
    private void initUI() {
        profileImage = findViewById(R.id.other_profile_image);
        profileName = findViewById(R.id.other_profile_name);
        profileUsername = findViewById(R.id.other_profile_email);
        profileBio = findViewById(R.id.other_profile_bio);
        followButton = findViewById(R.id.follow_btn);
        followButton.setOnClickListener(v -> followUser());
    }

    /**
     * Loads the data for the searched user and populates the profile UI.
     *
     * @param userId The user ID to load the data for.
     */
    private void loadUserData(String userId) {
        // Fetch user profile data
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        profileName.setText(documentSnapshot.getString("name"));
                        profileUsername.setText("@" + documentSnapshot.getString("username"));
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

    /**
     * Allows the current user to follow the searched user by adding them to both
     * users' follower/following subcollections in Firestore.
     */
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

                    // Call suggestion function
                    showSuggestedUsers();
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowUser", "Error following user", e);
                    Toast.makeText(this, "Failed to follow.", Toast.LENGTH_SHORT).show();
                });

    }
    private void showSuggestedUsers() {
        db.collection("users")
                .limit(10) // Increased limit to get more users before filtering
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<User> suggestions = new ArrayList<>();

                    // Fetch the current user's following list
                    db.collection("users").document(currentUserId)
                            .collection("following")
                            .get()
                            .addOnSuccessListener(followingDocs -> {
                                List<String> followingList = new ArrayList<>();
                                for (var doc : followingDocs) {
                                    followingList.add(doc.getId());
                                }

                                for (var doc : queryDocumentSnapshots) {
                                    String uid = doc.getId();

                                    // Exclude self, searched user, and users already followed
                                    if (!uid.equals(currentUserId) &&
                                            !uid.equals(searchedUserId) &&
                                            !followingList.contains(uid)) {

                                        User user = new User(
                                                uid,
                                                doc.getString("name"),
                                                doc.getString("username"),
                                                doc.getString("profilePictureUrl")
                                        );
                                        suggestions.add(user);
                                    }
                                }

                                if (!suggestions.isEmpty()) {
                                    displaySuggestions(suggestions);
                                }
                            })
                            .addOnFailureListener(e -> Log.e("Suggestions", "Error fetching following list", e));
                })
                .addOnFailureListener(e -> Log.e("Suggestions", "Error fetching users", e));
    }

    private void displaySuggestions(ArrayList<User> users) {
        LinearLayout suggestionsContainer = findViewById(R.id.suggestions_container);
        TextView suggestionsTitle = findViewById(R.id.suggestions_title);
        View suggestionsScroll = findViewById(R.id.suggestions_scroll);
        suggestionsContainer.removeAllViews(); // clear existing suggestions
        suggestionsContainer.setVisibility(View.VISIBLE); // make visible
        suggestionsTitle.setVisibility(View.VISIBLE);
        suggestionsScroll.setVisibility(View.VISIBLE);

        for (User user : users) {
            View userView = getLayoutInflater().inflate(R.layout.item_suggestion_user, null);

            TextView nameText = userView.findViewById(R.id.suggestion_name);
            ImageView profilePic = userView.findViewById(R.id.suggestion_image);
            Button followBtn = userView.findViewById(R.id.suggestion_follow_btn);

            nameText.setText(user.getName());
            String imageUrl = user.getProfilePictureUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(profilePic);
            } else {
                profilePic.setImageResource(R.drawable.ic_profile);
            }

            followBtn.setOnClickListener(v -> followSuggestedUser(user.getId(), followBtn));

            userView.setOnClickListener(v -> {
                Intent intent = new Intent(this, OtherProfileActivity.class);
                intent.putExtra("USER_ID", user.getId()); // Pass the user ID
                startActivity(intent);
            });

            suggestionsContainer.addView(userView);
        }
    }

    private void followSuggestedUser(String userId, Button button) {
        WriteBatch batch = db.batch();

        DocumentReference followerRef = db.collection("users")
                .document(userId)
                .collection("followers")
                .document(currentUserId);
        batch.set(followerRef, new HashMap<>());

        DocumentReference followingRef = db.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(userId);
        batch.set(followingRef, new HashMap<>());

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    button.setText("Following");
                    button.setEnabled(false);
                    Toast.makeText(this, "Followed!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("FollowSuggestedUser", "Failed", e));
    }
}
