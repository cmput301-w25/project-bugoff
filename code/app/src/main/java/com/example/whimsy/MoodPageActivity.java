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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private ListenerRegistration moodListener; // Added field
    private EditText commentInput;
    private LinearLayout commentLayout;
    private Button commentConfirmButton;
    private FloatingActionButton editMoodFab;
    private ImageView backBtn;
    private Set<String> followedMoodsSet = new HashSet<>();

    private BroadcastReceiver connectivityReceiver;
    private boolean wasOffline = false;

    /**
     * Initializes the activity, sets up the RecyclerView, and fetches mood data.
     *
     * @param savedInstanceState Saved instance state bundle.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_mood_page, contentFrame, true);


        connectivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (!isConnected) {
                    wasOffline = true;
                    Toast.makeText(MoodPageActivity.this, "Offline: Changes will sync later", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Retrieve mood data from intent
        selectedMood = (Mood) getIntent().getSerializableExtra("SELECTED_MOOD");
        moodId = getIntent().getStringExtra("MOOD_ID");
        ownerUid = getIntent().getStringExtra("OWNER_UID");
        if (selectedMood != null) {
            selectedMood.setOwnerUid(ownerUid);
            selectedMood.setMoodId(moodId);
        }

        backBtn = findViewById(R.id.tool_back_button);
        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(v -> finish());

        int colorBg;
        int colorFg;
        int cardBg;
        switch (selectedMood.getMoodStatus().toLowerCase()) {
            case "feeling happy":
                colorBg = getColor(R.color.happy_background);
                colorFg = getColor(R.color.happy_text);
                cardBg = getColor(R.color.happy_card);
                break;
            case "feeling sad":
                colorBg = getColor(R.color.sad_background);
                colorFg = getColor(R.color.sad_text);
                cardBg = getColor(R.color.sad_card);
                break;
            case "feeling angry":
                colorBg = getColor(R.color.anger_background);
                colorFg = getColor(R.color.anger_text);
                cardBg = getColor(R.color.anger_card);
                break;
            case "feeling scared":
                colorBg = getColor(R.color.scared_background);
                colorFg = getColor(R.color.scared_text);
                cardBg = getColor(R.color.scared_card);
                break;
            case "feeling confused":
                colorBg = getColor(R.color.confused_background);
                colorFg = getColor(R.color.confused_text);
                cardBg = getColor(R.color.confused_card);
                break;
            case "feeling disgusted":
                colorBg = getColor(R.color.disgust_background);
                colorFg = getColor(R.color.disgust_text);
                cardBg = getColor(R.color.disgust_card);
                break;
            case "feeling excited":
                colorBg = getColor(R.color.excited_background);
                colorFg = getColor(R.color.excited_text);
                cardBg = getColor(R.color.excited_card);
                break;
            case "feeling ashamed":
                colorBg = getColor(R.color.ashamed_background);
                colorFg = getColor(R.color.ashamed_text);
                cardBg = getColor(R.color.ashamed_card);
                break;
            default:
                colorBg = getColor(R.color.white);
                colorFg = getColor(R.color.black);
                cardBg = getColor(R.color.white);
                break;
        }
        editMoodFab = findViewById(R.id.edit_mood_fab); // Initialize the FAB properly
        editMoodFab.setBackgroundTintList(ColorStateList.valueOf(colorBg));
        editMoodFab.setImageTintList(ColorStateList.valueOf(colorFg));
        RecyclerView moodRecyclerView = findViewById(R.id.mood_detail_recycler_view);
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(this));

// Set up comments RecyclerView
        RecyclerView commentsRecyclerView = findViewById(R.id.mood_comments_recycler_view);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(comments, colorFg, colorBg);
        commentsRecyclerView.setAdapter(commentAdapter);

// Initialize comment input components
        commentInput = findViewById(R.id.comment_input);
        commentInput.setBackgroundTintList(ColorStateList.valueOf(colorBg));
        commentInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(200)});
        commentInput.setTextColor(colorFg);

        commentConfirmButton = findViewById(R.id.comment_confirm_button);
        commentConfirmButton.setBackgroundTintList(ColorStateList.valueOf(colorBg));
        commentConfirmButton.setTextColor(colorFg);
        commentLayout = findViewById(R.id.comment_layout);
        commentLayout.setVisibility(View.GONE); // Initially hidden
        commentConfirmButton.setOnClickListener(v -> postComment());

        // Fetch comments
        fetchComments();

        // Set up the comment button listener
        moodRecyclerView.post(() -> {
            if (moodRecyclerView.getChildCount() > 0) {
                View itemView = moodRecyclerView.getChildAt(0);
                Button commentButton = itemView.findViewById(R.id.comment_button);
                if (commentButton != null) {
                    commentButton.setOnClickListener(v -> {
                        // Toggle comment layout visibility
                        commentLayout.setVisibility(
                                commentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                        );
                        if (commentLayout.getVisibility() == View.VISIBLE) {
                            commentInput.requestFocus();
                        }
                    });
                }
            }
        });

        FloatingActionButton editMoodFab = findViewById(R.id.edit_mood_fab);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (selectedMood != null && moodId != null) {
            selectedMood.setOwnerUid(ownerUid);
            selectedMood.setMoodId(moodId);
            List<Mood> moodList = new ArrayList<>();
            moodList.add(selectedMood);
            moodAdapter = new MoodAdapter(moodList);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                db.collection("users")
                        .document(user.getUid())
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            String username = documentSnapshot.getString("username");
                            if (selectedMood.getUserId().equals(username)) {
                                editMoodFab.setVisibility(View.VISIBLE);
                                editMoodFab.setOnClickListener(v -> showEditDialog(selectedMood, moodId));
                            } else {
                                editMoodFab.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(e -> {
                            editMoodFab.setVisibility(View.GONE);
                        });
            } else {
                editMoodFab.setVisibility(View.GONE);
            }

            if (currentUser != null) {
                String currentUserId = currentUser.getUid();
                db.collection("users")
                        .document(currentUserId)
                        .collection("followedMoods")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {


                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                String ownerUidDoc = doc.getString("ownerUid");
                                String moodIdDoc = doc.getString("moodId");
                                followedMoodsSet.add(ownerUidDoc + "_" + moodIdDoc);
                            }
                            moodAdapter.setFollowedMoodsSet(followedMoodsSet);
                            setupListeners(currentUserId);
                            moodRecyclerView.setAdapter(moodAdapter);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("MoodPageActivity", "Error fetching followed moods", e);
                            moodAdapter.setFollowedMoodsSet(followedMoodsSet); // Empty set if failed
                            setupListeners(currentUserId);
                            moodRecyclerView.setAdapter(moodAdapter);
                        });
            } else {
                moodRecyclerView.setAdapter(moodAdapter);
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
        Switch privacySwitch = dialogView.findViewById(R.id.privacySwitch);
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

        privacySwitch.setChecked(mood.isPrivateMood()); // Add this



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
            mood.setPrivate(privacySwitch.isChecked());
            mood.setMoodImage(mood.getMoodImage());

            moodAdapter.notifyDataSetChanged();
            updateMoodInFirestore(mood, moodId); // Your existing method to save to Firestore
            dialog.dismiss();
        });

        // Delete mood with confirmation
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Mood")
                    .setMessage("Are you sure you want to delete this mood?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        deleteMoodFromFirestore(moodId);
                        dialog.dismiss();
                        // finish() is called inside deleteMoodFromFirestore
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
                    if (snapshots != null) {
                        // Clear the list to avoid duplicates
                        comments.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            comments.add(comment);
                        }
                        // Update the UI on the main thread
                        runOnUiThread(() -> {
                            // Notify the adapter of data changes
                            commentAdapter.notifyDataSetChanged();
                            // Update visibility of "No Comments Yet" TextView
                            TextView noCommentsText = findViewById(R.id.no_comments_text);
                            if (comments.isEmpty()) {
                                noCommentsText.setVisibility(View.VISIBLE);
                            } else {
                                noCommentsText.setVisibility(View.GONE);
                            }
                        });
                    }
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
        updatedData.put("reason", mood.getMoodReason());
        updatedData.put("isPrivate", mood.isPrivateMood());

        // Convert taggedUserNames to Firestore-compatible tags format
        List<Map<String, Object>> tags = new ArrayList<>();
        if (mood.getTaggedUserNames() != null) {
            for (String username : mood.getTaggedUserNames()) {
                Map<String, Object> tag = new HashMap<>();
                tag.put("username", username);
                // Note: userId and name are unavailable in edit dialog; only username is used
                tags.add(tag);
            }
        }
        updatedData.put("tags", tags);

        db.collection("users").document(user.getUid()).collection("moods").document(moodId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("MoodPageActivity", "Mood updated successfully");
                    Toast.makeText(this, "Mood updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodPageActivity", "Error updating mood", e);
                    Toast.makeText(this, "Failed to update mood", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Deletes a mood entry from Firestore and removes the associated image from Firebase Storage (if any).
     *
     * @param moodId The unique identifier of the mood entry.
     */
    private void deleteMoodFromFirestore(String moodId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Reference to the mood document
        db.collection("users").document(user.getUid()).collection("moods").document(moodId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Mood document deleted successfully");
                    // Check connectivity for user feedback
                    if (isOnline()) {
                        Toast.makeText(this, "Mood deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Mood will be deleted when online", Toast.LENGTH_SHORT).show();
                    }
                    finish(); // Close the activity immediately
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting mood document", e);
                    Toast.makeText(this, "Failed to delete mood", Toast.LENGTH_SHORT).show();
                });

        // Handle image deletion if it exists
        String imageUrl = selectedMood.getMoodImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d("Storage", "Mood image deleted successfully"))
                    .addOnFailureListener(e -> Log.e("Storage", "Error deleting mood image", e));
        }
    }


    private void setupListeners(String currentUserId) {
        moodAdapter.setOnFollowClickListener((mood, isFollowing, button) -> {
            if (isFollowing) {
                unfollowMood(mood.getOwnerUid(), mood.getMoodId(), currentUserId, button);
            } else {
                followMood(mood.getOwnerUid(), mood.getMoodId(), currentUserId, button);
            }
        });

        moodAdapter.setOnShowFollowersListener(mood -> showFollowers(mood.getOwnerUid(), mood.getMoodId()));

        moodAdapter.setOnCommentButtonClickListener(() -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please log in to comment", Toast.LENGTH_SHORT).show();
                return;
            }
            // Toggle comment layout visibility
            commentLayout.setVisibility(
                    commentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
            if (commentLayout.getVisibility() == View.VISIBLE) {
                commentInput.requestFocus();
            }
        });
    }

    private void followMood(String ownerUid, String moodId, String currentUserId, Button button) {
        Map<String, Object> followerData = new HashMap<>();
        followerData.put("timestamp", FieldValue.serverTimestamp());

        // Add to mood's followers
        db.collection("users").document(ownerUid).collection("moods").document(moodId)
                .collection("followers").document(currentUserId)
                .set(followerData)
                .addOnSuccessListener(aVoid -> {
                    button.setText("Following");
                    followedMoodsSet.add(ownerUid + "_" + moodId);
                    Toast.makeText(this, "Now following this mood", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodPageActivity", "Error following mood", e);
                    Toast.makeText(this, "Failed to follow mood", Toast.LENGTH_SHORT).show();
                });

        // Add to user's followedMoods
        Map<String, Object> followedMoodData = new HashMap<>();
        followedMoodData.put("ownerUid", ownerUid);
        followedMoodData.put("moodId", moodId);
        followedMoodData.put("timestamp", FieldValue.serverTimestamp());
        db.collection("users").document(currentUserId).collection("followedMoods")
                .document(ownerUid + "_" + moodId)
                .set(followedMoodData);
    }

    private void unfollowMood(String ownerUid, String moodId, String currentUserId, Button button) {
        db.collection("users").document(ownerUid).collection("moods").document(moodId)
                .collection("followers").document(currentUserId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    button.setText("Follow");
                    followedMoodsSet.remove(ownerUid + "_" + moodId);
                    Toast.makeText(this, "Unfollowed this mood", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("MoodPageActivity", "Error unfollowing mood", e);
                    Toast.makeText(this, "Failed to unfollow mood", Toast.LENGTH_SHORT).show();
                });

        db.collection("users").document(currentUserId).collection("followedMoods")
                .document(ownerUid + "_" + moodId)
                .delete();
    }

    private void showFollowers(String ownerUid, String moodId) {
        db.collection("users").document(ownerUid).collection("moods").document(moodId)
                .collection("followers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> followerNames = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String followerUid = doc.getId();
                        db.collection("users").document(followerUid).get()
                                .addOnSuccessListener(userDoc -> {
                                    String username = userDoc.getString("username");
                                    followerNames.add(username != null ? username : followerUid);
                                    if (followerNames.size() == querySnapshot.size()) {
                                        String followersStr = followerNames.isEmpty() ? "No followers" : String.join("\n", followerNames);
                                        new AlertDialog.Builder(this).setTitle("Followers").setMessage(followersStr).setPositiveButton("OK", null).show();
                                    }
                                });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityReceiver != null) {
            unregisterReceiver(connectivityReceiver);
        }
        if (moodListener != null) {
            moodListener.remove();
        }
    }
}