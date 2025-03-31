package com.example.whimsy;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowRequestsActivity extends ActivityBase {

    // NEW: Declare RecyclerView and adapter for follow requests.
    private RecyclerView recyclerView;
    private FollowRequestsAdapter adapter;
    private List<DocumentSnapshot> requestSnapshots = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the add_mood.xml layout into the content frame.
        getLayoutInflater().inflate(R.layout.activity_follow_requests, findViewById(R.id.content_frame), true);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recyclerView = findViewById(R.id.follow_requests_recycler_view); // NEW: Ensure layout has this RecyclerView
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

    private void acceptFollowRequest(DocumentSnapshot requestDoc) {
        String requesterId = requestDoc.getId();
        WriteBatch batch = db.batch();

        // NEW: Add requester to current user's followers.
        batch.set(db.collection("users").document(currentUserId)
                .collection("followers").document(requesterId), new HashMap<String, Object>());
        // NEW: Add current user to requester's following.
        batch.set(db.collection("users").document(requesterId)
                .collection("following").document(currentUserId), new HashMap<String, Object>());
        // NEW: Delete the follow request.
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
