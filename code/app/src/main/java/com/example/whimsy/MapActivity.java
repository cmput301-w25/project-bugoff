package com.example.whimsy;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * MapActivity displays moods on a Google Map by fetching data from Firebase Firestore.
 * It supports viewing the current user's moods, moods of followed users, and applying filters
 * based on time, mood type, and a text search on mood reasons.
 */
public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String currentUserId;
    private List<String> followedUserIds = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private ImageButton btnMyMoods, btnFollowingMoods, btnAllMoods, btnBack, btnHeart, filterIcon;
    private List<Mood> currentDisplayedMoods = new ArrayList<>(); // Track moods for filtering
    private String currentMoodSource = ""; // "my_moods", "followed_moods", etc.

    // Instance variables to store filter state
    private int filterDays = 0; // 0: no time filter, 7: week, 30: month
    private String filterMood = "Select Mood"; // Default spinner value for mood type
    private String filterSearchQuery = ""; // Text search filter for mood reason

    /**
     * Called when the activity is starting. Initializes Firebase, location services, UI elements,
     * and sets up the map and button listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize Firebase and location services
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize UI elements
        btnMyMoods = findViewById(R.id.btn_show_my_moods);
        btnFollowingMoods = findViewById(R.id.btn_show_following_moods);
        btnAllMoods = findViewById(R.id.btn_show_both_moods);
        btnBack = findViewById(R.id.btn_back);
        btnHeart = findViewById(R.id.btn_heart);
        filterIcon = findViewById(R.id.filter_icon);

        // Set up button listeners
        btnMyMoods.setOnClickListener(v -> {
            mMap.clear();
            fetchCurrentUserMoods();
            filterIcon.setVisibility(View.VISIBLE);
            currentMoodSource = "my_moods";
        });

        btnFollowingMoods.setOnClickListener(v -> {
            mMap.clear();
            fetchLatestMoodsOfFollowedUsers();
            filterIcon.setVisibility(View.GONE);
            currentMoodSource = "following_moods";
        });

        btnAllMoods.setOnClickListener(v -> {
            mMap.clear();
            fetchCurrentUserMoods();
            fetchLatestMoodsOfFollowedUsers();
            filterIcon.setVisibility(View.GONE);
            currentMoodSource = "";
        });

        btnBack.setOnClickListener(v -> finish());

        btnHeart.setOnClickListener(v -> {
            mMap.clear();
            fetchFollowedMoods();
            filterIcon.setVisibility(View.VISIBLE);
            currentMoodSource = "followed_moods";
        });

        filterIcon.setOnClickListener(v -> showFilterDialog());

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MapActivity", "Error: Map fragment is null");
        }

        checkLocationPermission();
    }

    /**
     * Checks for the ACCESS_FINE_LOCATION permission and requests it if not granted.
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    /**
     * Called when the map is ready to be used.
     *
     * @param googleMap The GoogleMap object ready for use.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 12));
                        } else {
                            LatLng northAmerica = new LatLng(37.0902, -95.7129);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(northAmerica, 5));
                        }
                    });
        }

        if (currentUserId != null && !currentUserId.isEmpty()) {
            fetchFollowedUsers();
            fetchCurrentUserMoods();
        } else {
            Log.e("MapActivity", "Error: currentUserId is not initialized");
        }
    }

    /**
     * Handles the result of permission requests.
     *
     * @param requestCode  The request code passed in requestPermissions.
     * @param permissions  The requested permissions.
     * @param grantResults The results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            } else {
                Toast.makeText(this, "Permission denied! Showing default location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Fetches the list of users that the current user is following.
     * After fetching, it may trigger fetching followed users' moods if applicable.
     */
    private void fetchFollowedUsers() {
        db.collection("users").document(currentUserId).collection("following")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    followedUserIds.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        followedUserIds.add(doc.getId());
                    }
                    if (currentMoodSource.equals("following_moods") || currentMoodSource.equals("")) {
                        fetchLatestMoodsOfFollowedUsers();
                    }
                })
                .addOnFailureListener(e -> Log.e("MapActivity", "Error fetching followed users", e));
    }

    /**
     * Fetches the latest mood of each followed user.
     */
    private void fetchLatestMoodsOfFollowedUsers() {
        for (String userId : followedUserIds) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(userDoc -> {
                        String username = userDoc.getString("name");
                        String profilePictureUrl = userDoc.getString("profilePictureUrl");

                        db.collection("users").document(userId).collection("moods")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        showMoodsOnMap(querySnapshot, username, profilePictureUrl);
                                    }
                                });
                    });
        }
    }

    /**
     * Fetches the current user's moods from Firestore and displays them on the map.
     */
    private void fetchCurrentUserMoods() {
        db.collection("users").document(currentUserId).collection("moods")
                .whereNotEqualTo("locationName", null)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    currentDisplayedMoods.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Mood mood = doc.toObject(Mood.class);
                        mood.setUserName("You");
                        currentDisplayedMoods.add(mood);
                    }
                    showMoodsOnMap(querySnapshot, "You", null);
                })
                .addOnFailureListener(e -> Log.e("MapActivity", "Error fetching current user moods", e));
    }

    /**
     * Fetches explicitly followed moods from the "followedMoods" collection and displays them.
     */
    private void fetchFollowedMoods() {
        db.collection("users").document(currentUserId).collection("followedMoods")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    currentDisplayedMoods.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String ownerUid = doc.getString("ownerUid");
                        String moodId = doc.getString("moodId");

                        // Fetch the mood document from users/{ownerUid}/moods/{moodId}
                        db.collection("users").document(ownerUid).collection("moods").document(moodId)
                                .get()
                                .addOnSuccessListener(moodDoc -> {
                                    if (moodDoc.exists()) {
                                        // Convert the Firestore document to a Mood object
                                        Mood mood = moodDoc.toObject(Mood.class);
                                        mood.setMoodStatus(moodDoc.getString("mood"));

                                        // Fetch additional user data (name and profile picture)
                                        db.collection("users").document(ownerUid).get()
                                                .addOnSuccessListener(userDoc -> {
                                                    mood.setUserName(userDoc.getString("name"));
                                                    mood.setProfileImageUrl(userDoc.getString("profilePictureUrl"));

                                                    // Add the mood to the list and display it on the map
                                                    currentDisplayedMoods.add(mood);
                                                    showMoodOnMap(mood);
                                                });
                                    } else {
                                        Log.w("MapActivity", "Mood document does not exist: " + moodId);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("MapActivity", "Error fetching mood: " + moodId, e));
                    }
                })
                .addOnFailureListener(e -> Log.e("MapActivity", "Error fetching followed moods", e));
    }

    /**
     * Displays a collection of moods on the map using markers.
     *
     * @param querySnapshot   The Firestore QuerySnapshot containing mood documents.
     * @param username        The username associated with the moods.
     * @param profilePictureUrl The URL of the profile picture to be used as a marker icon.
     */
    private void showMoodsOnMap(QuerySnapshot querySnapshot, String username, String profilePictureUrl) {
        int index = 0;
        for (QueryDocumentSnapshot doc : querySnapshot) {
            double lat = doc.getDouble("locationLat");
            double lng = doc.getDouble("locationLng");
            String mood = doc.getString("mood");

            // Select emoji based on the mood
            String emoji = getEmojiForMood(mood);

            // Add a slight offset to avoid marker overlap
            double offset = 0.0002 * (index % 5);
            LatLng location = new LatLng(lat + offset, lng + offset);

            // Combine username, mood, and emoji into the title
            String markerTitle = username + " - " + mood + " " + emoji;

            // Add marker with title and emoji
            if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                loadProfilePictureMarker(profilePictureUrl, location, markerTitle, mood);
            } else {
                mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(markerTitle)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
            index++;
        }
    }

    /**
     * Displays a single mood on the map.
     *
     * @param mood The Mood object containing the details to display.
     */
    private void showMoodOnMap(Mood mood) {
        double lat = mood.getLocationLat();
        double lng = mood.getLocationLng();

        // Check for invalid coordinates to avoid adding invalid markers
        if (Double.isNaN(lat) || Double.isNaN(lng)) {
            Log.e("MapActivity", "Invalid location for mood: " + mood.getMoodStatus());
            return;
        }

        String moodStr = mood.getMoodStatus();
        String username = mood.getUserName();
        String profilePictureUrl = mood.getProfileImageUrl();
        String emoji = getEmojiForMood(moodStr);
        LatLng location = new LatLng(lat, lng);
        String markerTitle = username + " - " + moodStr + " " + emoji;

        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
            loadProfilePictureMarker(profilePictureUrl, location, markerTitle, moodStr);
        } else {
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        }
    }

    /**
     * Loads a profile picture from a URL as a marker icon using Glide.
     *
     * @param imageUrl    The URL of the profile picture.
     * @param location    The LatLng location for the marker.
     * @param markerTitle The title of the marker.
     * @param mood        The mood status (used in determining emoji).
     */
    private void loadProfilePictureMarker(String imageUrl, LatLng location, String markerTitle, String mood) {
        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .circleCrop()
                .into(new CustomTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(markerTitle)
                                .icon(BitmapDescriptorFactory.fromBitmap(resource)));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle placeholder if necessary
                    }
                });
    }

    /**
     * Returns an emoji representation based on the mood string.
     *
     * @param mood The mood status.
     * @return A string containing the corresponding emoji.
     */
    private String getEmojiForMood(String mood) {
        if (mood == null) return "\uD83D\uDE10"; // Default neutral emoji
        switch (mood.toLowerCase()) {
            case "happy": return "\uD83D\uDE00"; // 😀
            case "sad": return "\uD83D\uDE1E"; // 😞
            case "angry": return "\uD83D\uDE20"; // 😠
            case "scared": return "\uD83D\uDE28"; // 😨
            case "confused": return "\uD83D\uDE15"; // 😕
            case "disgusted": return "\uD83E\uDD22"; // 🤢
            case "excited": return "\uD83D\uDE04"; // 😄
            case "ashamed": return "\uD83D\uDE33"; // 😳
            default: return "\uD83D\uDE10"; // 😐
        }
    }

    /**
     * Displays a filter dialog for time range, mood type, and mood reason search.
     * The dialog pre-populates with previously applied filters.
     */
    private void showFilterDialog() {
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

        RadioButton filterWeek = popupView.findViewById(R.id.filter_week);
        RadioButton filterMonth = popupView.findViewById(R.id.filter_month);
        Spinner moodSpinner = popupView.findViewById(R.id.spinner_emotional_state);
        EditText searchBox = popupView.findViewById(R.id.search_reason_box);
        Button applyButton = popupView.findViewById(R.id.apply_button);
        Button resetButton = popupView.findViewById(R.id.reset_button);
        popupView.findViewById(R.id.mood_source_filter).setVisibility(View.GONE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.emotional_states, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(adapter);

        // Pre-populate controls with stored filter state
        if (filterDays == 7) {
            filterWeek.setChecked(true);
        } else if (filterDays == 30) {
            filterMonth.setChecked(true);
        }
        int spinnerPosition = adapter.getPosition(filterMood);
        moodSpinner.setSelection(spinnerPosition);
        searchBox.setText(filterSearchQuery);

        applyButton.setOnClickListener(v -> {
            // Update the instance variables with the user's selections
            filterDays = filterWeek.isChecked() ? 7 : filterMonth.isChecked() ? 30 : 0;
            filterMood = moodSpinner.getSelectedItem().toString();
            filterSearchQuery = searchBox.getText().toString().trim();
            applyFilters(filterDays, filterMood, filterSearchQuery);
            filterDialog.dismiss();
        });

        resetButton.setOnClickListener(v -> {
            mMap.clear();
            if (currentMoodSource.equals("my_moods")) {
                fetchCurrentUserMoods();
            } else if (currentMoodSource.equals("followed_moods")) {
                fetchFollowedMoods();
            }
            // Reset filter state to defaults
            filterDays = 0;
            filterMood = "Select Mood";
            filterSearchQuery = "";
            filterDialog.dismiss();
        });

        popupView.findViewById(R.id.close_popup).setOnClickListener(v -> filterDialog.dismiss());
    }

    /**
     * Applies the selected filters to the current list of displayed moods and updates the map markers.
     *
     * @param days        The time filter in days (e.g., 7 for week, 30 for month).
     * @param moodFilter  The mood type filter.
     * @param searchFilter The text search filter for mood reasons.
     */
    private void applyFilters(int days, String moodFilter, String searchFilter) {
        long cutoffTimestamp = days > 0 ? calculateCutoffTimestamp(days) : 0;
        List<Mood> filteredMoods = new ArrayList<>();

        for (Mood mood : currentDisplayedMoods) {
            long moodTimestamp = convertTimestampToMillis(mood.getTimestamp());
            boolean withinTimeRange = days == 0 || moodTimestamp >= cutoffTimestamp;

            String moodStatus = mood.getMoodStatus();
            boolean matchesMood = moodFilter.equals("Select Mood") ||
                    (moodStatus != null && moodStatus.equalsIgnoreCase(moodFilter));

            boolean matchesSearch = searchFilter.isEmpty() ||
                    (mood.getMoodReason() != null &&
                            mood.getMoodReason().toLowerCase().contains(searchFilter.toLowerCase()));

            if (withinTimeRange && matchesMood && matchesSearch) {
                filteredMoods.add(mood);
            }
        }

        mMap.clear();
        for (Mood mood : filteredMoods) {
            showMoodOnMap(mood);
        }
        currentDisplayedMoods = filteredMoods;
    }

    /**
     * Calculates the cutoff timestamp for filtering moods by time.
     *
     * @param days The number of days to look back.
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
     * @param timestampStr The timestamp string to convert.
     * @return The timestamp in milliseconds, or 0 if parsing fails.
     */
    private long convertTimestampToMillis(String timestampStr) {
        if (timestampStr == null) return 0;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a - MMMM dd, yyyy", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(timestampStr);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            Log.e("MapActivity", "Error parsing timestamp: " + timestampStr, e);
            return 0;
        }
    }
}
