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
 *     <li>Location selection using the current location or Google Places autocomplete.</li>
 *     <li>Image upload from either the camera or the gallery.</li>
 *     <li>User tagging functionality for mood entries.</li>
 *     <li>Input validation and error handling with user feedback.</li>
 *     <li>Integration with Firebase for persistent data storage and retrieval.</li>
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddMood extends ActivityBase {

    // UI Components
    private Spinner moodSpinner;
    private MoodRepository moodRepository;
    private Switch privacySwitch;
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

    // Global list for tagged users (managed internally).
    private List<User> taggedUsers = new ArrayList<>();

    // For acquiring current location data.
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar progressBar;

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
     * configures Firebase and location services, sets up event listeners, and loads user profile data.
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

        // Initialize FusedLocationProviderClient for acquiring the current location.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Bind UI elements to their corresponding views in the layout.
        moodSpinner = findViewById(R.id.moodSpinner);
        privacySwitch = findViewById(R.id.privacySwitch);
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
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int remaining = 200 - s.length();
                reasonCharCountText.setText(String.valueOf(remaining));
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
            // Show the progress bar while saving the mood.
            progressBar.setVisibility(View.VISIBLE);

            if (moodSpinner.getSelectedItemPosition() == 0) {
                progressBar.setVisibility(View.GONE);
                showSnackbar("Please select an emotion.");
                return;
            }
            saveMoodToFirebase();
        });
    }

    /**
     * Displays a Snackbar with the specified message.
     *
     * @param message The message to display.
     */
    private void showSnackbar(String message) {
        showSnackbar(message, true);
    }

    /**
     * Helper method to display a Snackbar at the bottom of the screen with an optional error style.
     *
     * @param message The message to display.
     * @param isError If {@code true}, the Snackbar is styled as an error; otherwise, it is styled as a success message.
     */
    private void showSnackbar(String message, boolean isError) {
        View parentView = findViewById(R.id.content_frame);
        Snackbar snackbar;
        if (isError) {
            snackbar = Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(Color.RED)
                    .setTextColor(Color.WHITE);
        } else {
            snackbar = Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(R.color.dark_green))
                    .setTextColor(Color.WHITE);
        }
        View snackbarView = snackbar.getView();
        // Adjust the Snackbar position by moving it up by 150 pixels.
        snackbarView.setTranslationY(-150);
        snackbar.show();
    }

    // --- LOCATION POPUP ---
    /**
     * Displays a popup dialog that allows the user to select a location.
     * The dialog provides options to use the current location, remove the current location,
     * or cancel the selection.
     */
    private void showLocationPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        // Inflate the custom layout for the location popup.
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_selection, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button btnUseCurrent = dialogView.findViewById(R.id.btn_use_current);
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

        btnRemoveLocation.setOnClickListener(v -> {
            selectedLocation = null;
            locationIcon.clearColorFilter();
            updateSelectedLocationDisplay();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // --- IMAGE SELECTION METHODS ---
    /**
     * Initiates the image picker dialog to allow the user to select an image.
     */
    private void showImagePickerDialog() {
        openGallery();
    }

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2001;

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
     * Handles results from launched activities (camera capture and image picking).
     * Displays the selected image and updates UI elements accordingly.
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

    // --- SAVE MOOD TO FIRESTORE ---
    /**
     * Validates user inputs and assembles mood data into a map.
     * Delegates the process of saving mood data (and optionally an image) to the MoodRepository.
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

        boolean isPrivate = privacySwitch.isChecked();
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

        // Delegate the save operation to the MoodRepository.
        moodRepository.saveMood(moodData, selectedImageUri, new MoodRepository.SaveMoodCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                // Clear the UI upon successful save.
                reasonInput.setText("");
                selectedImageView.setVisibility(View.GONE);
                selectedImageUri = null;
                importImageIcon.clearColorFilter();
                locationIcon.clearColorFilter();
                selectedLocation = null;
                taggedUsers.clear();
                tagIcon.clearColorFilter();
                showSnackbar("Mood added successfully", false);
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                showSnackbar("Error adding mood: " + e.getMessage());
            }
        });
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

        @Override
        public int getItemCount() {
            return users.size();
        }

        @Override
        public TagUsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_user, parent, false);
            return new ViewHolder(view);
        }

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
