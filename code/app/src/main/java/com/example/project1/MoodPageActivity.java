package com.example.project1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodPageActivity extends ActivityBase {

    private Mood selectedMood;
    private String moodId;
    private FirebaseFirestore db;
    private MoodAdapter moodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_mood_page, contentFrame, true);

        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView = findViewById(R.id.mood_detail_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton editMoodFab = findViewById(R.id.edit_mood_fab);

        selectedMood = (Mood) getIntent().getSerializableExtra("SELECTED_MOOD");
        moodId = getIntent().getStringExtra("MOOD_ID");

        if (selectedMood != null && moodId != null) {
            List<Mood> moodList = new ArrayList<>();
            moodList.add(selectedMood);
            moodAdapter = new MoodAdapter(moodList);
            recyclerView.setAdapter(moodAdapter);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && selectedMood.getUserId().equals(user.getEmail())) {
                editMoodFab.setVisibility(View.VISIBLE);
                editMoodFab.setOnClickListener(v -> showEditDialog(selectedMood, moodId));
            } else {
                editMoodFab.setVisibility(View.GONE);
            }
        } else {
            Log.e("MoodPageActivity", "Missing mood or moodId, finishing activity");
            finish();
        }
    }

    private void showEditDialog(Mood mood, String moodId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_mood, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Spinner moodSpinner = dialogView.findViewById(R.id.moodSpinner);
        EditText triggerInput = dialogView.findViewById(R.id.triggerInput);
        EditText reasonInput = dialogView.findViewById(R.id.reasonInput);
        AutoCompleteTextView friendSearchInput = dialogView.findViewById(R.id.friendSearchInput);
        Button addTagButton = dialogView.findViewById(R.id.addTagButton);
        TextView taggedFriendsText = dialogView.findViewById(R.id.taggedFriendsText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteButton);

        // Mood Spinner
        String[] moodOptions = {"Happy", "Sad", "Angry", "Scared", "Confused", "Disgusted", "Surprised", "Shameful"};
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moodOptions);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(moodAdapter);
        int moodPosition = Arrays.asList(moodOptions).indexOf(mood.getMoodStatus().replace("Feeling ", ""));
        if (moodPosition >= 0) moodSpinner.setSelection(moodPosition);

        // Populate fields
        triggerInput.setText(mood.getMoodTrigger() != null ? mood.getMoodTrigger() : "");
        reasonInput.setText(mood.getMoodReason() != null ? mood.getMoodReason() : "");

        // Tagging setup (assumed from AddMoodActivity)
        List<String> taggedFriends = new ArrayList<>(mood.getTaggedUserNames() != null ? mood.getTaggedUserNames() : new ArrayList<>());
        updateTaggedFriendsText(taggedFriendsText, taggedFriends);

        List<String> allUsers = new ArrayList<>();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userName = doc.getString("name"); // Assuming "name" field; adjust if AddMoodActivity uses "username" or "email"
                        if (userName != null && !userName.isEmpty()) {
                            allUsers.add(userName);
                        }
                    }
                    if (allUsers.isEmpty()) {
                        allUsers.add("No users found");
                        addTagButton.setEnabled(false);
                    }
                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allUsers);
                    friendSearchInput.setAdapter(userAdapter);
                    friendSearchInput.setThreshold(1); // Show suggestions after 1 character
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodPageActivity", "Failed to load users", e);
                    allUsers.add("Error loading users");
                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allUsers);
                    friendSearchInput.setAdapter(userAdapter);
                    addTagButton.setEnabled(false);
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                });

        addTagButton.setOnClickListener(v -> {
            String selectedUser = friendSearchInput.getText().toString().trim();
            if (selectedUser.isEmpty()) {
                Toast.makeText(this, "Please enter or select a user", Toast.LENGTH_SHORT).show();
            } else if (taggedFriends.contains(selectedUser)) {
                Toast.makeText(this, "User already tagged", Toast.LENGTH_SHORT).show();
            } else if (!allUsers.contains(selectedUser) || selectedUser.equals("No users found") || selectedUser.equals("Error loading users")) {
                Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            } else {
                taggedFriends.add(selectedUser);
                updateTaggedFriendsText(taggedFriendsText, taggedFriends);
                friendSearchInput.setText(""); // Clear input after adding
            }
        });

        // Save button
        saveButton.setOnClickListener(v -> {
            String newMoodStatus = "Feeling " + moodSpinner.getSelectedItem().toString();
            String newTrigger = triggerInput.getText().toString().trim();
            String newReason = reasonInput.getText().toString().trim();

            if (newReason.length() > 20) {
                Toast.makeText(this, "Reason must be 20 characters or less", Toast.LENGTH_SHORT).show();
                return;
            }

            mood.setMoodStatus(newMoodStatus);
            mood.setMoodTrigger(newTrigger);
            mood.setMoodReason(newReason);
            mood.setTaggedUserNames(taggedFriends);

            updateMoodInFirestore(mood, moodId);
            moodAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Delete button
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Deletion")
                    .setMessage("Are you sure you want to delete this mood?")
                    .setPositiveButton("Yes", (d, which) -> {
                        deleteMoodFromFirestore(moodId);
                        dialog.dismiss();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        dialog.show();
    }

    private void updateTaggedFriendsText(TextView textView, List<String> taggedFriends) {
        if (taggedFriends.isEmpty()) {
            textView.setText("No friends tagged");
        } else {
            textView.setText(String.join(", ", taggedFriends));
        }
    }

    private void updateMoodInFirestore(Mood mood, String moodId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("mood", mood.getMoodStatus().replace("Feeling ", ""));
        updatedData.put("trigger", mood.getMoodTrigger());
        updatedData.put("reason", mood.getMoodReason());

        List<Map<String, String>> tags = new ArrayList<>();
        for (String friend : mood.getTaggedUserNames()) {
            Map<String, String> tag = new HashMap<>();
            tag.put("name", friend);
            tags.add(tag);
        }
        updatedData.put("tags", tags);

        db.collection("users").document(user.getUid()).collection("moods").document(moodId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Mood updated successfully for moodId: " + moodId);
                    Toast.makeText(this, "Mood updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating mood", e);
                    Toast.makeText(this, "Failed to update mood", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteMoodFromFirestore(String moodId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).collection("moods").document(moodId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Mood deleted successfully for moodId: " + moodId);
                    Toast.makeText(this, "Mood deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting mood", e);
                    Toast.makeText(this, "Failed to delete mood", Toast.LENGTH_SHORT).show();
                });
    }
}