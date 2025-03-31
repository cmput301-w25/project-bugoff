package com.example.whimsy;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity for handling follow requests.
 */
public class FollowRequestsActivity extends ActivityBase {

    private RecyclerView recyclerView;
    private FollowRequestsAdapter adapter;
    private List<DocumentSnapshot> requestSnapshots = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    /**
     * Called when the activity is first created.
     * Initializes the RecyclerView, adapter, and loads follow requests.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_follow_requests, findViewById(R.id.content_frame), true);
        // 👇 Only use Firebase if we're not in test mode
        if (getIntent().hasExtra("testMode")) {
            return; // Skip Firebase logic in tests
        }

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recyclerView = findViewById(R.id.follow_requests_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FollowRequestsAdapter(requestSnapshots, new FollowRequestsAdapter.RequestActionListener() {
            @Override
            public void onAccept(DocumentSnapshot requestDoc) {
                acceptFollowRequest(requestDoc);
            }
            @Override
            public void onReject(DocumentSnapshot requestDoc) {
                rejectFollowRequest(requestDoc);
            }
        });
        recyclerView.setAdapter(adapter);

        loadFollowRequests();
    }

    /**
     * Loads follow requests from Firestore and updates the adapter.
     */
    private void loadFollowRequests() {
        db.collection("users").document(currentUserId)
                .collection("followRequests")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    requestSnapshots.clear();
                    requestSnapshots.addAll(querySnapshot.getDocuments());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading follow requests", Toast.LENGTH_SHORT).show();
                    Log.e("FollowRequests", "Error: ", e);
                });
    }

    /**
     * Accepts a follow request and updates Firestore.
     *
     * @param requestDoc The document snapshot of the follow request.
     */
    private void acceptFollowRequest(DocumentSnapshot requestDoc) {
        String requesterId = requestDoc.getId();
        WriteBatch batch = db.batch();

        batch.set(db.collection("users").document(currentUserId)
                .collection("followers").document(requesterId), new HashMap<String, Object>());
        batch.set(db.collection("users").document(requesterId)
                .collection("following").document(currentUserId), new HashMap<String, Object>());
        batch.delete(requestDoc.getReference());

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Follow request accepted", Toast.LENGTH_SHORT).show();
                    loadFollowRequests();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error accepting request", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Rejects a follow request and updates Firestore.
     *
     * @param requestDoc The document snapshot of the follow request.
     */
    private void rejectFollowRequest(DocumentSnapshot requestDoc) {
        requestDoc.getReference().delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Follow request rejected", Toast.LENGTH_SHORT).show();
                    loadFollowRequests();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error rejecting request", Toast.LENGTH_SHORT).show();
                });
    }
}