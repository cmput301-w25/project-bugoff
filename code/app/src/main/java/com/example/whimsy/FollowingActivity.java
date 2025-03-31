/**
 * FollowingActivity is responsible for displaying a list of users
 * that a specific user follows or is followed by.
 * It retrieves the user list from Firestore, differentiating
 * between "followers" and "following" based on the provided intent data.
 */

package com.example.whimsy;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FollowingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FollowingAdapter adapter;
    private List<User> userList;
    private String type;  // Type of list ("followers" or "following")
    private String userId; // The ID of the user whose list is being viewed
    private TextView following_followers_title;
    private ImageView back_button;

    /**
     * Called when the activity is first created.
     * Initializes the RecyclerView, retrieves intent extras, and fetches user data.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra("testMode")) {
            setContentView(R.layout.following); // 👈 still inflate layout for test
            return; // 👈 skip all Firebase logic
        }
        setContentView(R.layout.following); // Set the UI layout for the activity

        // Retrieve type and userId from the intent
        type = getIntent().getStringExtra("type");
        userId = getIntent().getStringExtra("userId");

        // Validate intent extras to ensure proper functionality
        if (type == null || userId == null) {
            Toast.makeText(this, "Invalid parameters", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the activity title based on whether viewing followers or following
        following_followers_title = findViewById(R.id.following_followers_title);
        back_button = findViewById(R.id.back_button);

        // Set the activity title based on type
        if (type.equals("followers")) {
            following_followers_title.setText("Followers");
        } else if (type.equals("following")) {
            following_followers_title.setText("Following");
        } else {
            setTitle("Unknown");
            Toast.makeText(this, "Invalid list type", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewFollowing);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager for list display
        userList = new ArrayList<>();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();  // NEW: Get current user id
        boolean isSelf = userId.equals(currentUserId);  // NEW: Flag to indicate if viewing own following list
        // EDIT: Pass currentUserId and isSelf flag to the adapter
        adapter = new FollowingAdapter(userList, currentUserId, isSelf);  // EDIT
        recyclerView.setAdapter(adapter);


        // Fetch the user list from Firestore
        fetchUsersList(type, userId);

        back_button.setOnClickListener(v -> finish());
    }

    /**
     * Fetches the list of user IDs from the specified subcollection ("followers" or "following").
     *
     * @param type   The type of list to fetch ("followers" or "following").
     * @param userId The ID of the user whose list is being fetched.
     */
    private void fetchUsersList(String type, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Determine the Firestore subcollection to query
        String subcollection = type.equals("followers") ? "followers" : "following";

        db.collection("users")
                .document(userId)
                .collection(subcollection)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> userIds = new ArrayList<>();

                    // Iterate over the retrieved documents and extract user IDs
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        userIds.add(document.getId());
                    }

                    // If users exist, fetch their details; otherwise, show a message
                    if (!userIds.isEmpty()) {
                        fetchUserDetails(userIds);
                    } else {
                        Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FollowingActivity", "Error fetching " + subcollection + " list", e);
                    Toast.makeText(this, "Failed to load " + subcollection + " list", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches user details for the given list of user IDs and updates the RecyclerView.
     *
     * @param userIds The list of user IDs whose details need to be retrieved.
     */
    private void fetchUserDetails(List<String> userIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userList.clear(); // Clear the current list before adding new data

        // Fetch details for each user ID in the list
        for (String id : userIds) {
            db.collection("users")
                    .document(id)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            // Extract user details from Firestore document
                            String name = userDoc.getString("name");
                            String username = userDoc.getString("username");
                            String profilePictureUrl = userDoc.getString("profilePictureUrl");

                            // Create a new User object and add it to the list
                            userList.add(new User(id, name, username, profilePictureUrl));

                            // Notify the adapter that data has changed to refresh the UI
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FollowingActivity", "Error fetching user details for " + id, e);
                    });
        }
    }
}
