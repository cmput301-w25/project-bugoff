package com.example.project1;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
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
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.maps.model.LatLng;

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

    private Spinner moodSpinner;
    private EditText triggerInput, reasonInput;
    private TextView timestampText;
    private Button addMoodButton;
    private ImageView locationIcon, importImageIcon, tagIcon, selectedImageView, profileImage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    // Instead of a Place, we use a custom LocationWrapper to store location info.
    private LocationWrapper selectedLocation;
    private Uri selectedImageUri;
    private String currentPhotoPath;

    // Views for displaying tagged users as chips
    private HorizontalScrollView taggedUsersScrollView;
    private LinearLayout taggedUsersContainer;

    // For the live character counter on the Reason field.
    private TextView reasonCharCountText;

    // Request codes
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_PICK = 3;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Global list for tagged users
    private List<User> taggedUsers = new ArrayList<>();

    // For current location
    private FusedLocationProviderClient fusedLocationClient;

    // --- Interface for location info ---
    public interface LocationWrapper {
        String getName();
        LatLng getLatLng();
    }

    // DummyLocation: simple implementation of LocationWrapper.
    private class DummyLocation implements LocationWrapper {
        private final String name;
        private final LatLng latLng;
        public DummyLocation(String name, double lat, double lng) {
            this.name = name;
            this.latLng = new LatLng(lat, lng);
        }
        @Override
        public String getName() { return name; }
        @Override
        public LatLng getLatLng() { return latLng; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate your provided add_mood.xml (the layout you shared)
        getLayoutInflater().inflate(R.layout.add_mood, findViewById(R.id.content_frame), true);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize Places API (replace "YOUR_API_KEY" with your actual key)
        Places.initialize(getApplicationContext(), "YOUR_API_KEY");
        PlacesClient placesClient = Places.createClient(this);

        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI elements (IDs from your provided XML)
        moodSpinner = findViewById(R.id.moodSpinner);
        triggerInput = findViewById(R.id.triggerInput);
        reasonInput = findViewById(R.id.reasonInput);
        timestampText = findViewById(R.id.timestampText);
        addMoodButton = findViewById(R.id.addMoodButton);
        locationIcon = findViewById(R.id.locationIcon);
        importImageIcon = findViewById(R.id.importImageIcon);
        tagIcon = findViewById(R.id.tagIcon);
        selectedImageView = findViewById(R.id.selectedImageView);
        taggedUsersScrollView = findViewById(R.id.taggedUsersScrollView);
        taggedUsersContainer = findViewById(R.id.taggedUsersContainer);
        profileImage = findViewById(R.id.profileImage);
        reasonCharCountText = findViewById(R.id.reasonCharCountText); // Make sure this TextView is added in your XML below Reason input

        // Load logged in user's profile image (it is rounded via XML)
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_profile);
        }

        // Setup Spinner: use an array with the placeholder as the first item.
        String[] moodOptions = {"Select an Emotion", "Happy", "Sad", "Angry", "Scared", "Confused", "Disgusted", "Surprised", "Shameful"};
        // Use default Android layouts for spinner items.
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moodOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(spinnerAdapter);
        // Always start with the placeholder.
        moodSpinner.setSelection(0);

        // Set current timestamp.
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a - MMMM dd, yyyy", Locale.getDefault());
        timestampText.setText(sdf.format(new Date()));

        // Setup Reason input: Limit to 20 characters and show remaining count.
        reasonInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        reasonInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int remaining = 20 - s.length();
                reasonCharCountText.setText(String.valueOf(remaining));
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Location icon: open the location popup.
        locationIcon.setOnClickListener(v -> showLocationPopup());

        // Image import icon: open image picker.
        importImageIcon.setOnClickListener(v -> showImagePickerDialog());

        // Tag icon: open the tagging popup.
        tagIcon.setOnClickListener(v -> showTagUsersDialog());

        // Add Mood button: Validate inputs and then save.
        addMoodButton.setOnClickListener(v -> {
            if (moodSpinner.getSelectedItemPosition() == 0) {
                showSnackbar("Please select an emotion.");
                return;
            }
            if (reasonInput.getText().toString().trim().isEmpty()) {
                showSnackbar("Please enter a reason (max 20 characters).");
                return;
            }
            saveMoodToFirebase();
        });
    }

    // Helper method to show error messages as a bottom Snackbar.
    private void showSnackbar(String message) {
        View parentView = findViewById(R.id.content_frame);
        Snackbar snackbar= Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT).setDuration(1500).setBackgroundTint(Color.RED).setTextColor(Color.WHITE);
        View snackbarView = snackbar.getView();
        snackbarView.setTranslationY(-150);
        snackbar.show();
    }

    // --- LOCATION POPUP ---
    private void showLocationPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate your custom location popup layout (create dialog_location_selection.xml as needed)
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_selection, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        Button btnUseCurrent = dialogView.findViewById(R.id.btn_use_current);
        Button btnSearchLocation = dialogView.findViewById(R.id.btn_search_location);
        Button btnRemoveLocation = dialogView.findViewById(R.id.btn_remove_location);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_location);

        btnRemoveLocation.setVisibility(selectedLocation == null ? View.GONE : View.VISIBLE);

        btnUseCurrent.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                showSnackbar("Location permission required");
                return;
            }
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    Geocoder geocoder = new Geocoder(AddMood.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address address = addresses.get(0);
                            String locName = (address.getFeatureName() != null && !address.getFeatureName().isEmpty())
                                    ? address.getFeatureName() : address.getLocality();
                            if (locName == null || locName.isEmpty()) {
                                locName = "Current Location";
                            }
                            selectedLocation = new DummyLocation(locName, location.getLatitude(), location.getLongitude());
                            locationIcon.setColorFilter(Color.BLUE);
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

        btnSearchLocation.setOnClickListener(v -> {
            List<Place.Field> fields = java.util.Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(AddMood.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            dialog.dismiss();
        });

        btnRemoveLocation.setOnClickListener(v -> {
            selectedLocation = null;
            locationIcon.clearColorFilter();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // --- IMAGE SELECTION METHODS ---
    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        String[] options = {"Take Photo", "Choose from Gallery"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                dispatchTakePictureIntent();
            } else {
                openGallery();
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
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
                        "com.example.project1.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // --- ON ACTIVITY RESULT ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                selectedLocation = new DummyLocation(place.getName(), place.getLatLng().latitude, place.getLatLng().longitude);
                locationIcon.setColorFilter(Color.BLUE);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e("AddMood", "Error retrieving location: " + status);
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
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

    // --- DISPLAY SELECTED IMAGE ---
    private void displaySelectedImage(Uri uri) {
        selectedImageView.setImageURI(uri);
        selectedImageView.setVisibility(View.VISIBLE);
    }

    // --- SETUP IMAGE REMOVAL ---
    private void setupImageRemoval() {
        selectedImageView.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(AddMood.this)
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
    private void saveMoodToFirebase() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            showSnackbar("User not logged in");
            return;
        }
        String mood = moodSpinner.getSelectedItem().toString();
        // The first item is the placeholder.
        String trigger = triggerInput.getText().toString().trim();
        String reason = reasonInput.getText().toString().trim();
        String timestamp = timestampText.getText().toString();

        Map<String, Object> moodData = new HashMap<>();
        moodData.put("mood", mood);
        moodData.put("trigger", trigger);
        moodData.put("reason", reason);
        moodData.put("timestamp", timestamp);

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

        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri, moodData);
        } else {
            addMoodToFirestore(moodData);
        }
    }

    // --- UPLOAD IMAGE ---
    private void uploadImageToFirebase(Uri imageUri, Map<String, Object> moodData) {
        try {
            AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(imageUri, "r");
            long fileSize = afd.getLength();
            afd.close();
            if (fileSize > 65536) {
                showSnackbar("Image exceeds maximum allowed size of 64KB");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            showSnackbar("Error reading image file");
            return;
        }
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            StorageReference imageRef = FirebaseStorage.getInstance()
                    .getReference("mood_images/" + user.getUid() + "/" + timeStamp + ".jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        moodData.put("imageUrl", uri.toString());
                        addMoodToFirestore(moodData);
                    }))
                    .addOnFailureListener(e -> {
                        Log.e("Storage", "Error uploading image", e);
                        addMoodToFirestore(moodData);
                    });
        }
    }

    // --- ADD MOOD DATA TO FIRESTORE ---
    private void addMoodToFirestore(Map<String, Object> moodData) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid()).collection("moods")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    // Clear UI on success.
                    triggerInput.setText("");
                    reasonInput.setText("");
                    selectedImageView.setVisibility(View.GONE);
                    selectedImageUri = null;
                    importImageIcon.clearColorFilter();
                    locationIcon.clearColorFilter();
                    selectedLocation = null;
                    taggedUsers.clear();
                    updateTaggedUsersUI();
                    tagIcon.clearColorFilter();
                    showSnackbar("Mood added successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding mood", e);
                    showSnackbar("Error adding mood");
                });
    }

    // --- TAGGING FUNCTIONALITY ---
    private void showTagUsersDialog() {
        final ArrayList<User> tempTaggedUsers = new ArrayList<>(taggedUsers);
        final List<User> currentData = new ArrayList<>();
        currentData.addAll(tempTaggedUsers);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tag_users_rounded, null);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        final EditText searchEditText = dialogView.findViewById(R.id.tag_search_edit_text);
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
                } else {
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
            updateTaggedUsersUI();
            if (!taggedUsers.isEmpty()) {
                tagIcon.setColorFilter(Color.BLUE);
            } else {
                tagIcon.clearColorFilter();
            }
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateTaggedUsersUI() {
        taggedUsersContainer.removeAllViews();
        if (taggedUsers.isEmpty()) {
            taggedUsersScrollView.setVisibility(View.GONE);
            return;
        }
        taggedUsersScrollView.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (User user : taggedUsers) {
            View chip = inflater.inflate(R.layout.item_tag_chip, taggedUsersContainer, false);
            TextView chipText = chip.findViewById(R.id.chip_text);
            chipText.setText(user.getName());
            chip.setOnClickListener(v -> {
                taggedUsers.remove(user);
                updateTaggedUsersUI();
                if (taggedUsers.isEmpty()) {
                    tagIcon.clearColorFilter();
                }
            });
            taggedUsersContainer.addView(chip);
        }
    }

    private class TagUsersAdapter extends RecyclerView.Adapter<TagUsersAdapter.ViewHolder> {
        private List<User> users;
        private List<User> selectedUsers;

        TagUsersAdapter(List<User> users, List<User> selectedUsers) {
            this.users = users;
            this.selectedUsers = selectedUsers;
        }

        @Override
        public int getItemCount() { return users.size(); }

        @Override
        public TagUsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TagUsersAdapter.ViewHolder holder, int position) {
            User user = users.get(position);
            holder.nameText.setText(user.getName());
            holder.usernameText.setText(user.getUsername());
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

        private boolean isUserSelected(User user) {
            for (User u : selectedUsers) {
                if (u.getId().equals(user.getId())) {
                    return true;
                }
            }
            return false;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView profileImage;
            TextView nameText, usernameText;
            ViewHolder(View itemView) {
                super(itemView);
                profileImage = itemView.findViewById(R.id.profile_image);
                nameText = itemView.findViewById(R.id.text_display_name);
                usernameText = itemView.findViewById(R.id.text_username);
            }
        }
    }
}
