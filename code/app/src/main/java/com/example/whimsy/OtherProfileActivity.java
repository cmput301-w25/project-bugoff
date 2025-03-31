package com.example.whimsy;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * <h1>OtherProfileActivity</h1>
 * <p>
 * The {@code OtherProfileActivity} handles the display of another user's profile and allows
 * the current user to follow or unfollow that user. This activity loads the profile information
 * of the searched user (including name, email, bio, and profile picture) and displays the follow
 * button state based on whether the current user is already following the searched user.
 * </p>
 * <p>
 * <strong>Usage:</strong> Launch this activity with an {@code Intent} containing the extra
 * "USER_ID" to view another user's profile. The activity manages loading profile data,
 * mood posts, follow counts, and handling follow/unfollow actions.
 * </p>
 *
 * @see ActivityBase
 * @see FirebaseFirestore
 * @see FirebaseAuth
 * @see MoodAdapter
 * @version 1.0
 */
public class OtherProfileActivity extends ActivityBase {

    // UI Components
    private ImageView profileImage;
    private TextView profileName, profileUsername, profileBio;
    private Button followButton;
    private TextView followersCount, followingCount;
    private TextView moodCountText;
    private RecyclerView moodsRecyclerView;
    private ImageView backBtn;

    // Data and Adapters
    private String searchedUserId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList = new ArrayList<>();
    private List<String> moodDocIds = new ArrayList<>();

    /**
     * Called when the activity is starting. This method sets up the UI components,
     * initializes Firebase instances, loads user data and moods, and sets up touch listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
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
        backBtn = findViewById(R.id.tool_back_button);
        backBtn.setVisibility(View.VISIBLE);

        // Back button click finishes the activity
        backBtn.setOnClickListener(v -> finish());

        // Set up RecyclerView for moods
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

        // Listen for changes in followed moods and update the adapter accordingly
        db.collection("users").document(currentUserId).collection("followedMoods")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("OtherProfileActivity", "Error listening to followed moods", e);
                        return;
                    }
                    Set<String> followedMoodsSet = new HashSet<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String ownerUid = doc.getString("ownerUid");
                            String moodId = doc.getString("moodId");
                            followedMoodsSet.add(ownerUid + "_" + moodId);
                        }
                    }
                    moodAdapter.setFollowedMoodsSet(followedMoodsSet);
                });

        // Retrieve the searched user ID from the Intent
        searchedUserId = getIntent().getStringExtra("USER_ID");

        if (searchedUserId != null && !searchedUserId.isEmpty()) {
            loadUserData(searchedUserId);
            setupFollowCounts(searchedUserId);
        } else {
            Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Add touch listener to RecyclerView for mood item selection
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
        moodsRecyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && gestureDetector.onTouchEvent(e)) {
                    int position = rv.getChildAdapterPosition(child);
                    if (position != RecyclerView.NO_POSITION) {
                        navigateToMoodPage(position);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Loads the user data for the specified user ID and updates the UI components.
     *
     * @param userId The ID of the user whose data is to be loaded.
     */
    private void loadUserData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        profileName.setText(documentSnapshot.getString("name"));
                        profileUsername.setText("@" + documentSnapshot.getString("username"));
                        profileBio.setText(documentSnapshot.getString("bio"));

                        // Retrieve the isPrivate flag from the user document (default to false if absent)
                        boolean isPrivate;
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
                                        // If the account is private and not followed, display a message instead of loading moods
                                        if (isPrivate) {
                                            TextView emptyMoodText = findViewById(R.id.emptyMoodText);
                                            emptyMoodText.setText(Html.fromHtml("<b>This account is private.</b><br>Follow to view their moods"));
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

    /**
     * Sets up the follow counts for the specified user ID and attaches click listeners to
     * navigate to the following or followers pages.
     *
     * @param userId The ID of the user whose follow counts are to be set up.
     */
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

    /**
     * Loads the moods for the specified user ID by querying the "moods" collection,
     * filters out private moods, and updates the UI.
     *
     * @param userId The ID of the user whose moods are to be loaded.
     */
    private void loadMoods(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(userDoc -> {
                    String profileImageUrl = userDoc.getString("profilePictureUrl");
                    String name = userDoc.getString("name");
                    String username = userDoc.getString("username");

                    moodList.clear(); // Clear existing moods

                    db.collection("users").document(userId).collection("moods")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(10)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    int count = 0;
                                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                        Boolean isPrivate = document.getBoolean("isPrivate");
                                        if (isPrivate == null || !isPrivate) {
                                            Mood moodObj = createMoodObject(document, name, username, profileImageUrl, userDoc.getId());
                                            moodObj.setMoodId(document.getId()); // Store mood ID in the Mood object
                                            moodList.add(moodObj);
                                            count++;
                                            if (count == 3) break; // Limit to 3 non-private moods
                                        }
                                    }
                                    sortAndUpdateMoods(); // Sort moods and update the UI
                                } else {
                                    updateMoodCount();
                                }
                            })
                            .addOnFailureListener(e -> handleMoodLoadFailure(e));
                });
    }

    /**
     * Creates a {@code Mood} object from the given DocumentSnapshot and user details.
     *
     * @param document        The DocumentSnapshot containing mood data.
     * @param name            The name of the user.
     * @param username        The username of the user.
     * @param profileImageUrl The profile image URL of the user.
     * @param ownerUid        The UID of the mood owner.
     * @return A {@code Mood} object populated with the provided data.
     */
    private Mood createMoodObject(DocumentSnapshot document, String name, String username, String profileImageUrl, String ownerUid) {
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

        Mood moodObj = new Mood(
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
        moodObj.setOwnerUid(ownerUid);
        return moodObj;
    }

    /**
     * Calculates the gathering status based on the number of tagged users.
     *
     * @param tags A list of tag maps from Firestore.
     * @return A String representing the gathering status (e.g., "Alone", "With 2 others").
     */
    private String calculateGatheringStatus(List<Map<String, Object>> tags) {
        if (tags == null || tags.isEmpty()) return "Alone";
        int tagCount = tags.size();
        if (tagCount == 1) return "With 1 other";
        if (tagCount <= 5) return "With " + tagCount + " others";
        return "With a crowd";
    }

    /**
     * Extracts tagged user names from the provided list of tag maps.
     *
     * @param tags A list of tag maps from Firestore.
     * @return A list of tagged user names.
     */
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

    /**
     * Converts a timestamp string into milliseconds.
     *
     * @param timestampStr The timestamp string in the format "hh:mm a - MMMM dd, yyyy".
     * @return The timestamp in milliseconds, or 0 if parsing fails.
     */
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

    /**
     * Navigates to the MoodPageActivity for the selected mood.
     *
     * @param position The position of the selected mood in the list.
     */
    private void navigateToMoodPage(int position) {
        Mood selectedMood = moodList.get(position);
        Intent intent = new Intent(this, MoodPageActivity.class);
        intent.putExtra("SELECTED_MOOD", selectedMood);
        intent.putExtra("MOOD_ID", selectedMood.getMoodId());
        intent.putExtra("OWNER_UID", selectedMood.getOwnerUid());
        startActivity(intent);
    }

    /**
     * Sorts the list of moods by timestamp (most recent first) and updates the RecyclerView.
     */
    private void sortAndUpdateMoods() {
        Collections.sort(moodList, (m1, m2) ->
                Long.compare(convertTimestampToMillis(m2.getTimestamp()), convertTimestampToMillis(m1.getTimestamp())));
        moodAdapter.notifyDataSetChanged();
        updateMoodCount();
    }

    /**
     * Updates the mood count text and toggles the visibility of the "empty mood" message.
     */
    private void updateMoodCount() {
        moodCountText.setText(String.valueOf(moodList.size()));
        TextView emptyMoodText = findViewById(R.id.emptyMoodText);
        if (moodList.isEmpty()) {
            emptyMoodText.setVisibility(View.VISIBLE);
        } else {
            emptyMoodText.setVisibility(View.GONE);
        }
    }

    /**
     * Handles failures during mood loading by logging the error and displaying a toast message.
     *
     * @param e The Exception that occurred during mood loading.
     */
    private void handleMoodLoadFailure(Exception e) {
        Log.e("OtherProfileActivity", "Error loading moods", e);
        Toast.makeText(this, "Error loading moods", Toast.LENGTH_SHORT).show();
        updateMoodCount();
    }

    /**
     * Handles the follow/unfollow process when the follow button is clicked.
     * <p>
     * This method prevents self-following and either sends a follow request for private accounts
     * or immediately follows public accounts. It updates the follow button state accordingly.
     * </p>
     */
    private void followUser() {
        // Prevent self-following with a clear message
        if (currentUserId == null || searchedUserId == null || currentUserId.equals(searchedUserId)) {
            Toast.makeText(this, "You cannot follow yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable the button to prevent multiple clicks
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

    /**
     * Fetches a list of suggested users for the current user to follow.
     * <p>
     * The suggestions exclude the current user, the searched user, and any users that the current user is already following.
     * </p>
     */
    private void showSuggestedUsers() {
        db.collection("users")
                .limit(10) // Increased limit to obtain more users before filtering
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

                                    // Exclude self, searched user, and already followed users
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

    /**
     * Displays the list of suggested users in the UI.
     *
     * @param users An ArrayList of {@code User} objects representing the suggested users.
     */
    private void displaySuggestions(ArrayList<User> users) {
        LinearLayout suggestionsContainer = findViewById(R.id.suggestions_container);
        TextView suggestionsTitle = findViewById(R.id.suggestions_title);
        View suggestionsScroll = findViewById(R.id.suggestions_scroll);
        suggestionsContainer.removeAllViews(); // Clear any existing suggestions
        suggestionsContainer.setVisibility(View.VISIBLE);
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
                intent.putExtra("USER_ID", user.getId());
                startActivity(intent);
            });

            suggestionsContainer.addView(userView);
        }
    }

    /**
     * Follows a suggested user and updates the state of the corresponding follow button.
     *
     * @param userId The ID of the user to be followed.
     * @param button The {@code Button} that triggered the follow action.
     */
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
