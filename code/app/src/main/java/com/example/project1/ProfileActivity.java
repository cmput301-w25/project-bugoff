// ProfileActivity.java
package com.example.project1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ProfileActivity extends ActivityBase {

    private ImageView profileImage, homeButton;
    private TextView profileName, profileEmail, profileBio;
    private Button editProfileButton, logoutButton;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList;
    private ActivityResultLauncher<Intent> selectImageLauncher;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        profileImage.setImageURI(imageUri);
                        uploadImageToFirebase(imageUri);
                    }
                }
        );

        // Include Profile Content inside the base layout
        getLayoutInflater().inflate(R.layout.profile_page, findViewById(R.id.content_frame), true);

        // Initialize UI elements
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileBio = findViewById(R.id.profile_bio);
        profileEmail = findViewById(R.id.profile_email);
        editProfileButton = findViewById(R.id.edit_profile_btn);
        logoutButton = findViewById(R.id.logout_btn);
        logoutButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                Toast.makeText(ProfileActivity.this, "Cannot log out while offline", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProfileActivity.this, SignInActivity.class));
            finish();
        });


        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.moods_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        moodList = new ArrayList<>();
        moodAdapter = new MoodAdapter(moodList);
        recyclerView.setAdapter(moodAdapter);

        homeButton = findViewById(R.id.home);

        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(this, HomePageActivity.class));
        });

        // Load user details
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            profileName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User Name");
            profileEmail.setText(user.getEmail());
            profileImage.setImageResource(R.drawable.ic_profile);

            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (error != null) {
                            Log.e("Firestore", "Error fetching profile updates", error);
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            // Update profile picture
                            String profilePicUrl = documentSnapshot.getString("profilePictureUrl");
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                Glide.with(this).load(profilePicUrl).into(profileImage);
                            }

                            // Update bio
                            String bio = documentSnapshot.getString("bio");
                            if (bio == null || bio.isEmpty()) {
                                bio = "Every emotion tells a story—write yours here. 📜💫";
                            }
                            profileBio.setText(bio);

                            // Update name (if stored in Firestore)
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                profileName.setText(name);
                            }
                        }
                    });
            loadMoods();
        }

        // Edit Profile Action
        editProfileButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                Toast.makeText(ProfileActivity.this, "Cannot edit profile while offline", Toast.LENGTH_SHORT).show();
                return;
            }
            showEditProfileDialog();
        });
    }

    private void loadMoods() {
        // Adding two sample Mood objects to the moodList
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", R.drawable.angry_photo));
        moodList.add(new Mood("Sample User", "User_id", "Edmonton, Canada", "4:39 AM, 2025-02-11", "Alone", "Feeling Angry", "Hunger", "Couldn't Find Food!", null));

        // Notify the adapter that the data has changed
        moodAdapter.notifyDataSetChanged();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        ImageView profilePic = view.findViewById(R.id.edit_profile_image);
        EditText editName = view.findViewById(R.id.edit_name);
        EditText editBio = view.findViewById(R.id.edit_bio);
        Spinner genderSpinner = view.findViewById(R.id.gender_spinner);
        Button btnResetPassword = view.findViewById(R.id.reset_password_btn);
        Button btnSave = view.findViewById(R.id.save_changes_btn);
        Button btnCancel = view.findViewById(R.id.cancel_btn);

        // Load user details
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            editName.setText(user.getDisplayName());
            FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String bio = documentSnapshot.getString("bio");
                            if (bio == null || bio.isEmpty()) {
                                bio = "Every emotion tells a story—write yours here. 📜💫";
                            }
                            editBio.setText(bio);
                        }
                    });
            Glide.with(this).load(user.getPhotoUrl()).into(profilePic);
        }

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

            // Save to Firestore
            saveUserProfile(newName, newBio, selectedGender);

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectImageLauncher.launch(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference("profile_pictures/" + user.getUid());

            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update UI instantly
                        Glide.with(this).load(uri).into(profileImage);

                        // Update Firebase Authentication profile
                        user.updateProfile(new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build());

                        // Store the download URL in Firestore
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUid())
                                .update("profilePictureUrl", uri.toString())
                                .addOnSuccessListener(aVoid ->
                                        Log.d("Firestore", "Profile picture updated successfully")
                                )
                                .addOnFailureListener(e ->
                                        Log.e("Firestore", "Error updating profile picture", e)
                                );
                    }))
                    .addOnFailureListener(e ->
                            Log.e("Storage", "Error uploading image", e)
                    );
        }
    }


    private void saveUserProfile(String name, String bio, String gender) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("name", name);
            userProfile.put("bio", bio);
            userProfile.put("gender", gender);

            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .set(userProfile, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();

                        // Update UI instantly
                        profileName.setText(name);
                        profileBio.setText(bio);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show()
                    );
        }
    }
}