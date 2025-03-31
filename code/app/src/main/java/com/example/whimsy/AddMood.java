/**
 * The {@code AddMood} class is an activity that enables users to log their current mood along with
 * optional details such as reasons, location, tags, and an image. Mood data is stored in Firebase Firestore,
 * while images are uploaded to Firebase Storage. This activity integrates with the Google Places API for location
 * selection and supports tagging other users in the app.
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li>Mood selection via a spinner control.</li>
 *     <li>Optional reason input with character limits (200 characters).</li>
 *     <li>Location selection using the current location, or by searching via the Google Places Autocomplete.</li>
 *     <li>Image upload from either the camera or the gallery.</li>
 *     <li>User tagging functionality for mood entries.</li>
 *     <li>Input validation and error handling with user feedback.</li>
 *     <li>Integration with Firebase for persistent data storage and retrieval.</li>
 *     <li>Offline mood queuing when no internet is available.</li>
 *     <li>A MaterialBanner-style notification showing the number of queued moods.</li>
 *     <li>Automatic sync when connectivity is restored with a "Back online" message.</li>
 * </ul>
 *
 * <p><b>Outstanding Issues:</b></p>
 * <ul>
 *     <li>The image size validation logic may require optimization to efficiently handle large files.</li>
 *     <li>The location permission handling could be refined to enhance user experience.</li>
 * </ul>
 */
package com.example.whimsy;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ComponentCaller;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.AddressComponent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AddMood extends ActivityBase {

    // UI Components
    private Spinner moodSpinner;
    private MoodRepository moodRepository;
    private ImageView visibilityIcon;
    private boolean isPrivate = false; // default state of a mood is public
    private EditText reasonInput;
    private TextView timestampText;
    private Button addMoodButton;
    private ImageView locationIcon, importImageIcon, tagIcon, selectedImageView, profileImage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // Instead of a Place, a custom LocationWrapper is used to store location details.
    private LocationWrapper selectedLocation;
    private Uri selectedImageUri;
    private String currentPhotoPath;
    private TextView selectedLocationText;

    // For live character counting in the reason input field.
    private TextView reasonCharCountText;

    // Request codes for activity results and permissions.
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_PICK = 3;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2001;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 4;

    // Global list for tagged users (managed internally).
    private List<User> taggedUsers = new ArrayList<>();

    // For acquiring current location data.
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar progressBar;

    // Offline queue keys and MaterialBanner components for displaying queued moods.
    private static final String PREFS_QUEUE = "offline_queue";
    private static final String KEY_OFFLINE_MOODS = "offline_moods";
    private MaterialCardView offlineBanner;
    private TextView bannerText;

    // Flag to track previous network state.
    private boolean wasOffline = false;

    // Connectivity monitoring
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private String generatedImageUrl;


    private ActivityResultLauncher<Intent> autocompleteLauncher;

    /**
     * The {@code LocationWrapper} interface abstracts location information.
     * It defines methods to retrieve a location's name and geographical coordinates.
     * This abstraction allows flexibility in sourcing location data.
     */
    public interface LocationWrapper {
        /**
         * Retrieves the location's name (for example, a city name or address).
         *
         * @return A {@code String} representing the location name.
         */
        String getName();

        /**
         * Retrieves the geographical coordinates (latitude and longitude) of the location.
         *
         * @return A {@code LatLng} object representing the location coordinates.
         */
        LatLng getLatLng();
    }

    /**
     * The {@code DummyLocation} class is a simple implementation of the {@code LocationWrapper} interface.
     * It is used to store location information when a user selects a location either via the Google Places API
     * or by using their current location.
     */
    private class DummyLocation implements LocationWrapper {
        private final String name;
        private final LatLng latLng;

        /**
         * Constructs a new {@code DummyLocation} instance with the specified name and coordinates.
         *
         * @param name The name of the location.
         * @param lat  The latitude coordinate of the location.
         * @param lng  The longitude coordinate of the location.
         */
        public DummyLocation(String name, double lat, double lng) {
            this.name = name;
            this.latLng = new LatLng(lat, lng);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() { return name; }

        /**
         * {@inheritDoc}
         */
        @Override
        public LatLng getLatLng() { return latLng; }
    }

    /**
     * Updates the display text that shows the selected location and/or tagged user count.
     * Depending on what information is available, it shows location details, tag count, or both.
     */
    private void updateSelectedLocationDisplay() {
        String displayText = "";
        if (selectedLocation != null && !taggedUsers.isEmpty()) {
            displayText = "Location: " + selectedLocation.getName() + " | Tagged: " + taggedUsers.size();
        } else if (selectedLocation != null) {
            displayText = "Location: " + selectedLocation.getName();
        } else if (!taggedUsers.isEmpty()) {
            displayText = "Tagged: " + taggedUsers.size();
        }

        if (!displayText.isEmpty()) {
            selectedLocationText.setText(displayText);
            selectedLocationText.setTextColor(Color.parseColor("#439DEB"));
            selectedLocationText.setVisibility(View.VISIBLE);
        } else {
            selectedLocationText.setVisibility(View.GONE);
        }
    }

    /**
     * Called when the activity is first created. This method inflates the layout, initializes UI components,
     * configures Firebase, Places, and location services, sets up event listeners, registers network connectivity callbacks,
     * and loads user profile data.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the add_mood.xml layout into the content frame.
        getLayoutInflater().inflate(R.layout.add_mood, findViewById(R.id.content_frame), true);

        // Initialize the MoodRepository to handle database operations.
        moodRepository = new MoodRepository(this);

        // Initialize Firebase components.
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true) // Enables offline caching
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            String apiKey = bundle.getString("com.google.android.geo.API_KEY");
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), apiKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        autocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());
                        String city = "";
                        String country = "";
                        // Extract city (locality) and country code from the address components.
                        if (place.getAddressComponents() != null) {
                            for (AddressComponent component : place.getAddressComponents().asList()) {
                                if (component.getTypes().contains("locality")) {
                                    city = component.getName();
                                }
                                if (component.getTypes().contains("country")) {
                                    country = component.getShortName();
                                }
                            }
                        }
                        String locName = (city.isEmpty() ? "Unknown City" : city) + ", " + (country.isEmpty() ? "XX" : country);
                        selectedLocation = new DummyLocation(locName, Objects.requireNonNull(place.getLatLng()).latitude, place.getLatLng().longitude);
                        locationIcon.setColorFilter(Color.BLUE);
                        updateSelectedLocationDisplay();
                        showSnackbar("Location selected: " + selectedLocation.getName(), false);
                    } else if (result.getResultCode() == AutocompleteActivity.RESULT_ERROR) {
                        // Handle the error if needed
                        // Status status = Autocomplete.getStatusFromIntent(result.getData());
                        showSnackbar("Error retrieving location", false);
                    }
                }
        );

        // Initialize FusedLocationProviderClient for acquiring the current location.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Bind UI elements to their corresponding views in the layout.
        moodSpinner = findViewById(R.id.moodSpinner);

        // Bind the offline banner components from the included layout.
        offlineBanner = findViewById(R.id.offlineBanner);
        bannerText = findViewById(R.id.bannerText);

        visibilityIcon = findViewById(R.id.visibilityIcon);
        visibilityIcon.setOnClickListener(v -> {
            // Toggle the privacy state.
            isPrivate = !isPrivate;
            if (isPrivate) {
                // Set crossed-eye icon and change tint to blue.
                visibilityIcon.setImageResource(R.drawable.ic_eye_closed);
                visibilityIcon.setColorFilter(Color.BLUE);
                showSnackbar("Mood set to Private", getColor(R.color.excited_text));
            } else {
                // Set open-eye icon and reset tint.
                visibilityIcon.setImageResource(R.drawable.ic_eye_open);
                visibilityIcon.clearColorFilter();
                showSnackbar("Mood set to Public", false);
            }
        });

        reasonInput = findViewById(R.id.reasonInput);
        timestampText = findViewById(R.id.timestampText);
        addMoodButton = findViewById(R.id.addMoodButton);
        locationIcon = findViewById(R.id.locationIcon);
        importImageIcon = findViewById(R.id.importImageIcon);
        tagIcon = findViewById(R.id.tagIcon);
        selectedImageView = findViewById(R.id.selectedImageView);
        profileImage = findViewById(R.id.profileImage);
        reasonCharCountText = findViewById(R.id.reasonCharCountText);
        selectedLocationText = findViewById(R.id.selectedLocationText);
        progressBar = findViewById(R.id.progress_bar);

        // Load the logged-in user's profile image using Glide. If not available, use a default image.
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_profile);
        }

        // Configure the mood spinner with available mood options.
        String[] moodOptions = {"Select an Emotion", "Happy", "Sad", "Angry", "Scared", "Confused", "Disgusted", "Excited", "Ashamed"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moodOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(spinnerAdapter);
        moodSpinner.setSelection(0);

        // Set the current timestamp in the designated TextView.
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a - MMMM dd, yyyy", Locale.getDefault());
        timestampText.setText(sdf.format(new Date()));

        // Set up the reason input field: enforce a 200-character limit and update the live character counter.
        reasonInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        reasonInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                int remaining = 200 - s.length();
//                reasonCharCountText.setText(String.valueOf(remaining));
//            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                int remaining = 200 - s.length();
                reasonCharCountText.setText(String.valueOf(remaining));
                if (text.startsWith("#generate-reason")) {
                    addMoodButton.setText("Generate");
                    SpannableString spannable = new SpannableString(text);
                    ForegroundColorSpan blueSpan = new ForegroundColorSpan(0xFF2196F3);
                    spannable.setSpan(blueSpan, 0, "#generate-reason".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    reasonInput.removeTextChangedListener(this);
                    reasonInput.setText(spannable);
                    reasonInput.setSelection(spannable.length());
                    reasonInput.addTextChangedListener(this);
                }
                else if (text.startsWith("#generate-image")) {
                    addMoodButton.setText("Generate");
                    SpannableString spannable = new SpannableString(text);
                    ForegroundColorSpan blueSpan = new ForegroundColorSpan(0xFF2196F3);
                    spannable.setSpan(blueSpan, 0, "#generate-image".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    reasonInput.removeTextChangedListener(this);
                    reasonInput.setText(spannable);
                    reasonInput.setSelection(spannable.length());
                    reasonInput.addTextChangedListener(this);
                }
                else {
                    addMoodButton.setText("Add Mood");
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Set up click listener for the location icon to show the location selection popup.
        locationIcon.setOnClickListener(v -> showLocationPopup());

        // Set up click listener for the image import icon to open the image picker.
        importImageIcon.setOnClickListener(v -> showImagePickerDialog());

        // Set up click listener for the tag icon to open the tagging popup.
        tagIcon.setOnClickListener(v -> showTagUsersDialog());

        // Set up click listener for the Add Mood button to validate inputs and initiate mood saving.
        addMoodButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            if (moodSpinner.getSelectedItemPosition() == 0) {
                progressBar.setVisibility(View.GONE);
                showSnackbar("Please select an emotion.");
                return;
            }
            handleAddMoodButtonClick();
        });


        // Initialize connectivity monitoring.
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        registerNetworkCallback();
    }

    private void handleAddMoodButtonClick() {
        String currentButtonText = addMoodButton.getText().toString();
        if ("Generate".equals(currentButtonText)) {
            String reason = reasonInput.getText().toString().trim();
            if (reason.startsWith("#generate-reason")) {
                generateReasonAndUpdateUI();
            } else if (reason.startsWith("#generate-image")) {
                generateImageAndUpdateUI();
            } else {
                saveMoodNormally();
            }
        } else {
            saveMoodNormally();
        }
    }

    private void generateReasonAndUpdateUI() {
        String reason = reasonInput.getText().toString().trim();
        if (!reason.startsWith("#generate-reason")) {
            showSnackbar("Please enter a prompt starting with #generate-reason");
            progressBar.setVisibility(View.GONE);
            return;
        }
        String prompt = reason.substring("#generate-reason".length()).trim();
        ReasonGenerator reasonGenerator = new ReasonGenerator();
        reasonGenerator.generateReason(prompt, new ReasonGenerator.ReasonGeneratorCallback() {
            @Override
            public void onSuccess(String generatedText) {
                reasonInput.setText(generatedText);
                addMoodButton.setText("Add Mood");
                showSnackbar("AI text Generated successfully", false);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                showSnackbar("Failed to generate text: " + e.getMessage());
            }
        });
    }
    private void generateImageAndUpdateUI() {
        String reason = reasonInput.getText().toString().trim();
        if (!reason.startsWith("#generate-image")) {
            showSnackbar("Please enter a prompt starting with #generate-image");
            progressBar.setVisibility(View.GONE);
            return;
        }
        String prompt = reason.substring("#generate-image".length()).trim();
        ImageGenerator imageGenerator = new ImageGenerator();
        imageGenerator.generateImage(prompt, new ImageGenerator.ImageGeneratorCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Glide.with(AddMood.this).load(imageUrl).into(selectedImageView);
                selectedImageView.setVisibility(ImageView.VISIBLE);
                generatedImageUrl = imageUrl;
                selectedImageUri = null;
                importImageIcon.setColorFilter(Color.BLUE);
                reasonInput.setText("");
                addMoodButton.setText("Add Mood");
                showSnackbar("AI image Generated successfully", false);
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                showSnackbar("Failed to generate image: " + e.getMessage());
            }
        });
    }


    private void saveMoodNormally() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showSnackbar("User not logged in");
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Basic mood validation
        String mood = moodSpinner.getSelectedItem().toString();
        if (mood.equals("Select an Emotion")) {
            showSnackbar("Please select an emotion");
            progressBar.setVisibility(View.GONE);
            return;
        }

        String reason = reasonInput.getText().toString().trim();
        String timestamp = timestampText.getText().toString();

        Map<String, Object> moodData = new HashMap<>();
        moodData.put("mood", mood);
        moodData.put("reason", reason);
        moodData.put("timestamp", timestamp);
        moodData.put("isPrivate", isPrivate);

        // Add location data if available
        if (selectedLocation != null) {
            moodData.put("locationName", selectedLocation.getName());
            moodData.put("locationLat", selectedLocation.getLatLng().latitude);
            moodData.put("locationLng", selectedLocation.getLatLng().longitude);
        }

        // Add tag data if available
        if (!taggedUsers.isEmpty()) {
            List<Map<String, Object>> tags = new ArrayList<>();
            for (User taggedUser : taggedUsers) {
                Map<String, Object> tagInfo = new HashMap<>();
                tagInfo.put("userId", taggedUser.getId());
                tagInfo.put("username", taggedUser.getUsername());
                tagInfo.put("name", taggedUser.getName());
                tags.add(tagInfo);
            }
            moodData.put("tags", tags);
        }

        // Handle AI-generated image
        if (generatedImageUrl != null) {
            if (!isNetworkAvailable()) {
                moodData.put("generatedImageUrl", generatedImageUrl);
                addMoodToQueue(moodData, null);
                progressBar.setVisibility(View.GONE);
                int count = getOfflineQueueCount();
                showSnackbar("No internet. Mood queued. Total queued: " + count, false);
                updateOfflineBanner();
                resetFields();
                return;
            }

            processGeneratedImage(moodData);
            return;
        }

        // Handle regular image upload
        if (!isNetworkAvailable()) {
            addMoodToQueue(moodData, selectedImageUri);
            progressBar.setVisibility(View.GONE);
            int count = getOfflineQueueCount();
            showSnackbar("No internet. Mood queued. Total queued: " + count, false);
            updateOfflineBanner();
            resetFields();
            return;
        }

        // Standard mood save with potential image upload
        moodRepository.saveMood(moodData, selectedImageUri, new MoodRepository.SaveMoodCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                resetFields();
                showSnackbar("Mood added successfully", false);
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                showSnackbar("Error adding mood: " + e.getMessage());
            }
        });
    }

    /**
     * Process and upload an AI-generated image to Firebase Storage
     * then save the mood with the image URL
     *
     * @param moodData The mood data map to save with the image URL
     */
    private void processGeneratedImage(Map<String, Object> moodData) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                showSnackbar("User not logged in");
            });
            return;
        }

        new Thread(() -> {
            try {
                // Download the AI-generated image using Glide
                Bitmap bitmap = Glide.with(AddMood.this)
                        .asBitmap()
                        .load(generatedImageUrl)
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();

                byte[] compressedBytes = ImageCompressor.compressImage(bitmap, 64000);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                StorageReference imageRef = storage.getReference("mood_images/" + user.getUid() + "/" + timeStamp + ".jpg");

                // Upload the compressed image to Firebase Storage
                imageRef.putBytes(compressedBytes)
                        .addOnSuccessListener(taskSnapshot -> {
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                moodData.put("imageUrl", uri.toString());

                                moodRepository.saveMood(moodData, null, new MoodRepository.SaveMoodCallback() {
                                    @Override
                                    public void onSuccess() {
                                        runOnUiThread(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            resetFields();
                                            showSnackbar("Mood with generated image added successfully", false);
                                        });
                                    }
                                    @Override
                                    public void onFailure(Exception e) {
                                        runOnUiThread(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            showSnackbar("Error adding mood: " + e.getMessage());
                                        });
                                    }
                                });
                            }).addOnFailureListener(e -> {
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    showSnackbar("Error getting download URL: " + e.getMessage());
                                });
                            });
                        }).addOnFailureListener(e -> {
                            runOnUiThread(() -> {
                                progressBar.setVisibility(View.GONE);
                                showSnackbar("Error uploading image: " + e.getMessage());
                            });
                        });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    showSnackbar("Error processing generated image: " + e.getMessage());
                });
            }
        }).start();
    }


    /**
     * Registers a network callback to listen for connectivity changes.
     * When the network becomes available, a "Back online" message is displayed and queued moods are synced.
     */
    private void registerNetworkCallback() {
        NetworkRequest networkRequest = new NetworkRequest.Builder().build();
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onLost(Network network) {
                // When network is lost, mark the state as offline.
                wasOffline = true;
            }
            @Override
            public void onAvailable(Network network) {
                // Only show "Back online" if we were offline before.
                runOnUiThread(() -> {
                    if (wasOffline) {
                        showSnackbar("Back online", false);
                        wasOffline = false;
                    }
                    syncQueuedMoods();
                    updateOfflineBanner();
                });
            }
        };
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    /**
     * Unregisters the network callback.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    /**
     * Called when the activity resumes.
     * Updates the offline banner and attempts to sync any queued moods if connectivity is available.
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateOfflineBanner();
        syncQueuedMoods();
    }

    // --- LOCATION POPUP ---

    /**
     * Displays a popup dialog that allows the user to select a location.
     * The dialog provides options to use the current location, search for a location using the Google Places Autocomplete,
     * remove the current location, or cancel the selection.
     */
    private void showLocationPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        // Inflate the custom layout for the location popup.
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_selection, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button btnUseCurrent = dialogView.findViewById(R.id.btn_use_current);
        Button btnSearchLocation = dialogView.findViewById(R.id.btn_search_location);
        Button btnRemoveLocation = dialogView.findViewById(R.id.btn_remove_location);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_location);

        // Only show the Remove Location button if a location has already been selected.
        btnRemoveLocation.setVisibility(selectedLocation == null ? View.GONE : View.VISIBLE);

        btnUseCurrent.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                showSnackbar("Location permission required");
                return;
            }
            // Obtain the current location with high accuracy.
            fusedLocationClient.getCurrentLocation(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Geocoder geocoder = new Geocoder(AddMood.this, Locale.getDefault());
                            try {
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    // Extract city and country information from the address.
                                    String city = address.getLocality();
                                    String country = address.getCountryCode();
                                    String locName;
                                    if (city != null && country != null) {
                                        locName = city + ", " + country;
                                    } else if (city != null) {
                                        locName = city;
                                    } else if (country != null) {
                                        locName = country;
                                    } else {
                                        locName = "Unknown Location";
                                    }
                                    selectedLocation = new DummyLocation(locName, location.getLatitude(), location.getLongitude());
                                    locationIcon.setColorFilter(Color.BLUE);
                                    updateSelectedLocationDisplay();
                                    showSnackbar("Location selected: " + selectedLocation.getName(), false);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                showSnackbar("Error retrieving address");
                            }
                        } else {
                            showSnackbar("Unable to get current location");
                        }
                    });
            dialog.dismiss();
        });

        // Set up the Search Location button to launch the Places Autocomplete search.
        btnSearchLocation.setVisibility(View.VISIBLE);
        btnSearchLocation.setOnClickListener(v -> {
            launchPlaceAutocomplete();
            dialog.dismiss();
        });

        btnRemoveLocation.setOnClickListener(v -> {
            selectedLocation = null;
            locationIcon.clearColorFilter();
            updateSelectedLocationDisplay();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Launches the Google Places Autocomplete activity for location search.
     * This allows the user to search for landmarks or other places.
     */
    private void launchPlaceAutocomplete() {
        // Define the fields to return after a selection.
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
        );
        // Build the autocomplete intent in OVERLAY mode.
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        autocompleteLauncher.launch(intent);
    }

    // --- IMAGE SELECTION METHODS ---

    /**
     * Initiates the image picker dialog to allow the user to select an image.
     */
    private void showImagePickerDialog() {
        openGallery();
    }

    /**
     * Dispatches an intent to capture an image using the device camera.
     * Checks for necessary camera permissions before launching the camera.
     */
    private void dispatchTakePictureIntent() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e("AddMood", "Error creating image file", ex);
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.whimsy.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Callback for the result from requesting permissions. Handles camera permission results.
     *
     * @param requestCode  The request code passed in {@link ActivityCompat#requestPermissions}.
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                showSnackbar("Camera permission denied");
            }
        }
    }

    /**
     * Creates a temporary file to store an image captured by the camera.
     *
     * @return A {@code File} object representing the temporary image file.
     * @throws IOException if an error occurs while creating the file.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Opens the device's image gallery for the user to pick an image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // --- ON ACTIVITY RESULT ---

    /**
     * Handles results from launched activities (camera capture, image picking, and location autocomplete).
     * Displays the selected image or updates location details accordingly.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity.
     * @param data        An {@code Intent} that carries the result data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedImageUri = Uri.fromFile(new File(currentPhotoPath));
            displaySelectedImage(selectedImageUri);
            importImageIcon.setColorFilter(Color.BLUE);
            setupImageRemoval();
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            displaySelectedImage(selectedImageUri);
            importImageIcon.setColorFilter(Color.BLUE);
            setupImageRemoval();
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            String city = "";
            String country = "";
            // Extract city (locality) and country code from the place's address components.
            if (place.getAddressComponents() != null) {
                for (AddressComponent component : place.getAddressComponents().asList()) {
                    if (component.getTypes().contains("locality")) {
                        city = component.getName();
                    }
                    if (component.getTypes().contains("country")) {
                        country = component.getShortName();
                    }
                }
            }
            String locName = (city.isEmpty() ? "Unknown City" : city) + ", " + (country.isEmpty() ? "XX" : country);
            selectedLocation = new DummyLocation(locName, place.getLatLng().latitude, place.getLatLng().longitude);
            locationIcon.setColorFilter(Color.BLUE);
            updateSelectedLocationDisplay();
            showSnackbar("Location selected: " + selectedLocation.getName(), false);
        }
    }

    /**
     * Displays the selected image in the UI.
     *
     * @param uri The {@code Uri} of the image to display.
     */
    private void displaySelectedImage(Uri uri) {
        selectedImageView.setImageURI(uri);
        selectedImageView.setVisibility(View.VISIBLE);
    }

    /**
     * Sets up a click listener on the displayed image to allow the user to remove it.
     * A confirmation dialog is shown before removal.
     */
    private void setupImageRemoval() {
        selectedImageView.setOnClickListener(v -> {
            new AlertDialog.Builder(AddMood.this)
                    .setTitle("Remove Image")
                    .setMessage("Do you want to remove the selected image?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        selectedImageUri = null;
                        selectedImageView.setVisibility(View.GONE);
                        importImageIcon.clearColorFilter();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    // --- OFFLINE QUEUE HELPER METHODS ---

    /**
     * Checks if network connectivity is available.
     *
     * @return {@code true} if the network is available, {@code false} otherwise.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Adds the provided mood data to the offline queue.
     * The mood data is stored in SharedPreferences as a JSON array.
     *
     * @param moodData The mood data to queue.
     * @param imageUri The {@code Uri} of the selected image (if any).
     */
    private void addMoodToQueue(Map<String, Object> moodData, Uri imageUri) {
        if (imageUri != null) {
            moodData.put("imageUri", imageUri.toString());
        }
        try {
            JSONObject jsonObject = new JSONObject(moodData);
            SharedPreferences prefs = getSharedPreferences(PREFS_QUEUE, MODE_PRIVATE);
            String queueString = prefs.getString(KEY_OFFLINE_MOODS, "[]");
            JSONArray queueArray = new JSONArray(queueString);
            queueArray.put(jsonObject);
            prefs.edit().putString(KEY_OFFLINE_MOODS, queueArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the number of moods currently queued offline.
     *
     * @return The count of queued moods.
     */
    private int getOfflineQueueCount() {
        SharedPreferences prefs = getSharedPreferences(PREFS_QUEUE, MODE_PRIVATE);
        String queueString = prefs.getString(KEY_OFFLINE_MOODS, "[]");
        try {
            JSONArray queueArray = new JSONArray(queueString);
            return queueArray.length();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Updates the offline banner to display the current number of queued moods.
     * If there are queued moods, the banner is made visible; otherwise, it is hidden.
     */
    private void updateOfflineBanner() {
        int count = getOfflineQueueCount();
        if (count > 0) {
            offlineBanner.setVisibility(View.VISIBLE);
            bannerText.setText("Queued moods: " + count);
        } else {
            offlineBanner.setVisibility(View.GONE);
        }
    }

    /**
     * Attempts to sync queued moods when the network is available.
     * Each queued mood is posted via the MoodRepository, and on success, removed from the queue.
     */
    private void syncQueuedMoods() {
        if (!isNetworkAvailable()) {
            return;
        }
        SharedPreferences prefs = getSharedPreferences(PREFS_QUEUE, MODE_PRIVATE);
        String queueString = prefs.getString(KEY_OFFLINE_MOODS, "[]");
        try {
            JSONArray queueArray = new JSONArray(queueString);
            if (queueArray.length() == 0) {
                updateOfflineBanner(); // Hide banner if empty.
                return;
            }
            // Loop through each queued mood and attempt to sync.
            for (int i = 0; i < queueArray.length(); i++) {
                JSONObject moodJson = queueArray.getJSONObject(i);
                Map<String, Object> moodData = jsonToMap(moodJson);
                Uri imageUri = null;
                if (moodData.containsKey("imageUri")) {
                    imageUri = Uri.parse((String) moodData.get("imageUri"));
                    moodData.remove("imageUri");
                }
                moodRepository.saveMood(moodData, imageUri, new MoodRepository.SaveMoodCallback() {
                    @Override
                    public void onSuccess() {
                        removeMoodFromQueue(moodJson);
                        updateOfflineBanner();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        // Leave the mood in the queue if syncing fails.
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes a mood from the offline queue.
     *
     * @param moodJson The {@code JSONObject} representing the mood to remove.
     */
    private void removeMoodFromQueue(JSONObject moodJson) {
        SharedPreferences prefs = getSharedPreferences(PREFS_QUEUE, MODE_PRIVATE);
        String queueString = prefs.getString(KEY_OFFLINE_MOODS, "[]");
        try {
            JSONArray queueArray = new JSONArray(queueString);
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < queueArray.length(); i++) {
                JSONObject obj = queueArray.getJSONObject(i);
                if (!obj.toString().equals(moodJson.toString())) {
                    newArray.put(obj);
                }
            }
            prefs.edit().putString(KEY_OFFLINE_MOODS, newArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts a {@code JSONObject} to a {@code Map<String, Object>}.
     *
     * @param json The {@code JSONObject} to convert.
     * @return A {@code Map<String, Object>} representation of the JSON data.
     */
    private Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<>();
        try {
            JSONArray keys = json.names();
            if (keys != null) {
                for (int i = 0; i < keys.length(); i++) {
                    String key = keys.getString(i);
                    Object value = json.get(key);
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // --- SAVE MOOD TO FIRESTORE ---

    /**
     * Validates user inputs and assembles mood data into a map.
     * Delegates the process of saving mood data (and optionally an image) to the MoodRepository.
     * If the device is offline, the mood is queued and the offline banner is updated.
     * After adding a mood (online or offline), all fields are reset.
     */
    private void saveMoodToFirebase() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showSnackbar("User not logged in");
            return;
        }
        String mood = moodSpinner.getSelectedItem().toString();
        if (!InputValidator.isValidMood(mood)) {
            showSnackbar("Please select a valid emotion.");
            progressBar.setVisibility(View.GONE);
            return;
        }
        String reason = reasonInput.getText().toString().trim();
        String reasonError = InputValidator.validateReason(reason);
        if (reasonError != null) {
            showSnackbar(reasonError);
            progressBar.setVisibility(View.GONE);
            return;
        }
        String timestamp = timestampText.getText().toString();
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("mood", mood);
        moodData.put("reason", reason);
        moodData.put("timestamp", timestamp);
        moodData.put("isPrivate", isPrivate);

        if (selectedLocation != null) {
            moodData.put("locationName", selectedLocation.getName());
            moodData.put("locationLat", selectedLocation.getLatLng().latitude);
            moodData.put("locationLng", selectedLocation.getLatLng().longitude);
        }
        if (!taggedUsers.isEmpty()) {
            List<Map<String, Object>> tags = new ArrayList<>();
            for (User taggedUser : taggedUsers) {
                Map<String, Object> tagInfo = new HashMap<>();
                tagInfo.put("userId", taggedUser.getId());
                tagInfo.put("username", taggedUser.getUsername());
                tagInfo.put("name", taggedUser.getName());
                tags.add(tagInfo);
            }
            moodData.put("tags", tags);
        }

        if (reason.startsWith("#generate-image")) {
            String prompt = reason.substring("#generate-image".length()).trim();
            ImageGenerator imageGenerator = new ImageGenerator();
            imageGenerator.generateImage(prompt, new ImageGenerator.ImageGeneratorCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    Glide.with(AddMood.this)
                            .load(imageUrl)
                            .into(selectedImageView);
                    selectedImageView.setVisibility(ImageView.VISIBLE);
                    Log.d("AddMood", "AI image generated with URL: " + imageUrl);
                    generatedImageUrl = imageUrl;
                    selectedImageUri = null;
                    processGeneratedImage(moodData);
                }
                @Override
                public void onFailure(Exception e) {
                    progressBar.setVisibility(View.GONE);
                    showSnackbar("Failed to generate image: " + e.getMessage());
                }
            });
            return; // NEW: Exit early since AI image branch is handled asynchronously.
        }

        // If a normal image is selected via gallery/camera, use that.
        if (selectedImageUri != null) {
            Log.d("AddMood", "Using selectedImageUri: " + selectedImageUri.toString());
        }

        // If offline, queue the mood.
        if (!isNetworkAvailable()) {
            addMoodToQueue(moodData, selectedImageUri);
            progressBar.setVisibility(View.GONE);
            int count = getOfflineQueueCount();
            showSnackbar("No internet. Mood queued. Total queued: " + count, false);
            updateOfflineBanner();
            resetFields();
            return;
        }

        moodRepository.saveMood(moodData, selectedImageUri, new MoodRepository.SaveMoodCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                resetFields();
                showSnackbar("Mood added successfully", false);
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                showSnackbar("Error adding mood: " + e.getMessage());
            }
        });
    }

    private void resetFields() {
        moodSpinner.setSelection(0);
        reasonInput.setText("");
        selectedImageView.setVisibility(ImageView.GONE);
        selectedImageUri = null;
        importImageIcon.clearColorFilter();
        locationIcon.clearColorFilter();
        selectedLocation = null;
        taggedUsers.clear();
        tagIcon.clearColorFilter();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a - MMMM dd, yyyy", Locale.getDefault());
        timestampText.setText(sdf.format(new Date()));
    }


    // --- TAGGING FUNCTIONALITY ---

    /**
     * Displays a tagging dialog that allows users to add or remove tags.
     * The dialog includes a search bar to query users and a RecyclerView to display matching results.
     * Selected users are maintained in the global taggedUsers list.
     */
    private void showTagUsersDialog() {
        final ArrayList<User> tempTaggedUsers = new ArrayList<>(taggedUsers);
        final List<User> currentData = new ArrayList<>();
        currentData.addAll(tempTaggedUsers);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tag_users_rounded, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        final EditText searchEditText = dialogView.findViewById(R.id.tag_search_edit_text);
        final ImageView searchIcon = dialogView.findViewById(R.id.search_icon);
        final RecyclerView recyclerView = dialogView.findViewById(R.id.tag_search_results_recycler_view);
        Button applyButton = dialogView.findViewById(R.id.tag_apply_button);
        Button cancelButton = dialogView.findViewById(R.id.tag_cancel_button);

        final TagUsersAdapter adapter = new TagUsersAdapter(currentData, tempTaggedUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    currentData.clear();
                    currentData.addAll(tempTaggedUsers);
                    adapter.notifyDataSetChanged();
                    searchIcon.clearColorFilter();
                } else {
                    searchIcon.setColorFilter(getColor(R.color.black));
                    db.collection("users")
                            .orderBy("name")
                            .startAt(query)
                            .endAt(query + "\uf8ff")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                currentData.clear();
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    String id = doc.getId();
                                    String username = doc.getString("username");
                                    String name = doc.getString("name");
                                    User user = new User(id, name, username, doc.getString("profilePictureUrl"));
                                    currentData.add(user);
                                }
                                adapter.notifyDataSetChanged();
                            });
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        applyButton.setOnClickListener(v -> {
            taggedUsers.clear();
            taggedUsers.addAll(tempTaggedUsers);
            if (!taggedUsers.isEmpty()) {
                tagIcon.setColorFilter(Color.BLUE);
            } else {
                tagIcon.clearColorFilter();
            }
            // Update the display to reflect the current tag count.
            updateSelectedLocationDisplay();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    /**
     * RecyclerView Adapter for displaying user search results in the tagging dialog.
     */
    private class TagUsersAdapter extends RecyclerView.Adapter<TagUsersAdapter.ViewHolder> {
        private List<User> users;
        private List<User> selectedUsers;

        /**
         * Constructs a new {@code TagUsersAdapter} with the provided lists.
         *
         * @param users         The list of users to display.
         * @param selectedUsers The list of currently selected users.
         */
        TagUsersAdapter(List<User> users, List<User> selectedUsers) {
            this.users = users;
            this.selectedUsers = selectedUsers;
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        @Override
        public int getItemCount() {
            return users.size();
        }

        /**
         * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent an item.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        @Override
        public TagUsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_user, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Called by RecyclerView to display the data at the specified position.
         * This method should update the contents of the {@link ViewHolder#itemView} to reflect the item at the given position.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(TagUsersAdapter.ViewHolder holder, int position) {
            User user = users.get(position);
            holder.nameText.setText(user.getName());
            holder.usernameText.setText("@" + user.getUsername());
            Glide.with(holder.profileImage.getContext())
                    .load(user.getProfilePictureUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.profileImage);
            if (isUserSelected(user)) {
                holder.itemView.setBackgroundColor(Color.LTGRAY);
            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.itemView.setOnClickListener(v -> {
                if (isUserSelected(user)) {
                    for (int i = 0; i < selectedUsers.size(); i++) {
                        if (selectedUsers.get(i).getId().equals(user.getId())) {
                            selectedUsers.remove(i);
                            break;
                        }
                    }
                } else {
                    selectedUsers.add(user);
                }
                notifyItemChanged(position);
            });
        }

        /**
         * Checks whether a given user is currently selected.
         *
         * @param user The user to check.
         * @return {@code true} if the user is selected; {@code false} otherwise.
         */
        private boolean isUserSelected(User user) {
            for (User u : selectedUsers) {
                if (u.getId().equals(user.getId())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * ViewHolder class for the RecyclerView items in the tagging dialog.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView profileImage;
            TextView nameText, usernameText;

            /**
             * Constructs a new {@code ViewHolder} and binds UI elements.
             *
             * @param itemView The view of the item.
             */
            ViewHolder(View itemView) {
                super(itemView);
                profileImage = itemView.findViewById(R.id.profile_image);
                nameText = itemView.findViewById(R.id.text_display_name);
                usernameText = itemView.findViewById(R.id.text_username);
            }
        }
    }
}
