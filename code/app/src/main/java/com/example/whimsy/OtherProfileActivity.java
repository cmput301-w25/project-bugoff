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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.google.firebase.firestore.DocumentReference;

public class OtherProfileActivity extends ActivityBase {

    private ImageView profileImage;
    private TextView profileName, profileUsername, profileBio;
    private Button followButton, backButton;
    private String searchedUserId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private TextView followersCount, followingCount;
    private RecyclerView moodsRecyclerView;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList = new ArrayList<>();
    private List<String> moodDocIds = new ArrayList<>();
    private TextView moodCountText;

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
            setupFollowCounts(searchedUserId);
            loadMoods(searchedUserId);
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initUI() {
        profileImage = findViewById(R.id.other_profile_image);
        profileName = findViewById(R.id.other_profile_name);
        profileUsername = findViewById(R.id.other_profile_email);
        profileBio = findViewById(R.id.other_profile_bio);
        followButton = findViewById(R.id.follow_btn);
        backButton = findViewById(R.id.back_button);

        followersCount = findViewById(R.id.other_followers_count);
        followingCount = findViewById(R.id.other_following_count);
        moodCountText = findViewById(R.id.other_moods_count);

        backButton.setOnClickListener(v -> finish());
        followButton.setOnClickListener(v -> followUser());

        moodsRecyclerView = findViewById(R.id.other_moods_recycler_view);
        moodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodList = new ArrayList<>();
        moodDocIds = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        moodsRecyclerView.setAdapter(moodAdapter);
    }

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

    private void setupFollowCounts(String userId) {
        followersCount.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowingActivity.class);
            intent.putExtra("type", "followers");
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        followingCount.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowingActivity.class);
            intent.putExtra("type", "following");
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        db.collection("users").document(userId).collection("followers")
                .get()
                .addOnSuccessListener(querySnapshot ->
                        followersCount.setText(String.valueOf(querySnapshot.size())))
                .addOnFailureListener(e -> {
                    Log.e("OtherProfileActivity", "Error fetching followers count", e);
                    followersCount.setText("0");
                });

        db.collection("users").document(userId).collection("following")
                .get()
                .addOnSuccessListener(querySnapshot ->
                        followingCount.setText(String.valueOf(querySnapshot.size())))
                .addOnFailureListener(e -> {
                    Log.e("OtherProfileActivity", "Error fetching following count", e);
                    followingCount.setText("0");
                });
    }

    private void loadMoods(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String profileImageUrl = userDoc.getString("profilePictureUrl");
                    String name = userDoc.getString("name");
                    String username = userDoc.getString("username");
                    moodList.clear();
                    moodDocIds.clear();

                    db.collection("users").document(userId).collection("moods")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        // Pass the retrieved name and username instead of a FirebaseUser
                                        Mood moodObj = createMoodObject(document, name, username, profileImageUrl);
                                        moodList.add(moodObj);
                                        moodDocIds.add(document.getId());
                                    }
                                    sortAndUpdateMoods();
                                } else {
                                    updateMoodCount();
                                }
                            })
                            .addOnFailureListener(e -> handleMoodLoadFailure(e));
                });
    }

    // Updated createMoodObject: uses name and username strings
    private Mood createMoodObject(DocumentSnapshot document, String name, String username, String profileImageUrl) {
        String mood = document.getString("mood");
        String locationName = document.getString("locationName");
        String timestampStr = document.getString("timestamp");
        String trigger = document.getString("trigger");
        String reason = document.getString("reason");
        String imageUrl = document.getString("imageUrl");

        List<Map<String, Object>> tags = (List<Map<String, Object>>) document.get("tags");
        List<String> taggedUserNames = extractTaggedUserNames(tags);
        String gatheringStatus = calculateGatheringStatus(tags);

        return new Mood(
                name,               // Using the provided name
                username,           // Provided username
                locationName != null ? locationName : "No location",
                timestampStr,
                timestampStr,
                gatheringStatus,
                "Feeling " + mood,
                trigger,
                reason,
                imageUrl,
                profileImageUrl,
                taggedUserNames
        );
    }
    /**
     * Calculates gathering status based on number of tagged users.
     *
     * @param tags List of tag maps from Firestore
     * @return A string representing the gathering status
     */
    private String calculateGatheringStatus(List<Map<String, Object>> tags) {
        if (tags == null || tags.isEmpty()) return "Alone";
        int tagCount = tags.size();
        if (tagCount == 1) return "With 1 other";
        if (tagCount <= 5) return "With " + tagCount + " others";
        return "With a crowd";
    }
    private List<String> extractTaggedUserNames(List<Map<String, Object>> tags) {
        List<String> taggedUserNames = new ArrayList<>();
        if (tags != null) {
            for (Map<String, Object> tag : tags) {
                String username = (String) tag.get("name");
                if (username != null) {
                    taggedUserNames.add(username);
                }
            }
        }
        return taggedUserNames;
    }

    private long convertTimestampToMillis(String timestampStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a - MMMM dd, yyyy", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(timestampStr);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e("OtherProfileActivity", "Error parsing timestamp: " + timestampStr, e);
            return 0;
        }
    }

    private void sortAndUpdateMoods() {
        Collections.sort(moodList, (m1, m2) ->
                Long.compare(convertTimestampToMillis(m2.getTimestamp()), convertTimestampToMillis(m1.getTimestamp())));
        moodAdapter.notifyDataSetChanged();
        updateMoodCount();
    }

    private void updateMoodCount() {
        moodCountText.setText(String.valueOf(moodList.size()));
        TextView emptyMoodText = findViewById(R.id.emptyMoodText);
        if (moodList.isEmpty()) {
            emptyMoodText.setVisibility(View.VISIBLE);
        } else {
            emptyMoodText.setVisibility(View.GONE);
        }
    }  // <-- Added missing closing brace here

    private void handleMoodLoadFailure(Exception e) {
        Log.e("OtherProfileActivity", "Error loading moods", e);
        Toast.makeText(this, "Error loading moods", Toast.LENGTH_SHORT).show();
        updateMoodCount();
    }

    private void followUser() {
        // Validate user IDs
        if (currentUserId == null || searchedUserId == null || currentUserId.equals(searchedUserId)) {
            Toast.makeText(this, "Invalid operation.", Toast.LENGTH_SHORT).show();
            return;
        }

        WriteBatch batch = db.batch();

        DocumentReference followerRef = db.collection("users")
                .document(searchedUserId)
                .collection("followers")
                .document(currentUserId);
        batch.set(followerRef, new HashMap<String, Object>());

        DocumentReference followingRef = db.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(searchedUserId);
        batch.set(followingRef, new HashMap<String, Object>());

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
