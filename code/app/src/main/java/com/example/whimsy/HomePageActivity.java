package com.example.whimsy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
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

/**
 * HomePageActivity displays a list of moods and allows users to filter the results.
 * This activity retrieves moods from followed users, applies filters, and retains filter settings between popup openings.
 * Snackbar messages are displayed when filters are applied or reset.
 */
public class HomePageActivity extends ActivityBase {
    private static final String TAG = "HomePageActivity";
    private List<Mood> moodList;
    private List<String> moodDocIds;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;

    // Instance variables to store current filter state
    private boolean currentShowOnlyFollowedMoods = false;
    private int currentDays = 0; // 0 indicates no time filter
    private String currentMoodFilter = "Select Mood";
    private String currentSearchFilter = "";

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

        final ConstraintLayout constraintLayout = findViewById(R.id.constraintLayout);
        final LinearLayout buttonPanel = findViewById(R.id.buttonPanel);

        // Hide or show the button panel based on scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && buttonPanel.getVisibility() == View.VISIBLE) {
                    // Hide the panel and update RecyclerView constraint
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);
                    constraintSet.clear(R.id.moods_recycler_view, ConstraintSet.TOP);
                    constraintSet.connect(R.id.moods_recycler_view, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0);
                    TransitionManager.beginDelayedTransition(constraintLayout);
                    constraintSet.applyTo(constraintLayout);
                    buttonPanel.setVisibility(View.GONE);
                } else if (dy < 0 && buttonPanel.getVisibility() != View.VISIBLE) {
                    // Show the panel and update RecyclerView constraint
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);
                    constraintSet.clear(R.id.moods_recycler_view, ConstraintSet.TOP);
                    constraintSet.connect(R.id.moods_recycler_view, ConstraintSet.TOP, R.id.buttonPanel, ConstraintSet.BOTTOM, 0);
                    TransitionManager.beginDelayedTransition(constraintLayout);
                    constraintSet.applyTo(constraintLayout);
                    buttonPanel.setVisibility(View.VISIBLE);
                }
            }
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            loadFollowedUsersMoods(userId);

            // Setup filter button
            Button filterButton = findViewById(R.id.filter_button);
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

        // Handle item touch events for RecyclerView
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

    /**
     * Loads all moods from followed users without any filters.
     *
     * @param userId The current user's ID.
     */
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
                    showSnackbar("Failed to load following list");
                    Log.e(TAG, "Error loading following", e);
                });
    }

    /**
     * Loads moods from followed users with optional filters.
     *
     * @param followedUserIds List of user IDs being followed.
     * @param days            Number of days for time filtering.
     * @param moodFilter      Filter for the mood state.
     * @param searchFilter    Filter for the search query.
     */
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

    /**
     * Applies the given filters and sorts the mood list by timestamp in descending order.
     *
     * @param days         Number of days for time filtering.
     * @param moodFilter   Filter for the mood state.
     * @param searchFilter Filter for the search query.
     */
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

    /**
     * Loads explicitly followed moods with optional filters.
     *
     * @param days         Number of days for time filtering.
     * @param moodFilter   Filter for the mood state.
     * @param searchFilter Filter for the search query.
     */
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

    /**
     * Displays the filter popup dialog.
     */
    private void showFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.filter_popup, null);
        builder.setView(popupView);
        AlertDialog filterDialog = builder.create();
        if (filterDialog.getWindow() != null) {
            filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            filterDialog.getWindow().setGravity(Gravity.CENTER);
        }
        filterDialog.show();

        configureFilterDialog(filterDialog, popupView);
    }

    /**
     * Configures the filter popup dialog's UI and logic.
     * Initializes UI components with any previously applied filter settings and shows Snackbar messages when filters are applied or reset.
     *
     * @param filterDialog The AlertDialog for the filter popup.
     * @param popupView    The inflated layout view for the filter popup.
     */
    private void configureFilterDialog(AlertDialog filterDialog, View popupView) {
        // Initialize the radio buttons for showing all moods or only followed moods
        RadioButton showAllMoods = popupView.findViewById(R.id.show_all_moods);
        RadioButton showFollowedMoods = popupView.findViewById(R.id.show_followed_moods);
        // Set initial state from stored filter settings
        showFollowedMoods.setChecked(currentShowOnlyFollowedMoods);
        showAllMoods.setChecked(!currentShowOnlyFollowedMoods);

        // Initialize the time filter radio buttons (week/month)
        RadioButton filterWeek = popupView.findViewById(R.id.filter_week);
        RadioButton filterMonth = popupView.findViewById(R.id.filter_month);
        if (currentDays == 7) {
            filterWeek.setChecked(true);
            filterMonth.setChecked(false);
        } else if (currentDays == 30) {
            filterWeek.setChecked(false);
            filterMonth.setChecked(true);
        } else {
            filterWeek.setChecked(false);
            filterMonth.setChecked(false);
        }

        // Initialize the spinner for emotional states
        Spinner moodSpinner = popupView.findViewById(R.id.spinner_emotional_state);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.emotional_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(adapter);
        // Set spinner selection based on saved mood filter
        int spinnerPosition = adapter.getPosition(currentMoodFilter);
        moodSpinner.setSelection(spinnerPosition);

        // Initialize the search box with saved search filter text
        EditText searchBox = popupView.findViewById(R.id.search_reason_box);
        searchBox.setText(currentSearchFilter);

        // Configure search icon color change on text input
        ImageView searchIcon = popupView.findViewById(R.id.search_button);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchIcon.setColorFilter(getColor(R.color.black));
                if (s.toString().isEmpty()) {
                    searchIcon.clearColorFilter();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No additional actions needed after text changes
            }
        });

        // Initialize apply and reset buttons
        Button applyButton = popupView.findViewById(R.id.apply_button);
        Button resetButton = popupView.findViewById(R.id.reset_button);

        // Update filter state and apply filters when the apply button is clicked
        applyButton.setOnClickListener(v -> {
            boolean showOnlyFollowedMoods = showFollowedMoods.isChecked();
            int days = filterWeek.isChecked() ? 7 : filterMonth.isChecked() ? 30 : 0;
            String selectedMood = moodSpinner.getSelectedItem().toString();
            String searchQuery = searchBox.getText().toString().trim();

            // Update current filter state variables
            currentShowOnlyFollowedMoods = showOnlyFollowedMoods;
            currentDays = days;
            currentMoodFilter = selectedMood;
            currentSearchFilter = searchQuery;

            applyFilters(showOnlyFollowedMoods, days, selectedMood, searchQuery);
            // Show Snackbar message to indicate filter has been applied
            showSnackbar("Filter applied", false);
            filterDialog.dismiss();
        });

        // Reset filters to default when the reset button is clicked
        resetButton.setOnClickListener(v -> {
            currentShowOnlyFollowedMoods = false;
            currentDays = 0;
            currentMoodFilter = "Select Mood";
            currentSearchFilter = "";
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                loadFollowedUsersMoods(user.getUid());
            }
            // Show Snackbar message to indicate filters have been reset
            showSnackbar("Filters reset", false);
            filterDialog.dismiss();
        });

        // Close the popup when the close icon is clicked
        popupView.findViewById(R.id.close_popup).setOnClickListener(v -> filterDialog.dismiss());
    }

    /**
     * Applies the selected filters by either loading followed moods or moods from all followed users.
     *
     * @param showOnlyFollowedMoods Whether to show only explicitly followed moods.
     * @param days                  The number of days for time filtering.
     * @param moodFilter            The selected mood filter.
     * @param searchFilter          The search query filter.
     */
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

    /**
     * Calculates the cutoff timestamp for filtering moods based on the given number of days.
     *
     * @param days The number of days in the past.
     * @return The cutoff timestamp in milliseconds.
     */
    private long calculateCutoffTimestamp(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar.getTimeInMillis();
    }

    /**
     * Converts a timestamp string to milliseconds.
     *
     * @param timestampStr The timestamp string in the format "hh:mm a - MMMM dd, yyyy".
     * @return The timestamp in milliseconds.
     */
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
