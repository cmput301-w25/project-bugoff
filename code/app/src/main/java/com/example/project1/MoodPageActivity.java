/**
 * MoodPageActivity serves as the activity for viewing and managing a specific mood entry,
 * allowing users to see detailed information, edit the mood, tag friends, and delete the entry.
 *
 * This class initializes the mood detail view, sets up editing capabilities via a dialog,
 * and interacts with Firebase Firestore for data persistence and retrieval.
 *
 * Outstanding Issues:
 * - Does not validate friend tags against an actual user database.
 * - Lacks confirmation prompt before deleting a mood entry.
 *
 */

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

/**
 * Activity for displaying and managing a selected mood entry.
 * Allows users to view mood details, edit the mood, tag friends, and delete the entry.
 */
public class MoodPageActivity extends ActivityBase {

    private Mood selectedMood;
    private String moodId;
    private FirebaseFirestore db;
    private MoodAdapter moodAdapter;

    /**
     * Initializes the activity, sets up the RecyclerView, and fetches mood data.
     *
     * @param savedInstanceState Saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_mood_page, contentFrame, true);

        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerView = findViewById(R.id.mood_detail_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton editMoodFab = findViewById(R.id.edit_mood_fab);

        // Retrieve mood data from intent
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

    /**
     * Displays an edit dialog for modifying mood details.
     *
     * @param mood   The mood entry to be edited.
     * @param moodId The unique identifier of the mood entry.
     */
    private void showEditDialog(Mood mood, String moodId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_mood, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Initialize UI components
        Spinner moodSpinner = dialogView.findViewById(R.id.moodSpinner);
        EditText triggerInput = dialogView.findViewById(R.id.triggerInput);
        EditText reasonInput = dialogView.findViewById(R.id.reasonInput);
        AutoCompleteTextView friendSearchInput = dialogView.findViewById(R.id.friendSearchInput);
        Button addTagButton = dialogView.findViewById(R.id.addTagButton);
        TextView taggedFriendsText = dialogView.findViewById(R.id.taggedFriendsText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteButton);

        // Setup mood spinner
        String[] moodOptions = {"Happy", "Sad", "Angry", "Scared", "Confused", "Disgusted", "Surprised", "Shameful"};
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moodOptions);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(moodAdapter);

        // Populate fields with existing mood data
        triggerInput.setText(mood.getMoodTrigger());
        reasonInput.setText(mood.getMoodReason());
        List<String> taggedFriends = new ArrayList<>(mood.getTaggedUserNames());
        updateTaggedFriendsText(taggedFriendsText, taggedFriends);

        // Handle tagging friends
        addTagButton.setOnClickListener(v -> {
            String selectedUser = friendSearchInput.getText().toString().trim();
            if (!selectedUser.isEmpty() && !taggedFriends.contains(selectedUser)) {
                taggedFriends.add(selectedUser);
                updateTaggedFriendsText(taggedFriendsText, taggedFriends);
                friendSearchInput.setText("");
            }
        });

        // Save mood updates
        saveButton.setOnClickListener(v -> {
            mood.setMoodStatus("Feeling " + moodSpinner.getSelectedItem().toString());
            mood.setMoodTrigger(triggerInput.getText().toString().trim());
            mood.setMoodReason(reasonInput.getText().toString().trim());
            mood.setTaggedUserNames(taggedFriends);
            updateMoodInFirestore(mood, moodId);
            moodAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        // Delete mood entry
        deleteButton.setOnClickListener(v -> {
            deleteMoodFromFirestore(moodId);
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    /**
     * Updates the tagged friends text view.
     *
     * @param textView       The TextView displaying tagged friends.
     * @param taggedFriends  List of tagged friends.
     */
    private void updateTaggedFriendsText(TextView textView, List<String> taggedFriends) {
        textView.setText(taggedFriends.isEmpty() ? "No friends tagged" : String.join(", ", taggedFriends));
    }

    /**
     * Updates the mood entry in Firestore.
     *
     * @param mood   The mood entry to update.
     * @param moodId The unique identifier of the mood entry.
     */
    private void updateMoodInFirestore(Mood mood, String moodId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("mood", mood.getMoodStatus().replace("Feeling ", ""));
        updatedData.put("trigger", mood.getMoodTrigger());
        updatedData.put("reason", mood.getMoodReason());
        db.collection("users").document(user.getUid()).collection("moods").document(moodId)
                .update(updatedData);
    }

    /**
     * Deletes a mood entry from Firestore.
     *
     * @param moodId The unique identifier of the mood entry.
     */
    private void deleteMoodFromFirestore(String moodId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid()).collection("moods").document(moodId).delete();
    }
}