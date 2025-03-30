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

package com.example.whimsy;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private String ownerUid;
    private FirebaseFirestore db;
    private MoodAdapter moodAdapter;
    private List<Comment> comments = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private ListenerRegistration commentsListener;
    private EditText commentInput;
    private LinearLayout commentLayout;
    private Button commentConfirmButton;
    private FloatingActionButton editMoodFab;

    /**
     * Initializes the activity, sets up the RecyclerView, and fetches mood data.
     *
     * @param savedInstanceState Saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_mood_page, contentFrame, true);

        FloatingActionButton editMoodFab = findViewById(R.id.edit_mood_fab);

        // Retrieve mood data from intent
        selectedMood = (Mood) getIntent().getSerializableExtra("SELECTED_MOOD");
        moodId = getIntent().getStringExtra("MOOD_ID");
        ownerUid = getIntent().getStringExtra("OWNER_UID");

        int colorBg;
        int colorFg;
        switch (selectedMood.getMoodStatus().toLowerCase()) {
            case "feeling happy":
                colorBg = getColor(R.color.happy_background);
                colorFg = getColor(R.color.happy_text);
                break;
            case "feeling sad":
                colorBg = getColor(R.color.sad_background);
                colorFg = getColor(R.color.sad_text);
                break;
            case "feeling angry":
                colorBg = getColor(R.color.anger_background);
                colorFg = getColor(R.color.anger_text);
                break;
            case "feeling scared":
                colorBg = getColor(R.color.scared_background);
                colorFg = getColor(R.color.scared_text);
                break;
            case "feeling confused":
                colorBg = getColor(R.color.confused_background);
                colorFg = getColor(R.color.confused_text);
                break;
            case "feeling disgusted":
                colorBg = getColor(R.color.disgust_background);
                colorFg = getColor(R.color.disgust_text);
                break;
            case "feeling excited":
                colorBg = getColor(R.color.excited_background);
                colorFg = getColor(R.color.excited_text);
                break;
            case "feeling ashamed":
                colorBg = getColor(R.color.ashamed_background);
                colorFg = getColor(R.color.ashamed_text);
                break;
            default:
                colorBg = getColor(R.color.white);
                colorFg = getColor(R.color.black);
                break;
        }
        editMoodFab.setBackgroundTintList(ColorStateList.valueOf(colorBg));
        editMoodFab.setImageTintList(ColorStateList.valueOf(colorFg));

        RecyclerView moodRecyclerView = findViewById(R.id.mood_detail_recycler_view);
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Set up comments RecyclerView
        RecyclerView commentsRecyclerView = findViewById(R.id.mood_comments_recycler_view);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(comments, colorFg);
        commentsRecyclerView.setAdapter(commentAdapter);


        commentInput = findViewById(R.id.comment_input);
        commentConfirmButton = findViewById(R.id.comment_confirm_button);
        commentLayout = findViewById(R.id.comment_layout);
        commentLayout.setVisibility(View.GONE); // Initially hidden
        commentInput.setBackgroundTintList(ColorStateList.valueOf(colorBg));
        commentInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        commentInput.setTextColor(colorFg);
        commentConfirmButton.setBackgroundTintList(ColorStateList.valueOf(colorBg));
        commentConfirmButton.setTextColor(colorFg);
        commentConfirmButton.setOnClickListener(v -> postComment());




        fetchComments();

        moodRecyclerView.post(() -> {
            if (moodRecyclerView.getChildCount() > 0) {
                View itemView = moodRecyclerView.getChildAt(0); // Get the mood item view
                Button commentButton = itemView.findViewById(R.id.comment_button);
                if (commentButton != null) {
                    commentButton.setOnClickListener(v -> {
                        // Toggle visibility of comment input area
                        if (commentLayout.getVisibility() == View.VISIBLE) {
                            commentLayout.setVisibility(View.GONE); // Hide if visible
                        } else {
                            commentLayout.setVisibility(View.VISIBLE); // Show if hidden
                            commentInput.requestFocus(); // Focus on EditText for typing
                        }
                    });
                }
            }
        });

        if (selectedMood != null && moodId != null) {
            List<Mood> moodList = new ArrayList<>();
            moodList.add(selectedMood);
            moodAdapter = new MoodAdapter(moodList);
            moodRecyclerView.setAdapter(moodAdapter);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                // Fetch the username of the current user from Firestore
                db.collection("users")
                        .document(user.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String username = documentSnapshot.getString("username");
                            // Compare the selectedMood's userId (assumed to be the username) with the fetched username
                            if (selectedMood.getUserId().equals(username)) {
                                editMoodFab.setVisibility(View.VISIBLE);
                                editMoodFab.setOnClickListener(v -> showEditDialog(selectedMood, moodId));
                            } else {
                                editMoodFab.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("TAG", "Error fetching username", e);
                            editMoodFab.setVisibility(View.GONE);
                        });
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
        EditText reasonInput = dialogView.findViewById(R.id.reasonInput);
        TextView reasonCharCountText = dialogView.findViewById(R.id.reasonCharCountText);
        AutoCompleteTextView friendSearchInput = dialogView.findViewById(R.id.friendSearchInput);
        Button addTagButton = dialogView.findViewById(R.id.addTagButton);
        TextView taggedFriendsText = dialogView.findViewById(R.id.taggedFriendsText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteButton);

        // Setup mood spinner
        String[] moodOptions = {"Happy", "Sad", "Angry", "Scared", "Confused", "Disgusted", "Excited", "Ashamed"};
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moodOptions);
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(moodAdapter);

        // Pre-select current mood
        String currentMood = mood.getMoodStatus().replace("Feeling ", ""); // Assuming mood status is stored as "Feeling Happy"
        int moodIndex = Arrays.asList(moodOptions).indexOf(currentMood);
        if (moodIndex >= 0) {
            moodSpinner.setSelection(moodIndex);
        }

        // Populate reason field and setup character counter
        reasonInput.setText(mood.getMoodReason());
        reasonInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int remaining = 200 - s.length();
                reasonCharCountText.setText(String.valueOf(remaining));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle tagging friends
        List<String> taggedFriends = new ArrayList<>(mood.getTaggedUserNames());
        updateTaggedFriendsText(taggedFriendsText, taggedFriends);

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
            mood.setMoodReason(reasonInput.getText().toString().trim());
            mood.setTaggedUserNames(taggedFriends);
            updateMoodInFirestore(mood, moodId); // Your existing method to save to Firestore
            dialog.dismiss();
        });

        // Delete mood with confirmation
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Mood")
                    .setMessage("Are you sure you want to delete this mood?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        deleteMoodFromFirestore(moodId); // Your existing method to delete from Firestore
                        dialog.dismiss();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Cancel button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchComments() {
        commentsListener = db.collection("users").document(ownerUid)
                .collection("moods").document(moodId).collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("MoodPageActivity", "Listen failed", e);
                        Toast.makeText(this, "Error loading comments", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    comments.clear();
                    if (snapshots != null) {
                        for (var doc : snapshots.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            comments.add(comment);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void postComment() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String commentText = commentInput.getText().toString().trim();

        if (user == null) {
            Toast.makeText(this, "Please log in to comment", Toast.LENGTH_SHORT).show();
            return;
        }
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");
                    String profileImageUrl = documentSnapshot.getString("profilePictureUrl"); // Assuming this field exists in Firestore
                    if (username == null) username = "Anonymous";
                    Comment comment = new Comment(
                            user.getUid(),
                            username,
                            commentText,
                            Timestamp.now(),
                            profileImageUrl
                    );

                    db.collection("users").document(ownerUid)
                            .collection("moods").document(moodId).collection("comments")
                            .add(comment)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("MoodPageActivity", "Comment added: " + documentReference.getId());
                                commentInput.setText("");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MoodPageActivity", "Error adding comment", e);
                                Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodPageActivity", "Error fetching user data", e);
                    Toast.makeText(this, "Error retrieving user info", Toast.LENGTH_SHORT).show();
                });
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
     * Deletes a mood entry from Firestore and removes the associated image from Firebase Storage (if any).
     *
     * @param moodId The unique identifier of the mood entry.
     */
    private void deleteMoodFromFirestore(String moodId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // If the mood has an associated image, delete it from Firebase Storage.
        // Assuming selectedMood is the Mood object being displayed.
        String imageUrl = selectedMood.getMoodImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Storage", "Mood image deleted successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Storage", "Error deleting mood image", e);
                    });
        }

        // Delete the mood document from Firestore.
        db.collection("users").document(user.getUid()).collection("moods").document(moodId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Mood document deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting mood document", e);
                });
    }

}