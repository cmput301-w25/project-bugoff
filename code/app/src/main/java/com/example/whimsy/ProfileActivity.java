/**
 * ProfileActivity serves as the main activity for displaying and managing a user's profile,
 * including their personal information, mood entries, and social connections.
 *
 * This class initializes the profile UI, handles interactions with Firebase services for
 * authentication, storage, and database operations, and provides filtering and editing capabilities.
 *
 * Outstanding Issues:
 * - Offline editing is disabled but lacks robust offline data caching.
 * - Mood list sorting may not handle edge cases with invalid timestamps gracefully.
 *
 */

package com.example.whimsy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Activity class for displaying and managing the user's profile.
 * Extends ActivityBase to inherit common functionality.
 */
public class ProfileActivity extends ActivityBase {

    private ImageView profileImage;         // Displays user's profile picture
    private TextView profileName;           // Displays user's name
    private TextView profileUsername;          // Displays user's email
    private TextView profileBio;            // Displays user's bio
    private Button editProfileButton;       // Button to trigger profile editing
    private FirebaseAuth mAuth;             // Firebase Authentication instance
    private RecyclerView recyclerView;      // Displays list of user's moods
    private MoodAdapter moodAdapter;        // Adapter for mood RecyclerView
    private List<Mood> moodList;            // List of mood entries
    private List<String> moodDocIds;        // List of mood document IDs from Firestore
    private ActivityResultLauncher<Intent> selectImageLauncher; // Launcher for image selection
    private AlertDialog editProfileDialog;  // Dialog for editing profile
    private int selectedDaysFilter = 0;     // Filter for days (0 = no filter, 7 = week, 30 = month)
    private String selectedMoodFilter = "Select Mood"; // Selected mood filter
    private String searchQuery = "";        // Search query for mood filtering
    private TextView followersCount;        // Displays number of followers
    private TextView followingCount;        // Displays number of following
    public TextView moodCountText;          // Displays total mood count

    private Uri pendingProfileImageUri = null; // URI for pending profile image


    /**
     * Called when the activity is first created.
     * Initializes UI components, Firebase, and mood list.
     *
     * @param savedInstanceState If the activity is being re-initialized, this contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeNavigation(); // Setup navigation from base class

        // Register launcher for selecting profile image
        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        // If the edit profile dialog is showing, update its image view and store the URI.
                        if (editProfileDialog != null && editProfileDialog.isShowing()) {
                            ImageView profilePic = editProfileDialog.findViewById(R.id.edit_profile_image);
                            profilePic.setImageURI(imageUri);
                            pendingProfileImageUri = imageUri;
                        } else {
                            // Otherwise, update the main profile image immediately (if needed)
                            profileImage.setImageURI(imageUri);
                        }
                    }
                }
        );


        // Inflate profile page layout into content frame
        getLayoutInflater().inflate(R.layout.profile_page, findViewById(R.id.content_frame), true);

        // Initialize UI components
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileBio = findViewById(R.id.profile_bio);
        profileUsername = findViewById(R.id.profile_email);
        editProfileButton = findViewById(R.id.edit_profile_btn);
        followersCount = findViewById(R.id.followers_count);
        followingCount = findViewById(R.id.following_count);
        moodCountText = findViewById(R.id.moods_count);

        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.moods_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodList = new ArrayList<>();
        moodDocIds = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);

        // Setup filter button
        ImageButton filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(v -> showFilterPopup());

        // Load user data
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            profileName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User Name");
            profileImage.setImageResource(R.drawable.ic_profile);

            // Real-time listener for profile updates
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (error != null) {
                            Log.e("Firestore", "Error fetching profile updates", error);
                            return;
                        }
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            updateProfileUI(documentSnapshot);
                        }
                    });
            loadMoods(); // Load user's mood entries
        }

        // Setup click listeners for followers/following counts
        setupFollowCounts(user);

        editProfileButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                Toast.makeText(this, "Cannot edit profile while offline", Toast.LENGTH_SHORT).show();
                return;
            }
            showEditProfileDialog();
        });

        // Add touch listener to RecyclerView for mood item selection
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && e.getAction() == MotionEvent.ACTION_UP) {
                    int position = rv.getChildAdapterPosition(child);
                    if (position != RecyclerView.NO_POSITION) {
                        navigateToMoodPage(position);
                    }
                }
                return false;
            }
        });
    }

    /**
     * Updates profile UI elements based on Firestore document snapshot.
     *
     * @param documentSnapshot The Firestore document containing user data
     */
    private void updateProfileUI(DocumentSnapshot documentSnapshot) {
        String profilePicUrl = documentSnapshot.getString("profilePictureUrl");
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            Glide.with(this).load(profilePicUrl).into(profileImage);
        }
        String bio = documentSnapshot.getString("bio");
        if (bio == null || bio.isEmpty()) {
            bio = "Add a bio...";
        }
        profileBio.setText(bio);
        String name = documentSnapshot.getString("name");
        String username = documentSnapshot.getString("username");
        if (name != null && !name.isEmpty()) {
            profileName.setText(name);
        }
        if (username != null && !username.isEmpty()) {
            profileUsername.setText("@" + username);
        }
    }

    /**
     * Sets up click listeners and fetches followers/following counts.
     *
     * @param user The current Firebase user
     */
    private void setupFollowCounts(FirebaseUser user) {
        followersCount.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowingActivity.class);
            intent.putExtra("type", "followers");
            intent.putExtra("userId", user.getUid());
            startActivity(intent);
        });

        followingCount.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowingActivity.class);
            intent.putExtra("type", "following");
            intent.putExtra("userId", user.getUid());
            startActivity(intent);
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        db.collection("users").document(userId).collection("followers").get()
                .addOnSuccessListener(queryDocumentSnapshots -> followersCount.setText(String.valueOf(queryDocumentSnapshots.size())))
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error fetching followers count", e);
                    followersCount.setText("0");
                });

        db.collection("users").document(userId).collection("following").get()
                .addOnSuccessListener(queryDocumentSnapshots -> followingCount.setText(String.valueOf(queryDocumentSnapshots.size())))
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error fetching following count", e);
                    followingCount.setText("0");
                });
    }

    /**
     * Navigates to the MoodPageActivity for a selected mood.
     *
     * @param position The position of the selected mood in the list
     */
    private void navigateToMoodPage(int position) {
        Intent intent = new Intent(this, MoodPageActivity.class);
        intent.putExtra("SELECTED_MOOD", moodList.get(position));
        intent.putExtra("MOOD_ID", moodDocIds.get(position));
        startActivity(intent);
    }

    /**
     * Displays a popup dialog for filtering mood entries.
     */
    private void showFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.filter_popup, null);
        builder.setView(popupView);

        AlertDialog filterDialog = builder.create();
        filterDialog.show();

        configureFilterDialog(filterDialog, popupView);
    }

    /**
     * Configures the filter dialog UI and functionality.
     *
     * @param filterDialog The AlertDialog instance
     * @param popupView The inflated view for the dialog
     */
    private void configureFilterDialog(AlertDialog filterDialog, View popupView) {
        if (filterDialog.getWindow() != null) {
            filterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            filterDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            filterDialog.getWindow().setGravity(Gravity.CENTER);
        }

        ImageView closePopup = popupView.findViewById(R.id.close_popup);
        closePopup.setOnClickListener(v -> filterDialog.dismiss());

        RadioButton filterWeek = popupView.findViewById(R.id.filter_week);
        RadioButton filterMonth = popupView.findViewById(R.id.filter_month);

        if (selectedDaysFilter == 7) filterWeek.setChecked(true);
        else if (selectedDaysFilter == 30) filterMonth.setChecked(true);

        Spinner moodSpinner = popupView.findViewById(R.id.spinner_emotional_state);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.emotional_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(adapter);

        int moodPosition = adapter.getPosition(selectedMoodFilter);
        moodSpinner.setSelection(moodPosition);

        EditText searchBox = popupView.findViewById(R.id.search_reason_box);
        searchBox.setText(searchQuery);

        Button applyButton = popupView.findViewById(R.id.apply_button);
        Button resetButton = popupView.findViewById(R.id.reset_button);

        resetButton.setOnClickListener(v -> resetFilters(filterDialog));
        applyButton.setOnClickListener(v -> applyFilters(filterDialog, moodSpinner, filterWeek, filterMonth, searchBox));
    }

    /**
     * Resets all filters and reloads moods.
     *
     * @param filterDialog The filter dialog to dismiss
     */
    private void resetFilters(AlertDialog filterDialog) {
        selectedDaysFilter = 0;
        selectedMoodFilter = "Select Mood";
        searchQuery = "";
        loadMoods();
        filterDialog.dismiss();
        Toast.makeText(this, "Filters reset", Toast.LENGTH_SHORT).show();
    }

    /**
     * Applies selected filters and reloads moods.
     *
     * @param filterDialog The filter dialog to dismiss
     * @param moodSpinner The spinner for mood selection
     * @param filterWeek Radio button for week filter
     * @param filterMonth Radio button for month filter
     * @param searchBox EditText for search query
     */
    private void applyFilters(AlertDialog filterDialog, Spinner moodSpinner, RadioButton filterWeek, RadioButton filterMonth, EditText searchBox) {
        String selectedMood = moodSpinner.getSelectedItem().toString();
        boolean applyMoodFilter = !selectedMood.equals("Select Mood");

        selectedDaysFilter = filterWeek.isChecked() ? 7 : filterMonth.isChecked() ? 30 : 0;
        selectedMoodFilter = selectedMood;
        searchQuery = searchBox.getText().toString().trim();

        loadMoodsFiltered(selectedDaysFilter, applyMoodFilter ? selectedMood : null, searchQuery.isEmpty() ? null : searchQuery);
        filterDialog.dismiss();
    }

    /**
     * Loads all mood entries for the current user from Firestore.
     */
    public void loadMoods() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(userDoc -> {
                        String profileImageUrl = userDoc.getString("profilePictureUrl");
                        moodList.clear();
                        moodDocIds.clear();

                        db.collection("users").document(userId).collection("moods")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> processMoodDocuments(queryDocumentSnapshots, user, profileImageUrl))
                                .addOnFailureListener(e -> handleMoodLoadFailure(e));
                    })
                    .addOnFailureListener(e -> handleProfileLoadFailure(e));
        }
    }

    /**
     * Processes mood documents from Firestore query result.
     *
     * @param queryDocumentSnapshots The query result containing mood documents
     * @param user The current Firebase user
     * @param profileImageUrl The user's profile image URL
     */
    private void processMoodDocuments(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots, FirebaseUser user, String profileImageUrl) {
        if (!queryDocumentSnapshots.isEmpty()) {
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Mood moodObj = createMoodObject(document, user, profileImageUrl);
                moodList.add(moodObj);
                moodDocIds.add(document.getId());
            }
            sortAndUpdateMoods();
        } else {
            updateMoodCount();
        }
    }

    /**
     * Creates a Mood object from a Firestore document.
     *
     * @param document The Firestore document
     * @param user The current Firebase user
     * @param profileImageUrl The user's profile image URL
     * @return A new Mood object
     */
    private Mood createMoodObject(DocumentSnapshot document, FirebaseUser user, String profileImageUrl) {
        String mood = document.getString("mood");
        String locationName = document.getString("locationName");
        String timestampStr = document.getString("timestamp");
        String trigger = document.getString("trigger");
        String reason = document.getString("reason");
        String imageUrl = document.getString("imageUrl");

        List<Map<String, Object>> tags = (List<Map<String, Object>>) document.get("tags");
        List<String> taggedUserNames = extractTaggedUserNames(tags);
        String gatheringStatus = calculateGatheringStatus(tags);

        return new Mood(
                user.getDisplayName(),
                user.getEmail(),
                locationName != null ? locationName : "No location",
                timestampStr,
                timestampStr,
                gatheringStatus,
                "Feeling " + mood,
                trigger,
                reason,
                imageUrl,
                profileImageUrl,
                taggedUserNames
        );
    }

    /**
     * Extracts tagged user names from tags list.
     *
     * @param tags List of tag maps from Firestore
     * @return List of tagged usernames
     */
    private List<String> extractTaggedUserNames(List<Map<String, Object>> tags) {
        List<String> taggedUserNames = new ArrayList<>();
        if (tags != null) {
            for (Map<String, Object> tag : tags) {
                String username = (String) tag.get("name");
                if (username != null) {
                    taggedUserNames.add(username);
                }
            }
        }
        return taggedUserNames;
    }

    /**
     * Calculates gathering status based on number of tagged users.
     *
     * @param tags List of tag maps from Firestore
     * @return A string representing the gathering status
     */
    private String calculateGatheringStatus(List<Map<String, Object>> tags) {
        if (tags == null || tags.isEmpty()) return "Alone";
        int tagCount = tags.size();
        if (tagCount == 1) return "With 1 other";
        if (tagCount <= 5) return "With " + tagCount + " others";
        return "With a crowd";
    }

    /**
     * Sorts mood list by timestamp and updates UI.
     */
    private void sortAndUpdateMoods() {
        Collections.sort(moodList, (m1, m2) ->
                Long.compare(convertTimestampToMillis(m2.getTimestamp()), convertTimestampToMillis(m1.getTimestamp())));
        moodAdapter.notifyDataSetChanged();
        updateMoodCount();
    }

    /**
     * Handles failure to load moods.
     *
     * @param e The exception encountered
     */
    private void handleMoodLoadFailure(Exception e) {
        Log.e("ProfileActivity", "Error loading moods", e);
        Toast.makeText(this, "Error loading moods", Toast.LENGTH_SHORT).show();
        updateMoodCount();
    }

    /**
     * Handles failure to load user profile.
     *
     * @param e The exception encountered
     */
    private void handleProfileLoadFailure(Exception e) {
        Log.e("ProfileActivity", "Error fetching user profile", e);
        Toast.makeText(this, "Error loading user profile", Toast.LENGTH_SHORT).show();
    }

    /**
     * Loads filtered mood entries based on specified criteria.
     *
     * @param days Number of days to filter (0 for no filter)
     * @param moodFilter Mood to filter by (null for no filter)
     * @param searchFilter Search query to filter reasons (null for no filter)
     */
    private void loadMoodsFiltered(int days, @Nullable String moodFilter, @Nullable String searchFilter) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(userDoc -> {
                        String profileImageUrl = userDoc.getString("profilePictureUrl");
                        moodList.clear();
                        moodDocIds.clear();

                        long cutoffTimestamp = days > 0 ? calculateCutoffTimestamp(days) : 0;

                        db.collection("users").document(userId).collection("moods")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> processFilteredMoods(queryDocumentSnapshots, user, profileImageUrl, days, moodFilter, searchFilter, cutoffTimestamp))
                                .addOnFailureListener(e -> handleMoodLoadFailure(e));
                    })
                    .addOnFailureListener(e -> handleProfileLoadFailure(e));
        }
    }

    /**
     * Calculates the timestamp cutoff for filtering by days.
     *
     * @param days Number of days to look back
     * @return The cutoff timestamp in milliseconds
     */
    private long calculateCutoffTimestamp(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -days);
        return calendar.getTimeInMillis();
    }

    /**
     * Processes filtered mood documents from Firestore.
     *
     * @param queryDocumentSnapshots The query result
     * @param user The current Firebase user
     * @param profileImageUrl User's profile image URL
     * @param days Days filter
     * @param moodFilter Mood filter
     * @param searchFilter Search filter
     * @param cutoffTimestamp Cutoff timestamp for date filtering
     */
    private void processFilteredMoods(com.google.firebase.firestore.QuerySnapshot queryDocumentSnapshots, FirebaseUser user, String profileImageUrl, int days, String moodFilter, String searchFilter, long cutoffTimestamp) {
        if (!queryDocumentSnapshots.isEmpty()) {
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                if (shouldIncludeMood(document, days, moodFilter, searchFilter, cutoffTimestamp)) {
                    Mood moodObj = createMoodObject(document, user, profileImageUrl);
                    moodList.add(moodObj);
                    moodDocIds.add(document.getId());
                }
            }
            sortAndUpdateMoods();
        } else {
            Toast.makeText(this, "No moods found for the selected filter", Toast.LENGTH_SHORT).show();
            updateMoodCount();
        }
    }

    /**
     * Determines if a mood should be included based on filters.
     *
     * @param document The Firestore document
     * @param days Days filter
     * @param moodFilter Mood filter
     * @param searchFilter Search filter
     * @param cutoffTimestamp Cutoff timestamp
     * @return True if the mood should be included
     */
    private boolean shouldIncludeMood(DocumentSnapshot document, int days, String moodFilter, String searchFilter, long cutoffTimestamp) {
        String mood = document.getString("mood");
        String reason = document.getString("reason");
        String timestampStr = document.getString("timestamp");

        long moodTimestamp = convertTimestampToMillis(timestampStr);
        boolean withinTimeRange = (days == 0) || (moodTimestamp >= cutoffTimestamp);
        boolean matchesMood = (moodFilter == null) || (mood.equalsIgnoreCase(moodFilter));
        boolean matchesSearch = (searchFilter == null) || (reason != null && reason.toLowerCase().contains(searchFilter.toLowerCase()));

        return withinTimeRange && matchesMood && matchesSearch;
    }

    /**
     * Converts a timestamp string to milliseconds.
     *
     * @param timestampStr The timestamp string to convert
     * @return The timestamp in milliseconds
     */
    private long convertTimestampToMillis(String timestampStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a - MMMM dd, yyyy", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(timestampStr);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e("ProfileActivity", "Error parsing timestamp: " + timestampStr, e);
            return 0;
        }
    }

    /**
     * Checks if network is available.
     *
     * @return True if no network is available (offline)
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Displays the edit profile dialog.
     */
    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);
        editProfileDialog = builder.create();

        setupEditProfileDialog(view);
        editProfileDialog.show();
    }

    /**
     * Sets up the edit profile dialog UI and listeners.
     *
     * @param view The inflated dialog view
     */
    private void setupEditProfileDialog(View view) {
        ImageView profilePic = view.findViewById(R.id.edit_profile_image);
        EditText editName = view.findViewById(R.id.edit_name);
        EditText editBio = view.findViewById(R.id.edit_bio);
        Spinner genderSpinner = view.findViewById(R.id.gender_spinner);
        Button btnResetPassword = view.findViewById(R.id.reset_password_btn);
        Button btnSave = view.findViewById(R.id.save_changes_btn);
        Button btnCancel = view.findViewById(R.id.cancel_btn);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadExistingProfileData(user, editName, editBio, genderSpinner, profilePic);
            profilePic.setOnClickListener(v -> selectImageFromGallery());

            btnResetPassword.setOnClickListener(v -> {
                FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });

            btnSave.setOnClickListener(v -> {
                String newName = editName.getText().toString().trim();
                String newBio = editBio.getText().toString().trim();
                String selectedGender = genderSpinner.getSelectedItem().toString();

                // Only update the profile image if a new image has been selected.
                if (pendingProfileImageUri != null) {
                    uploadImageToFirebase(pendingProfileImageUri);
                    pendingProfileImageUri = null; // Clear the pending URI after uploading.
                }

                saveUserProfile(newName, newBio, selectedGender);
                editProfileDialog.dismiss();
            });

            btnCancel.setOnClickListener(v -> editProfileDialog.dismiss());
        }
    }

    /**
     * Loads existing profile data into the edit dialog.
     *
     * @param user The current Firebase user
     * @param editName EditText for name
     * @param editBio EditText for bio
     * @param genderSpinner Spinner for gender selection
     * @param profilePic ImageView for profile picture
     */
    private void loadExistingProfileData(FirebaseUser user, EditText editName, EditText editBio, Spinner genderSpinner, ImageView profilePic) {
        editName.setText(user.getDisplayName());
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String bio = documentSnapshot.getString("bio");
                        if (bio == null || bio.isEmpty()) {
                            bio = "Add Bio...";
                        }
                        editBio.setText(bio);
                    }
                    String gender = documentSnapshot.getString("gender");
                    if (gender != null) {
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                                this, R.array.gender_options, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        genderSpinner.setAdapter(adapter);
                        int genderPosition = adapter.getPosition(gender);
                        genderSpinner.setSelection(genderPosition);
                    }
                    String profilePicUrl = documentSnapshot.getString("profilePictureUrl");
                    if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                        Glide.with(this).load(profilePicUrl).into(profilePic);
                    }
                });
        Glide.with(this).load(user.getPhotoUrl()).into(profilePic);
    }

    /**
     * Launches intent to select an image from the gallery.
     */
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
    }

    /**
     * Handles activity result for image selection (legacy method).
     *
     * @param requestCode The request code
     * @param resultCode The result code
     * @param data The returned data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    /**
     * Uploads the selected image to Firebase Storage using the ImageCompressor for compression,
     * and updates the user's profile picture in Firebase Authentication and Firestore.
     * <p>
     * Before uploading the new image, this method checks Firestore for an existing profile picture URL.
     * If one exists and is hosted on Firebase Storage, it attempts to delete that file.
     * </p>
     *
     * @param imageUri The URI of the selected new profile image.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // First, retrieve the current profile picture URL from Firestore.
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String previousUrl = documentSnapshot.getString("profilePictureUrl");
                        // If a previous image exists and its URL indicates it's hosted in Firebase Storage, delete it.
                        if (previousUrl != null && !previousUrl.isEmpty() &&
                                previousUrl.startsWith("https://firebasestorage.googleapis.com/")) {
                            StorageReference previousRef = FirebaseStorage.getInstance().getReferenceFromUrl(previousUrl);
                            previousRef.delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Storage", "Old profile image deleted successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Storage", "Error deleting old profile image", e);
                                    });
                        }
                        // Proceed with the new image upload.
                        try {
                            // Decode the image from the URI into a Bitmap.
                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            if (bitmap == null) {
                                Toast.makeText(this, "Unable to decode image", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Compress the bitmap to a byte array under 64KB using ImageCompressor.
                            byte[] compressedImageBytes = ImageCompressor.compressImage(bitmap, 65536);

                            // Generate a new, unique filename using a timestamp.
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                            StorageReference imageRef = FirebaseStorage.getInstance()
                                    .getReference("profile_pictures/" + user.getUid() + "/" + timeStamp + ".jpg");

                            // Upload the compressed byte array.
                            imageRef.putBytes(compressedImageBytes)
                                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                        // Update the profile image view with the new URL.
                                        Glide.with(this).load(uri).into(profileImage);
                                        // Update the Firebase Authentication profile.
                                        user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build());
                                        // Update Firestore with the new profile picture URL.
                                        FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(user.getUid())
                                                .update("profilePictureUrl", uri.toString())
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Firestore", "Profile picture updated successfully");
                                                    if (editProfileDialog != null && editProfileDialog.isShowing()) {
                                                        ImageView profilePic = editProfileDialog.findViewById(R.id.edit_profile_image);
                                                        Glide.with(this).load(uri).into(profilePic);
                                                    }
                                                })
                                                .addOnFailureListener(e -> Log.e("Firestore", "Error updating profile picture", e));
                                    }))
                                    .addOnFailureListener(e -> Log.e("Storage", "Error uploading new profile image", e));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error fetching user data", e);
                        Toast.makeText(this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    /**
     * Saves updated user profile information to Firebase.
     *
     * @param name The new display name
     * @param bio The new bio
     * @param gender The selected gender
     */
    private void saveUserProfile(String name, String bio, String gender) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnSuccessListener(aVoid -> Log.d("Profile", "Firebase Auth profile updated"))
                    .addOnFailureListener(e -> Log.e("Profile", "Failed to update Firebase Auth profile", e));
            String userId = user.getUid();
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("name", name);
            userProfile.put("bio", bio);
            userProfile.put("gender", gender);

            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .set(userProfile, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String updatedName = documentSnapshot.getString("name");
                                        String updatedBio = documentSnapshot.getString("bio");
                                        profileName.setText(updatedName);
                                        profileBio.setText(updatedBio);
                                        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Updates the mood count display.
     */
    private void updateMoodCount() {
        moodCountText.setText(String.valueOf(moodList.size()));
        TextView emptyMoodText = findViewById(R.id.emptyMoodText);
        if (moodList.isEmpty()) {
            emptyMoodText.setVisibility(View.VISIBLE);
        } else {
            emptyMoodText.setVisibility(View.GONE);
        }
    }
}