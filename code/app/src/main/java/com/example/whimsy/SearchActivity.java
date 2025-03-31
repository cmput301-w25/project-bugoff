/**
 * SearchActivity allows the user to search for other users by name and view the results in a RecyclerView.
 * It uses Firebase Firestore to fetch user data based on the search query and displays the results in real-time
 * as the user types in the search box.
 * This activity includes functionality to handle user input, query the database, and display search results.
 */

package com.example.whimsy;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private ImageView searchIcon;
    private List<User> userList;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes Firestore, sets up the RecyclerView for search results,
     * and adds a TextWatcher to the search EditText for real-time searching.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initializeNavigation();

        // Inflate the layout for the search box into the content frame
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.search_box, contentFrame, true);

        // Retrieve views for the search input and results RecyclerView
        searchEditText = contentFrame.findViewById(R.id.search_edit_text);
        searchIcon = contentFrame.findViewById(R.id.search_icon);
        recommendationsRecyclerView = contentFrame.findViewById(R.id.search_results_recycler_view);

        // Set up RecyclerView for displaying search recommendations
        userList = new ArrayList<>();
        adapter = new SearchResultAdapter(this, userList);
        recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendationsRecyclerView.setAdapter(adapter);

        // Initialize Firestore instance for querying user data
        db = FirebaseFirestore.getInstance();

        // Add TextWatcher to searchEditText for live search results
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Placeholder for before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Trigger search as the user types in the search box
                searchIcon.setColorFilter(getColor(R.color.black));
                if (s.toString().isEmpty()) {
                    searchIcon.clearColorFilter();
                }
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Placeholder for after text changes
            }
        });
    }

    /**
     * Performs a search query in Firestore based on the input string.
     * If the search query is empty, it clears the list of results and hides the RecyclerView.
     * If users are found, it adds them to the list and updates the RecyclerView.
     *
     * @param query The search query entered by the user.
     */
    private void performSearch(String query) {
        if (query.isEmpty()) {
            userList.clear(); // Clear the previous search results
            adapter.notifyDataSetChanged();
            recommendationsRecyclerView.setVisibility(RecyclerView.GONE); // Hide the RecyclerView when there's no query
            return;
        }

        // Clear previous search results and show the RecyclerView
        userList.clear();
        adapter.notifyDataSetChanged();
        recommendationsRecyclerView.setVisibility(RecyclerView.VISIBLE);

        // Query Firestore for users where the "name" field starts with the search query
        db.collection("users")
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff") // Query range to include all users whose names start with the query
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId();
                            String username = doc.getString("username");
                            String displayName = doc.getString("name");
                            String profilePicUrl = doc.getString("profilePictureUrl"); // Fetch the profile picture URL

                            // Add the user to the list of results
                            userList.add(new User(id, username, displayName, profilePicUrl));
                        }
                        adapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SearchActivity", "Error performing search", e);
                    showSnackbar("Error performing search");
                });
    }
}
