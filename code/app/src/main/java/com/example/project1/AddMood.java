package com.example.project1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
    private ImageView locationIcon, importImageIcon, selectedImageView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Place selectedPlace;
    private Uri selectedImageUri;
    private String currentPhotoPath;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_PICK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.add_mood, findViewById(R.id.content_frame), true);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize Places API (replace "YOUR_API_KEY" with your Google API key)
        Places.initialize(getApplicationContext(), "YOUR_API_KEY");
        PlacesClient placesClient = Places.createClient(this);

        // Initialize UI elements
        moodSpinner = findViewById(R.id.moodSpinner);
        triggerInput = findViewById(R.id.triggerInput);
        reasonInput = findViewById(R.id.reasonInput);
        timestampText = findViewById(R.id.timestampText);
        addMoodButton = findViewById(R.id.addMoodButton);
        locationIcon = findViewById(R.id.locationIcon);
        importImageIcon = findViewById(R.id.importImageIcon);
        selectedImageView = findViewById(R.id.selectedImageView);

        // Set up Spinner with mood options
        String[] moods = {"Happy", "Sad", "Angry", "Scared", "Confused", "Disgusted", "Surprised", "Shameful"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(adapter);

        // Set current timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a - MMMM dd, yyyy", Locale.getDefault());
        timestampText.setText(sdf.format(new Date()));

        // Location icon click listener
        locationIcon.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        });

        // Image import icon click listener
        importImageIcon.setOnClickListener(v -> showImagePickerDialog());

        // Add Mood button click listener
        addMoodButton.setOnClickListener(v -> saveMoodToFirebase());
    }

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
                // Handle error (e.g., show toast)
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
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                selectedPlace = Autocomplete.getPlaceFromIntent(data);
                locationIcon.setColorFilter(Color.BLUE);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                // Handle error (e.g., show toast)
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            selectedImageUri = Uri.fromFile(new File(currentPhotoPath));
            displaySelectedImage(selectedImageUri);
            importImageIcon.setColorFilter(Color.BLUE);
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            displaySelectedImage(selectedImageUri);
            importImageIcon.setColorFilter(Color.BLUE);
        }
    }

    private void displaySelectedImage(Uri uri) {
        selectedImageView.setImageURI(uri);
        selectedImageView.setVisibility(View.VISIBLE);
    }

    private void saveMoodToFirebase() {
        String mood = moodSpinner.getSelectedItem().toString();
        String trigger = triggerInput.getText().toString().trim();
        String reason = reasonInput.getText().toString().trim();
        String timestamp = timestampText.getText().toString();
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> moodData = new HashMap<>();
        moodData.put("mood", mood);
        moodData.put("trigger", trigger);
        moodData.put("reason", reason);
        moodData.put("timestamp", timestamp);

        if (selectedPlace != null) {
            moodData.put("locationName", selectedPlace.getName());
            moodData.put("locationLat", selectedPlace.getLatLng().latitude);
            moodData.put("locationLng", selectedPlace.getLatLng().longitude);
        }

        if (selectedImageUri != null) {
            String imageName = "image_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child("images/" + userId + "/" + imageName);
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        moodData.put("imageUrl", uri.toString());
                        addMoodToFirestore(moodData);
                    }))
                    .addOnFailureListener(e -> {
                        // Handle failure (e.g., show toast)
                    });
        } else {
            addMoodToFirestore(moodData);
        }
    }

    private void addMoodToFirestore(Map<String, Object> moodData) {
        db.collection("users").document(auth.getCurrentUser().getUid()).collection("moods")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    triggerInput.setText("");
                    reasonInput.setText("");
                    selectedImageView.setVisibility(View.GONE);
                    selectedImageUri = null;
                    importImageIcon.clearColorFilter();
                    locationIcon.clearColorFilter();
                    selectedPlace = null;
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., show toast)
                });
    }
}