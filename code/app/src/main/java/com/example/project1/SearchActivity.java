package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends ActivityBase {

    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView recommendationsRecyclerView;
    private SearchResultAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Step 1: Load the base layout with header and footer
        setContentView(R.layout.activity_base);

        // Step 2: Inflate search_box.xml into the FrameLayout container (content_frame)
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.search_box, contentFrame, true);

        // Step 3: Retrieve views from search_box.xml
        searchEditText = contentFrame.findViewById(R.id.search_edit_text);
        searchButton = contentFrame.findViewById(R.id.search_button);
        recommendationsRecyclerView = contentFrame.findViewById(R.id.search_results_recycler_view);

        // Step 4: Setup RecyclerView for search recommendations
        userList = new ArrayList<>();
        adapter = new SearchResultAdapter(this, userList);
        recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendationsRecyclerView.setAdapter(adapter);

        // Initially hide the RecyclerView until results are available
        recommendationsRecyclerView.setVisibility(RecyclerView.GONE);

        // Step 5: Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Step 6: Set click listener for the search button to perform search query
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(SearchActivity.this, "Please enter a username!", Toast.LENGTH_SHORT).show();
            } else {
                performSearch(query);
            }
        });
    }

    private void performSearch(String query) {
        // Clear previous results and hide RecyclerView
        userList.clear();
        adapter.notifyDataSetChanged();
        recommendationsRecyclerView.setVisibility(RecyclerView.GONE);

        // Query Firestore for user documents where the "username" field matches (or starts with) the query
        db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(SearchActivity.this, "No users found.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String username = doc.getString("username");
                            String displayName = doc.getString("name");
                            User user = new User(id, username, displayName);
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                        // Make the RecyclerView visible now that we have data
                        recommendationsRecyclerView.setVisibility(RecyclerView.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SearchActivity", "Error performing search", e);
                    Toast.makeText(SearchActivity.this, "Error performing search", Toast.LENGTH_SHORT).show();
                });
    }
}