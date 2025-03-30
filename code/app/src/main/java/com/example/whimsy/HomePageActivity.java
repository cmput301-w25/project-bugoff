package com.example.whimsy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class HomePageActivity extends ActivityBase {
    private static final String TAG = "HomePageActivity";
    private List<Mood> moodList;
    private List<String> moodDocIds;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initializeNavigation();

        // Inflate home page layout into content frame
        getLayoutInflater().inflate(R.layout.activity_home_page, findViewById(R.id.content_frame), true);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.moods_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodList = new ArrayList<>();
        moodDocIds = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            loadFollowedUsersMoods(userId);

            // Setup filter button
            ImageButton filterButton = findViewById(R.id.filter_button);
            filterButton.setOnClickListener(v -> showFilterPopup());

            // Real-time listener for followed moods
            db.collection("users").document(userId).collection("followedMoods")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.e(TAG, "Error listening to followed moods", e);
                            return;
                        }
                        Set<String> followedMoodsSet = new HashSet<>();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            String ownerUid = doc.getString("ownerUid");
                            String moodId = doc.getString("moodId");
                            followedMoodsSet.add(ownerUid + "_" + moodId);
                        }
                        moodAdapter.setFollowedMoodsSet(followedMoodsSet);
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
        }

        // Handle item touch events
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && gestureDetector.onTouchEvent(e)) {
                    int position = rv.getChildAdapterPosition(child);
                    if (position != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(HomePageActivity.this, MoodPageActivity.class);
                        intent.putExtra("SELECTED_MOOD", moodList.get(position));
                        intent.putExtra("MOOD_ID", moodDocIds.get(position));
                        intent.putExtra("OWNER_UID", moodList.get(position).getOwnerUid());
                        startActivity(intent);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /** Loads all moods from followed users without filters. */
    private void loadFollowedUsersMoods(String userId) {
        db.collection("users").document(userId).collection("following")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> followedUserIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        followedUserIds.add(doc.getId());
                    }
                    loadMoodsFromFollowedUsers(followedUserIds, 0, null, null);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load following list", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading following", e);
                });
    }

    /** Loads moods from followed users with optional filters. */
    /** Loads moods from followed users with optional filters, ensuring latest order. */
    private void loadMoodsFromFollowedUsers(List<String> followedUserIds, int days, String moodFilter, String searchFilter) {
        moodList.clear();
        moodDocIds.clear();

        if (followedUserIds.isEmpty()) {
            moodAdapter.notifyDataSetChanged();
            return;
        }

        final int totalUsers = followedUserIds.size();
        final AtomicInteger usersProcessed = new AtomicInteger(0);

        for (String followedId : followedUserIds) {
            db.collection("users").document(followedId).get()
                    .addOnSuccessListener(userDoc -> {
                        String profileImageUrl = userDoc.getString("profilePictureUrl");
                        String displayName = userDoc.getString("name");
                        String username = userDoc.getString("username");

                        db.collection("users").document(followedId).collection("moods")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(querySnapshots -> {
                                    for (DocumentSnapshot doc : querySnapshots) {
                                        String moodStr = doc.getString("mood");
                                        String locationName = doc.getString("locationName");
                                        Log.d(TAG, "moodStr: " + locationName);
                                        String timestampStr = doc.getString("timestamp");
                                        String trigger = doc.getString("trigger");
                                        String reason = doc.getString("reason");
                                        String imageUrl = doc.getString("imageUrl");
                                        boolean isPrivate = Boolean.TRUE.equals(doc.getBoolean("isPrivate"));
                                        List<Map<String, Object>> tags = (List<Map<String, Object>>) doc.get("tags");
                                        List<String> taggedUserNames = new ArrayList<>();
                                        if (tags != null) {
                                            for (Map<String, Object> tag : tags) {
                                                String tagName = (String) tag.get("name");
                                                if (tagName != null) taggedUserNames.add(tagName);
                                            }
                                        }
                                        String gatheringStatus = tags == null || tags.isEmpty() ? "Alone" :
                                                tags.size() == 1 ? "With 1 other" :
                                                        tags.size() <= 5 ? "With " + tags.size() + " others" : "With a crowd";

                                        Mood mood = new Mood(
                                                displayName != null ? displayName : "Unknown",
                                                username != null ? username : "Unknown",
                                                locationName != null ? locationName : "No location",
                                                timestampStr,
                                                timestampStr,
                                                gatheringStatus,
                                                "Feeling " + moodStr,
                                                trigger,
                                                reason,
                                                imageUrl,
                                                profileImageUrl,
                                                taggedUserNames,
                                                isPrivate
                                        );
                                        mood.setOwnerUid(followedId);
                                        mood.setMoodId(doc.getId());
                                        moodList.add(mood); // Add all moods without filtering yet
                                        moodDocIds.add(doc.getId());
                                    }

                                    // Check if all users' moods have been processed
                                    if (usersProcessed.incrementAndGet() == totalUsers) {
                                        applyFiltersAndSort(days, moodFilter, searchFilter);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error fetching moods for user " + followedId, e);
                                    if (usersProcessed.incrementAndGet() == totalUsers) {
                                        applyFiltersAndSort(days, moodFilter, searchFilter);
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user data for " + followedId, e);
                        if (usersProcessed.incrementAndGet() == totalUsers) {
                            applyFiltersAndSort(days, moodFilter, searchFilter);
                        }
                    });
        }
    }

    /** Applies filters and sorts the mood list by timestamp in descending order. */
    private void applyFiltersAndSort(int days, String moodFilter, String searchFilter) {
        long cutoffTimestamp = days > 0 ? calculateCutoffTimestamp(days) : 0;

        // Filter the moods
        List<Mood> filteredMoods = new ArrayList<>();
        for (Mood mood : moodList) {
            if (mood.isPrivate()) continue;

            long moodTimestamp = convertTimestampToMillis(mood.getTimestamp());
            boolean withinTimeRange = (days == 0) || (moodTimestamp >= cutoffTimestamp);
            boolean matchesMood = (moodFilter == null || moodFilter.equals("Select Mood")) ||
                    mood.getMoodStatus().substring("Feeling ".length()).equalsIgnoreCase(moodFilter);
            boolean matchesSearch = (searchFilter == null || searchFilter.isEmpty()) ||
                    (mood.getMoodReason() != null && mood.getMoodReason().toLowerCase().contains(searchFilter.toLowerCase()));

            if (withinTimeRange && matchesMood && matchesSearch) {
                filteredMoods.add(mood);
            }
        }

        // Sort by timestamp in descending order
        Collections.sort(filteredMoods, (m1, m2) -> {
            long t1 = convertTimestampToMillis(m1.getTimestamp());
            long t2 = convertTimestampToMillis(m2.getTimestamp());
            return Long.compare(t2, t1); // Descending order (most recent first)
        });

        // Update the moodList and notify the adapter
        moodList.clear();
        moodList.addAll(filteredMoods);
        moodDocIds.clear();
        for (Mood mood : filteredMoods) {
            moodDocIds.add(mood.getMoodId());
        }
        moodAdapter.notifyDataSetChanged();
    }

    /** Loads explicitly followed moods with optional filters. */
    private void loadFollowedMoodsFiltered(int days, String moodFilter, String searchFilter) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        moodList.clear();
        moodDocIds.clear();
        long cutoffTimestamp = days > 0 ? calculateCutoffTimestamp(days) : 0;

        db.collection("users").document(userId).collection("followedMoods")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<DocumentSnapshot> followedDocs = querySnapshot.getDocuments();
                    if (followedDocs.isEmpty()) {
                        moodAdapter.notifyDataSetChanged();
                        return;
                    }

                    for (DocumentSnapshot doc : followedDocs) {
                        String ownerUid = doc.getString("ownerUid");
                        String moodId = doc.getString("moodId");
                        if (ownerUid != null && moodId != null) {
                            db.collection("users").document(ownerUid).collection("moods").document(moodId)
                                    .get()
                                    .addOnSuccessListener(moodDoc -> {
                                        if (moodDoc.exists()) {
                                            String moodStr = moodDoc.getString("mood");
                                            String locationName = moodDoc.getString("location");
                                            String timestampStr = moodDoc.getString("timestamp");
                                            String trigger = moodDoc.getString("trigger");
                                            String reason = moodDoc.getString("reason");
                                            String imageUrl = moodDoc.getString("imageUrl");
                                            boolean isPrivate = Boolean.TRUE.equals(moodDoc.getBoolean("isPrivate"));
                                            List<Map<String, Object>> tags = (List<Map<String, Object>>) moodDoc.get("tags");
                                            List<String> taggedUserNames = new ArrayList<>();
                                            if (tags != null) {
                                                for (Map<String, Object> tag : tags) {
                                                    String tagName = (String) tag.get("name");
                                                    if (tagName != null) taggedUserNames.add(tagName);
                                                }
                                            }
                                            String gatheringStatus = tags == null || tags.isEmpty() ? "Alone" :
                                                    tags.size() == 1 ? "With 1 other" :
                                                            tags.size() <= 5 ? "With " + tags.size() + " others" : "With a crowd";

                                            db.collection("users").document(ownerUid).get()
                                                    .addOnSuccessListener(userDoc -> {
                                                        String profileImageUrl = userDoc.getString("profilePictureUrl");
                                                        String displayName = userDoc.getString("name");
                                                        String username = userDoc.getString("username");

                                                        long moodTimestamp = convertTimestampToMillis(timestampStr);
                                                        boolean withinTimeRange = (days == 0) || (moodTimestamp >= cutoffTimestamp);
                                                        boolean matchesMood = (moodFilter == null || moodFilter.equals("Select Mood")) ||
                                                                moodStr.equalsIgnoreCase(moodFilter);
                                                        boolean matchesSearch = (searchFilter == null || searchFilter.isEmpty()) ||
                                                                (reason != null && reason.toLowerCase().contains(searchFilter.toLowerCase()));

                                                        if (!isPrivate && withinTimeRange && matchesMood && matchesSearch) {
                                                            Mood mood = new Mood(
                                                                    displayName != null ? displayName : "Unknown",
                                                                    username != null ? username : "Unknown",
                                                                    locationName != null ? locationName : "No location",
                                                                    timestampStr,
                                                                    timestampStr,
                                                                    gatheringStatus,
                                                                    "Feeling " + moodStr,
                                                                    trigger,
                                                                    reason,
                                                                    imageUrl,
                                                                    profileImageUrl,
                                                                    taggedUserNames,
                                                                    isPrivate
                                                            );
                                                            mood.setOwnerUid(ownerUid);
                                                            mood.setMoodId(moodId);
                                                            moodList.add(mood);
                                                            moodDocIds.add(moodId);
                                                            moodAdapter.notifyDataSetChanged();
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                });
    }

    /** Shows the filter popup dialog. */
    private void showFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.filter_popup, null);
        builder.setView(popupView);
        AlertDialog filterDialog = builder.create();
        filterDialog.show();

        configureFilterDialog(filterDialog, popupView);
    }

    /** Configures the filter dialog UI and logic. */
    private void configureFilterDialog(AlertDialog filterDialog, View popupView) {
        RadioButton showAllMoods = popupView.findViewById(R.id.show_all_moods);
        RadioButton showFollowedMoods = popupView.findViewById(R.id.show_followed_moods);
        showAllMoods.setChecked(true); // Default to "All Moods"

        RadioButton filterWeek = popupView.findViewById(R.id.filter_week);
        RadioButton filterMonth = popupView.findViewById(R.id.filter_month);
        Spinner moodSpinner = popupView.findViewById(R.id.spinner_emotional_state);
        EditText searchBox = popupView.findViewById(R.id.search_reason_box);

        // Setup emotional state spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.emotional_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(adapter);

        Button applyButton = popupView.findViewById(R.id.apply_button);
        Button resetButton = popupView.findViewById(R.id.reset_button);

        applyButton.setOnClickListener(v -> {
            boolean showOnlyFollowedMoods = showFollowedMoods.isChecked();
            int days = filterWeek.isChecked() ? 7 : filterMonth.isChecked() ? 30 : 0;
            String selectedMood = moodSpinner.getSelectedItem().toString();
            String searchQuery = searchBox.getText().toString().trim();
            applyFilters(showOnlyFollowedMoods, days, selectedMood, searchQuery);
            filterDialog.dismiss();
        });

        resetButton.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                loadFollowedUsersMoods(user.getUid());
            }
            filterDialog.dismiss();
        });

        popupView.findViewById(R.id.close_popup).setOnClickListener(v -> filterDialog.dismiss());
    }

    /** Applies the selected filters. */
    private void applyFilters(boolean showOnlyFollowedMoods, int days, String moodFilter, String searchFilter) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        if (showOnlyFollowedMoods) {
            loadFollowedMoodsFiltered(days, moodFilter, searchFilter);
        } else {
            db.collection("users").document(userId).collection("following")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> followedUserIds = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            followedUserIds.add(doc.getId());
                        }
                        loadMoodsFromFollowedUsers(followedUserIds, days, moodFilter, searchFilter);
                    });
        }
    }

    /** Calculates the cutoff timestamp for time filtering. */
    private long calculateCutoffTimestamp(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar.getTimeInMillis();
    }

    /** Converts a timestamp string to milliseconds. */
    private long convertTimestampToMillis(String timestampStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a - MMMM dd, yyyy", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(timestampStr);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing timestamp: " + timestampStr, e);
            return 0;
        }
    }
}