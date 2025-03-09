package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    private RecyclerView recommendationsRecyclerView;
    private SearchResultAdapter adapter;
    private List<User> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Inflate search_box.xml into the FrameLayout container
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.search_box, contentFrame, true);

        // Retrieve views from search_box.xml
        searchEditText = contentFrame.findViewById(R.id.search_edit_text);
        recommendationsRecyclerView = contentFrame.findViewById(R.id.search_results_recycler_view);

        // Setup RecyclerView for search recommendations
        userList = new ArrayList<>();
        adapter = new SearchResultAdapter(this, userList);
        recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendationsRecyclerView.setAdapter(adapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // ✅ Manually initialize bottom navigation buttons
        findViewById(R.id.home).setOnClickListener(v -> startActivity(new Intent(this, HomePageActivity.class)));
        findViewById(R.id.search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class))); // Redundant, but keeps it consistent
        //findViewById(R.id.add).setOnClickListener(v -> startActivity(new Intent(this, AddPostActivity.class)));
        //findViewById(R.id.heart).setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));
        findViewById(R.id.profile_button).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));


        // Add TextWatcher to searchEditText for live search results
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Placeholder for before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Trigger search as user types
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Placeholder for after text changes
            }
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            userList.clear();
            adapter.notifyDataSetChanged();
            recommendationsRecyclerView.setVisibility(RecyclerView.GONE);
            return;
        }

        // Clear previous results and show RecyclerView
        userList.clear();
        adapter.notifyDataSetChanged();
        recommendationsRecyclerView.setVisibility(RecyclerView.VISIBLE);

        // Query Firestore for users where "name" starts with the search query
        db.collection("users")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(SearchActivity.this, "No users found.", Toast.LENGTH_SHORT).show();
                    } else {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String username = doc.getString("username");
                            String displayName = doc.getString("name");
                            String profilePicUrl = doc.getString("profilePictureUrl"); // 🔹 Fetch profile picture URL

                            userList.add(new User(id, username, displayName, profilePicUrl)); // 🔹 Pass it to the User object
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SearchActivity", "Error performing search", e);
                    Toast.makeText(SearchActivity.this, "Error performing search", Toast.LENGTH_SHORT).show();
                });
    }
}
