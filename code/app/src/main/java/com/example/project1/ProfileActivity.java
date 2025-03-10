package com.example.project1;

import android.content.Context;
import android.content.Intent;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.text.ParseException;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends ActivityBase {

    private ImageView profileImage;
    private TextView profileName, profileEmail, profileBio;
    private Button editProfileButton;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private MoodAdapter moodAdapter;
    private List<Mood> moodList;
    private List<String> moodDocIds;
    private ActivityResultLauncher<Intent> selectImageLauncher;
    private AlertDialog editProfileDialog;
    private int selectedDaysFilter = 0;
    private String selectedMoodFilter = "Select Mood";
    private String searchQuery = "";
    private TextView followersCount;
    private TextView followingCount;
    public TextView moodCountText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeNavigation();
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

        getLayoutInflater().inflate(R.layout.profile_page, findViewById(R.id.content_frame), true);

        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileBio = findViewById(R.id.profile_bio);
        profileEmail = findViewById(R.id.profile_email);
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

        ImageButton filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(v -> showFilterPopup());

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
                            String profilePicUrl = documentSnapshot.getString("profilePictureUrl");
                            if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                Glide.with(this).load(profilePicUrl).into(profileImage);
                            }
                            String bio = documentSnapshot.getString("bio");
                            if (bio == null || bio.isEmpty()) {
                                bio = "Every emotion tells a story—write yours here. 📜💫";
                            }
                            profileBio.setText(bio);
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                profileName.setText(name);
                            }
                        }
                    });
            loadMoods();
        }

        followersCount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
            intent.putExtra("type", "followers");
            intent.putExtra("userId", mAuth.getCurrentUser().getUid());
            startActivity(intent);
        });

        followingCount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowingActivity.class);
            intent.putExtra("type", "following");
            intent.putExtra("userId", mAuth.getCurrentUser().getUid());
            startActivity(intent);
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

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

        editProfileButton.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                Toast.makeText(ProfileActivity.this, "Cannot edit profile while offline", Toast.LENGTH_SHORT).show();
                return;
            }
            showEditProfileDialog();
        });

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && e.getAction() == MotionEvent.ACTION_UP) {
                    int position = rv.getChildAdapterPosition(child);
                    if (position != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(ProfileActivity.this, MoodPageActivity.class);
                        intent.putExtra("SELECTED_MOOD", moodList.get(position));
                        intent.putExtra("MOOD_ID", moodDocIds.get(position));
                        startActivity(intent);
                    }
                }
                return false;
            }
        });
    }

    private void showFilterPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = getLayoutInflater().inflate(R.layout.filter_popup, null);
        builder.setView(popupView);

        AlertDialog filterDialog = builder.create();
        filterDialog.show();

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

        resetButton.setOnClickListener(v -> {
            filterWeek.setChecked(false);
            filterMonth.setChecked(false);
            moodSpinner.setSelection(0);
            searchBox.setText("");

            selectedDaysFilter = 0;
            selectedMoodFilter = "Select Mood";
            searchQuery = "";

            loadMoods();
            filterDialog.dismiss();
            Toast.makeText(ProfileActivity.this, "Filters reset", Toast.LENGTH_SHORT).show();
        });

        applyButton.setOnClickListener(v -> {
            String selectedMood = moodSpinner.getSelectedItem().toString();
            boolean applyMoodFilter = !selectedMood.equals("Select Mood");

            selectedDaysFilter = filterWeek.isChecked() ? 7 : filterMonth.isChecked() ? 30 : 0;
            selectedMoodFilter = selectedMood;
            searchQuery = searchBox.getText().toString().trim();

            loadMoodsFiltered(selectedDaysFilter, applyMoodFilter ? selectedMood : null, searchQuery.isEmpty() ? null : searchQuery);
            filterDialog.dismiss();
        });
    }

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
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                                            String mood = document.getString("mood");
                                            String locationName = document.getString("location");
                                            String timestampStr = document.getString("timestamp");
                                            String trigger = document.getString("trigger");
                                            String reason = document.getString("reason");
                                            String imageUrl = document.getString("imageUrl");

                                            // Fetch tagged users
                                            List<Map<String, Object>> tags = (List<Map<String, Object>>) document.get("tags");
                                            List<String> taggedUserNames = new ArrayList<>();
                                            if (tags != null) {
                                                for (Map<String, Object> tag : tags) {
                                                    String username = (String) tag.get("name");
                                                    if (username != null) {
                                                        taggedUserNames.add(username);
                                                    }
                                                }
                                            }

                                            // Calculate gathering status
                                            String gatheringStatus;
                                            if (tags == null || tags.isEmpty()) {
                                                gatheringStatus = "Alone";
                                            } else {
                                                int tagCount = tags.size();
                                                if (tagCount == 1) {
                                                    gatheringStatus = "With 1 other";
                                                } else if (tagCount <= 5) {
                                                    gatheringStatus = "With " + tagCount + " others";
                                                } else {
                                                    gatheringStatus = "With a crowd";
                                                }
                                            }

                                            Mood moodObj = new Mood(
                                                    user.getDisplayName(),
                                                    user.getEmail(), // Use email instead of UID
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
                                            moodList.add(moodObj);
                                            moodDocIds.add(document.getId());

                                        }
                                        // Ensure the list is sorted from latest to earliest
                                        Collections.sort(moodList, (m1, m2) ->
                                                Long.compare(convertTimestampToMillis(m2.getTimestamp()),
                                                        convertTimestampToMillis(m1.getTimestamp()))
                                        );
                                        moodAdapter.notifyDataSetChanged();
                                        updateMoodCount();
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "No moods found", Toast.LENGTH_SHORT).show();
                                        updateMoodCount();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ProfileActivity", "Error loading moods", e);
                                    Toast.makeText(ProfileActivity.this, "Error loading moods", Toast.LENGTH_SHORT).show();
                                    updateMoodCount();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "Error fetching user profile", e);
                        Toast.makeText(ProfileActivity.this, "Error loading user profile", Toast.LENGTH_SHORT).show();
                    });
        }
    }


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

                        final long cutoffTimestamp;
                        if (days > 0) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DAY_OF_YEAR, -days);
                            cutoffTimestamp = calendar.getTimeInMillis();
                        } else {
                            cutoffTimestamp = 0;
                        }

                        db.collection("users").document(userId).collection("moods")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                                            String mood = document.getString("mood");
                                            String reason = document.getString("reason");
                                            String timestampStr = document.getString("timestamp");
                                            String trigger = document.getString("trigger");
                                            String imageUrl = document.getString("imageUrl");
                                            String locationName = document.getString("location");

                                            List<Map<String, Object>> tags = (List<Map<String, Object>>) document.get("tags");
                                            List<String> taggedUserNames = new ArrayList<>();
                                            if (tags != null) {
                                                for (Map<String, Object> tag : tags) {
                                                    String username = (String) tag.get("name");
                                                    if (username != null) {
                                                        taggedUserNames.add(username);
                                                    }
                                                }
                                            }

                                            String gatheringStatus;
                                            int tagCount = taggedUserNames.size();
                                            if (tagCount == 0) {
                                                gatheringStatus = "Alone";
                                            } else if (tagCount == 1) {
                                                gatheringStatus = "With 1 other";
                                            } else if (tagCount <= 5) {
                                                gatheringStatus = "With " + tagCount + " others";
                                            } else {
                                                gatheringStatus = "With a crowd";
                                            }

                                            long moodTimestamp = convertTimestampToMillis(timestampStr);

                                            boolean withinTimeRange = (days == 0) || (moodTimestamp >= cutoffTimestamp);
                                            boolean matchesMood = (moodFilter == null) || (mood.equalsIgnoreCase(moodFilter));
                                            boolean matchesSearch = (searchFilter == null) ||
                                                    (reason != null && reason.toLowerCase().contains(searchFilter.toLowerCase()));

                                            if (withinTimeRange && matchesMood && matchesSearch) {

                                                Mood moodObj = new Mood(
                                                        user.getDisplayName(),
                                                        user.getEmail(), // Use email instead of UID
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
                                                moodList.add(moodObj);
                                                moodDocIds.add(document.getId());
                                            }
                                        }
                                        // Sort moods by date (latest first)
                                        Collections.sort(moodList, (m1, m2) ->
                                                Long.compare(convertTimestampToMillis(m2.getTimestamp()),
                                                        convertTimestampToMillis(m1.getTimestamp()))
                                        );
                                        moodAdapter.notifyDataSetChanged();
                                        updateMoodCount();
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "No moods found for the selected filter", Toast.LENGTH_SHORT).show();
                                        updateMoodCount();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ProfileActivity", "Error getting moods", e);
                                    Toast.makeText(ProfileActivity.this, "Error loading moods", Toast.LENGTH_SHORT).show();
                                    updateMoodCount();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileActivity", "Error fetching user profile", e);
                        Toast.makeText(ProfileActivity.this, "Error loading user profile", Toast.LENGTH_SHORT).show();
                    });
        }
    }

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

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);
        editProfileDialog = builder.create();

        ImageView profilePic = view.findViewById(R.id.edit_profile_image);
        EditText editName = view.findViewById(R.id.edit_name);
        EditText editBio = view.findViewById(R.id.edit_bio);
        Spinner genderSpinner = view.findViewById(R.id.gender_spinner);
        Button btnResetPassword = view.findViewById(R.id.reset_password_btn);
        Button btnSave = view.findViewById(R.id.save_changes_btn);
        Button btnCancel = view.findViewById(R.id.cancel_btn);

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
                        String profilePicUrl = documentSnapshot.getString("profilePictureUrl");
                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            Glide.with(this).load(profilePicUrl).into(profilePic);
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

            saveUserProfile(newName, newBio, selectedGender);
            editProfileDialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> editProfileDialog.dismiss());

        editProfileDialog.show();
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
                        Glide.with(this).load(uri).into(profileImage);
                        user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build());
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
                    .addOnFailureListener(e -> Log.e("Storage", "Error uploading image", e));
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
                        profileName.setText(name);
                        profileBio.setText(bio);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show());
        }
    }

    private void updateMoodCount() {
        moodCountText.setText("" + moodList.size());
    }
}