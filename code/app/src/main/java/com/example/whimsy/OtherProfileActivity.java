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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OtherProfileActivity extends ActivityBase {

    private ImageView profileImage;
    private TextView profileName, profileUsername, profileBio;
    private Button followButton;
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
        initializeNavigation();
        getLayoutInflater().inflate(R.layout.activity_other_profile, findViewById(R.id.content_frame), true);

        // Initialize UI components
        profileImage = findViewById(R.id.other_profile_image);
        profileName = findViewById(R.id.other_profile_name);
        profileUsername = findViewById(R.id.other_profile_email);
        profileBio = findViewById(R.id.other_profile_bio);
        followButton = findViewById(R.id.follow_btn);
        followersCount = findViewById(R.id.other_followers_count);
        followingCount = findViewById(R.id.other_following_count);
        moodCountText = findViewById(R.id.other_moods_count);
        moodsRecyclerView = findViewById(R.id.other_moods_recycler_view);

        // Set up RecyclerView
        moodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodList = new ArrayList<>();
        moodDocIds = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        moodsRecyclerView.setAdapter(moodAdapter);
        followButton.setOnClickListener(v -> followUser());

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Get the searched user ID from Intent
        searchedUserId = getIntent().getStringExtra("USER_ID");

        if (searchedUserId != null && !searchedUserId.isEmpty()) {
            loadUserData(searchedUserId);
            setupFollowCounts(searchedUserId);
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        profileName.setText(documentSnapshot.getString("name"));
                        profileUsername.setText("@" + documentSnapshot.getString("username"));
                        profileBio.setText(documentSnapshot.getString("bio"));

                        // NEW: Retrieve the isPrivate flag from user document
                        boolean isPrivate; // default
                        if (documentSnapshot.contains("isPrivate")) {
                            isPrivate = documentSnapshot.getBoolean("isPrivate");
                        } else {
                            isPrivate = false;
                        }

                        String imageUrl = documentSnapshot.getString("profilePictureUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(profileImage);
                        }

                        // Check if current user follows this account
                        db.collection("users").document(searchedUserId)
                                .collection("followers")
                                .document(currentUserId)
                                .get()
                                .addOnSuccessListener(followSnapshot -> {
                                    if (followSnapshot.exists()) {
                                        followButton.setText("Following");
                                        followButton.setEnabled(false);
                                        loadMoods(searchedUserId); // Load moods if already following
                                    } else {
                                        followButton.setText("Follow");
                                        followButton.setEnabled(true);
                                        // NEW: If account is private and not followed, do not load moods
                                        if (isPrivate) {
                                            TextView emptyMoodText = findViewById(R.id.emptyMoodText);
                                            emptyMoodText.setText("This account is private. Follow to view their moods"); // NEW: Changed text
                                            emptyMoodText.setVisibility(View.VISIBLE);
                                        } else {
                                            loadMoods(searchedUserId); // Load moods for public accounts
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("OtherProfileActivity", "Error checking follow status", e);
                                });
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OtherProfileActivity", "Error fetching user data", e);
                });
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
                            .limit(10) // Fetch more than 3 to account for private moods
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    int count = 0;
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        Boolean isPrivate = document.getBoolean("isPrivate");
                                        if (isPrivate == null || !isPrivate) {
                                            Mood moodObj = createMoodObject(document, name, username, profileImageUrl);
                                            moodList.add(moodObj);
                                            moodDocIds.add(document.getId());
                                            count++;
                                            if (count == 3) break; // Stop after adding 3 non-private moods
                                        }
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
        Boolean isPrivate = document.getBoolean("isPrivate");
        if (isPrivate == null) {
            isPrivate = false;
        }

        List<Map<String, Object>> tags = (List<Map<String, Object>>) document.get("tags");
        List<String> taggedUserNames = extractTaggedUserNames(tags);
        String gatheringStatus = calculateGatheringStatus(tags);

        return new Mood(
                name,
                username,
                locationName != null ? locationName : "No location",
                timestampStr,
                timestampStr,
                gatheringStatus,
                "Feeling " + mood,
                trigger,
                reason,
                imageUrl,
                profileImageUrl,
                taggedUserNames,
                isPrivate
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
    }
    private void handleMoodLoadFailure(Exception e) {
        Log.e("OtherProfileActivity", "Error loading moods", e);
        Toast.makeText(this, "Error loading moods", Toast.LENGTH_SHORT).show();
        updateMoodCount();
    }

    private void followUser() {
        // Prevent self-following with a clear message
        if (currentUserId == null || searchedUserId == null || currentUserId.equals(searchedUserId)) {
            Toast.makeText(this, "You cannot follow yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable the button to prevent multiple clicks during processing
        followButton.setEnabled(false);

        db.collection("users").document(searchedUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isPrivate = documentSnapshot.exists() &&
                            documentSnapshot.contains("isPrivate") &&
                            documentSnapshot.getBoolean("isPrivate");

                    if (isPrivate) {
                        // Handle follow request for private accounts
                        db.collection("users").document(searchedUserId)
                                .collection("followRequests").document(currentUserId)
                                .get()
                                .addOnSuccessListener(requestDoc -> {
                                    if (requestDoc.exists()) {
                                        // Cancel existing follow request
                                        requestDoc.getReference().delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    followButton.setText("Follow");
                                                    followButton.setEnabled(true);
                                                    Toast.makeText(this, "Follow request cancelled", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    followButton.setEnabled(true);
                                                    Toast.makeText(this, "Failed to cancel request. Please try again.", Toast.LENGTH_SHORT).show();
                                                    Log.e("FollowUser", "Error cancelling request", e);
                                                });
                                    } else {
                                        // Send new follow request
                                        Map<String, Object> requestData = new HashMap<>();
                                        requestData.put("timestamp", FieldValue.serverTimestamp());
                                        db.collection("users").document(searchedUserId)
                                                .collection("followRequests").document(currentUserId)
                                                .set(requestData)
                                                .addOnSuccessListener(aVoid -> {
                                                    followButton.setText("Requested");
                                                    followButton.setEnabled(true); // Allow cancellation
                                                    Toast.makeText(this, "Follow request sent", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    followButton.setEnabled(true);
                                                    Toast.makeText(this, "Failed to send request. Please try again.", Toast.LENGTH_SHORT).show();
                                                    Log.e("FollowUser", "Error sending request", e);
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    followButton.setEnabled(true);
                                    Toast.makeText(this, "Error checking request status.", Toast.LENGTH_SHORT).show();
                                    Log.e("FollowUser", "Error fetching request doc", e);
                                });
                    } else {
                        // Follow public accounts immediately
                        WriteBatch batch = db.batch();
                        batch.set(db.collection("users").document(searchedUserId)
                                .collection("followers").document(currentUserId), new HashMap<>());
                        batch.set(db.collection("users").document(currentUserId)
                                .collection("following").document(searchedUserId), new HashMap<>());
                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    followButton.setText("Following");
                                    followButton.setEnabled(false); // Permanently disable after following
                                    Toast.makeText(this, "Followed successfully!", Toast.LENGTH_SHORT).show();
                                    showSuggestedUsers();
                                })
                                .addOnFailureListener(e -> {
                                    followButton.setEnabled(true);
                                    Toast.makeText(this, "Failed to follow. Please try again.", Toast.LENGTH_SHORT).show();
                                    Log.e("FollowUser", "Error following user", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    followButton.setEnabled(true);
                    Toast.makeText(this, "Error retrieving user info. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("FollowUser", "Error fetching user doc", e);
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