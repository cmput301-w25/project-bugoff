/**
 * The `HomePageActivity` class represents the main screen where users can view mood updates from the
 * people they follow. It retrieves mood data from Firebase Firestore and displays it using a RecyclerView.
 * Users can tap on a mood entry to navigate to a detailed mood page.
 *
 * Key Features:
 * - Fetches followed users from Firebase Firestore.
 * - Retrieves and displays mood entries sorted by timestamp.
 * - Displays user details, mood descriptions, triggers, reasons, and tagged users.
 * - Implements touch interaction to navigate to the selected mood's details.
 *
 * Outstanding Issues:
 * - Performance optimizations may be required for large datasets.
 * - Error handling for Firebase failures could be improved for better user experience.
 */

package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HomePageActivity is responsible for displaying the moods of users followed by the current user.
 * It retrieves mood data from Firestore and displays it in a RecyclerView.
 */
public class HomePageActivity extends ActivityBase {
    private static final String TAG = "HomePageActivity";
    private List<Mood> moodList;
    private List<String> moodDocIds;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;

    /**
     * Initializes the activity, sets up UI components, and loads the moods of followed users.
     *
     * @param savedInstanceState The saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initializeNavigation();

        // Inflate home page layout into content frame
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_home_page, contentFrame, true);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.moods_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moodList = new ArrayList<>();
        moodDocIds = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);

        // Initialize Firebase authentication and database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadFollowedUsersMoods(user.getUid());
        } else {
            Log.e(TAG, "User is not authenticated");
        }

        // Handle item touch events to open the selected mood's details
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && e.getAction() == MotionEvent.ACTION_UP) {
                    int position = rv.getChildAdapterPosition(child);
                    if (position != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(HomePageActivity.this, MoodPageActivity.class);
                        intent.putExtra("SELECTED_MOOD", moodList.get(position));
                        intent.putExtra("MOOD_ID", moodDocIds.get(position));
                        startActivity(intent);
                    }
                }
                return false;
            }
        });
    }

    /**
     * Loads the moods of users followed by the given user ID.
     *
     * @param userId The ID of the current user.
     */
    private void loadFollowedUsersMoods(String userId) {
        db.collection("users").document(userId).collection("following")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> followedUserIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        followedUserIds.add(doc.getId());
                    }
                    loadMoodsFromFollowedUsers(followedUserIds);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load following list", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading following", e);
                });
    }

    /**
     * Retrieves and displays moods posted by the followed users.
     *
     * @param followedUserIds A list of user IDs that the current user follows.
     */
    private void loadMoodsFromFollowedUsers(List<String> followedUserIds) {
        moodList.clear();
        moodDocIds.clear();
        for (String followedId : followedUserIds) {
            db.collection("users").document(followedId).get()
                    .addOnSuccessListener(userDoc -> {
                        // Extract user details
                        String profileImageUrl = userDoc.getString("profilePictureUrl");
                        String displayName = userDoc.getString("name");
                        String userEmail = userDoc.getString("username"); // Assuming "username" stores email

                        // Retrieve moods for this user
                        db.collection("users").document(followedId)
                                .collection("moods")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(querySnapshots -> {
                                    for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                                        // Extract mood data
                                        String moodStr = doc.getString("mood");
                                        String locationName = doc.getString("location");
                                        String timestampStr = doc.getString("timestamp");
                                        String trigger = doc.getString("trigger");
                                        String reason = doc.getString("reason");
                                        String imageUrl = doc.getString("imageUrl");

                                        // Extract tagged users
                                        List<Map<String, Object>> tags = (List<Map<String, Object>>) doc.get("tags");
                                        List<String> taggedUserNames = new ArrayList<>();
                                        if (tags != null) {
                                            for (Map<String, Object> tag : tags) {
                                                String username = (String) tag.get("name");
                                                if (username != null) {
                                                    taggedUserNames.add(username);
                                                }
                                            }
                                        }

                                        // Determine gathering status
                                        String gatheringStatus;
                                        if (tags == null || tags.isEmpty()) {
                                            gatheringStatus = "Alone";
                                        } else if (tags.size() == 1) {
                                            gatheringStatus = "With 1 other";
                                        } else if (tags.size() <= 5) {
                                            gatheringStatus = "With " + tags.size() + " others";
                                        } else {
                                            gatheringStatus = "With a crowd";
                                        }

                                        // Create Mood object and add to the list
                                        Mood mood = new Mood(
                                                (displayName != null ? displayName : "Unknown"),
                                                (userEmail != null ? userEmail : "Unknown"),
                                                (locationName != null ? locationName : "No location"),
                                                timestampStr,
                                                timestampStr,
                                                gatheringStatus,
                                                "Feeling " + moodStr,
                                                trigger,
                                                reason,
                                                imageUrl,
                                                profileImageUrl,
                                                taggedUserNames
                                        );
                                        moodList.add(mood);
                                        moodDocIds.add(doc.getId());
                                    }
                                    moodAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load moods", Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show());
        }
    }
}
