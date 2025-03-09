package com.example.project1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project1.R; // Adjust to your package name
import com.example.project1.User; // Adjust to your package name
import com.example.project1.FollowingAdapter; // Adjust to your package name
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class FollowingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FollowingAdapter adapter;
    private List<User> userList;
    private String type;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.following);

        // Retrieve type and userId from intent
        type = getIntent().getStringExtra("type");
        userId = getIntent().getStringExtra("userId");

        // Validate intent extras
        if (type == null || userId == null) {
            Toast.makeText(this, "Invalid parameters", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the activity title based on type
        if (type.equals("followers")) {
            setTitle("Followers");
        } else if (type.equals("following")) {
            setTitle("Following");
        } else {
            setTitle("Unknown");
            Toast.makeText(this, "Invalid list type", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewFollowing);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new FollowingAdapter(userList, userId);
        recyclerView.setAdapter(adapter);

        // Fetch the list of users based on type and userId
        fetchUsersList(type, userId);
    }

    /**
     * Fetches the list of user IDs from the specified subcollection (followers or following).
     *
     * @param type   The type of list to fetch ("followers" or "following")
     * @param userId The ID of the user whose list is being fetched
     */
    private void fetchUsersList(String type, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Determine the subcollection based on type
        String subcollection = type.equals("followers") ? "followers" : "following";

        db.collection("users")
                .document(userId)
                .collection(subcollection)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> userIds = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        userIds.add(document.getId());
                    }

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
     * @param userIds The list of user IDs to fetch details for
     */
    private void fetchUserDetails(List<String> userIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userList.clear();

        for (String id : userIds) {
            db.collection("users")
                    .document(id)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        if (userDoc.exists()) {
                            String name = userDoc.getString("name");
                            String username = userDoc.getString("username");
                            String profilePictureUrl = userDoc.getString("profilePictureUrl");
                            userList.add(new User(id, name, username, profilePictureUrl));
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FollowingActivity", "Error fetching user details for " + id, e);
                    });
        }
    }
}