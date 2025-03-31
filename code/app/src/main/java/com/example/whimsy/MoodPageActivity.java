/**
 * MoodPageActivity.java
 *
 * Activity for displaying and managing a selected mood entry.
 * Allows users to view mood details, edit the mood, tag friends, and delete the entry.
 *
 * Key Features:
 * - Display mood details including mood, reason, tags, and privacy settings.
 * - Edit mood via a dialog, including updating mood type, reason, location, and tagged friends.
 * - Tag friends functionality using a consistent tagging dialog layout (dialog_tag_users_rounded).
 * - Update an existing mood document in Firestore (mirroring the push logic in AddMood.java).
 * - Delete mood with confirmation.
 *
 * Outstanding Issues:
 * - Does not validate friend tags against an actual user database.
 * - Lacks confirmation prompt before deleting a mood entry.
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
    private ListenerRegistration moodListener; // Listener for mood updates
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
    @Override
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
        editMoodFab = findViewById(R.id.edit_mood_fab);
        editMoodFab.setBackgroundTintList(ColorStateList.valueOf(colorBg));
        editMoodFab.setImageTintList(ColorStateList.valueOf(colorFg));

        RecyclerView moodRecyclerView = findViewById(R.id.mood_detail_recycler_view);
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up comments RecyclerView
        RecyclerView commentsRecyclerView = findViewById(R.id.mood_comments_recycler_view);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(comments, colorFg, cardBg);
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
        commentLayout.setVisibility(View.GONE);
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
            ArrayList<Mood> moodList = new ArrayList<>();
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
                                // When editing, retrieve full tag details from Firestore first.
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
                            moodAdapter.setFollowedMoodsSet(followedMoodsSet);
                            setupListeners(currentUser.getUid());
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
     * Instead of using cached tag info, this method retrieves the full tag details directly from Firestore,
     * then calls a helper method to build the edit dialog.
     *
     * @param mood   The mood entry to be edited.
     * @param moodId The unique identifier of the mood entry.
     */
    private void showEditDialog(Mood mood, String moodId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Retrieve the full mood document (including "tags") from Firestore.
        db.collection("users").document(ownerUid)
                .collection("moods").document(moodId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    // Retrieve the full tag list from the database.
                    List<Map<String, Object>> tagsFromDB = (List<Map<String, Object>>) documentSnapshot.get("tags");
                    final List<User> tempTaggedUsers = new ArrayList<>();
                    if (tagsFromDB != null && !tagsFromDB.isEmpty()) {
                        for (Map<String, Object> tag : tagsFromDB) {
                            String userId = (String) tag.get("userId");
                            String username = (String) tag.get("username");
                            String name = (String) tag.get("name");
                            tempTaggedUsers.add(new User(userId, name, username, null));
                        }
                    }
                    // Call helper method to build the edit dialog.
                    showEditDialogWithTags(mood, moodId, tempTaggedUsers);
                }).addOnFailureListener(e -> {
                    Toast.makeText(MoodPageActivity.this, "Failed to load mood tags", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Builds and displays the edit dialog for a mood, pre-populated with tag data retrieved from the database.
     *
     * @param mood            The mood entry to be edited.
     * @param moodId          The unique identifier of the mood entry.
     * @param tempTaggedUsers A list of User objects representing the current tags (with userId, username, and name).
     */
    private void showEditDialogWithTags(Mood mood, String moodId, List<User> tempTaggedUsers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_mood, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Initialize UI components.
        Spinner moodSpinner = dialogView.findViewById(R.id.moodSpinner);
        EditText reasonInput = dialogView.findViewById(R.id.reasonInput);
        TextView reasonCharCountText = dialogView.findViewById(R.id.reasonCharCountText);
        Button tagButton = dialogView.findViewById(R.id.addTagButton);
        TextView taggedFriendsText = dialogView.findViewById(R.id.taggedFriendsText);
        Switch privacySwitch = dialogView.findViewById(R.id.privacySwitch);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteButton);

        // Setup mood spinner.
        String[] moodOptions = {"Happy", "Sad", "Angry", "Scared", "Confused", "Disgusted", "Excited", "Ashamed"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moodOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(spinnerAdapter);
        String currentMood = mood.getMoodStatus().replace("Feeling ", "");
        int moodIndex = Arrays.asList(moodOptions).indexOf(currentMood);
        if (moodIndex >= 0) {
            moodSpinner.setSelection(moodIndex);
        }

        // Populate reason field and setup character counter.
        reasonInput.setText(mood.getMoodReason());
        reasonInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                int remaining = 200 - s.length();
                reasonCharCountText.setText(String.valueOf(remaining));
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        privacySwitch.setChecked(mood.isPrivateMood());

        // Build a list of usernames from the temporary tag list and update the TextView.
        List<String> usernames = new ArrayList<>();
        for (User user : tempTaggedUsers) {
            usernames.add(user.getUsername());
        }
        updateTaggedFriendsText(taggedFriendsText, usernames);

        // Set click listener on tag button to launch the tagging dialog.
        tagButton.setOnClickListener(v -> showTagUsersDialog(taggedFriendsText, tempTaggedUsers));

        // Save button: build the mood data map and update Firestore.
        saveButton.setOnClickListener(v -> {
            Map<String, Object> moodData = new HashMap<>();
            // For consistency, store the mood as the selected option (without "Feeling " prefix, if desired)
            moodData.put("mood", moodSpinner.getSelectedItem().toString());
            moodData.put("reason", reasonInput.getText().toString().trim());
            moodData.put("isPrivate", privacySwitch.isChecked());
            // (If location update is supported in edit, include location fields here.)
            // Build full tags list from tempTaggedUsers.
            List<Map<String, Object>> updatedTags = new ArrayList<>();
            for (User user : tempTaggedUsers) {
                Map<String, Object> tag = new HashMap<>();
                tag.put("userId", user.getId());
                tag.put("username", user.getUsername());
                tag.put("name", user.getName());
                updatedTags.add(tag);
            }
            moodData.put("tags", updatedTags);

            // Update the mood document in Firestore.
            updateMoodToFirebase(mood.getMoodId(), moodData);
            dialog.dismiss();
        });

        // Delete button with confirmation.
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Mood")
                    .setMessage("Are you sure you want to delete this mood?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        deleteMoodFromFirestore(moodId);
                        dialog.dismiss();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Updates the existing mood document in Firestore with the new data.
     * This method builds a moodData map similar to AddMood.java but calls update() on the existing document.
     *
     * @param moodId    The unique identifier of the mood entry.
     * @param moodData  The data map containing updated mood details.
     */
    private void updateMoodToFirebase(String moodId, Map<String, Object> moodData) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showSnackbar("User not logged in");
            return;
        }
        db.collection("users").document(user.getUid())
                .collection("moods").document(moodId)
                .update(moodData)
                .addOnSuccessListener(aVoid -> {
                    showSnackbar("Mood updated successfully", false);
                })
                .addOnFailureListener(e -> {
                    showSnackbar("Error updating mood: " + e.getMessage());
                });
    }

    /**
     * Updates the tagged friends TextView with a list of usernames.
     *
     * @param textView      The TextView to update.
     * @param taggedFriends List of tagged friends' usernames.
     */
    private void updateTaggedFriendsText(TextView textView, List<String> taggedFriends) {
        if (taggedFriends == null || taggedFriends.isEmpty()) {
            textView.setText("No friends tagged");
        } else {
            textView.setText(String.join(", ", taggedFriends));
        }
    }

    /**
     * Displays a dialog for tagging users. Uses the same XML layout as in AddMood.java.
     *
     * @param taggedFriendsText TextView in the edit dialog to display current tags.
     * @param selectedUsers     List of currently selected User objects.
     */
    private void showTagUsersDialog(TextView taggedFriendsText, List<User> selectedUsers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tag_users_rounded, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText searchEditText = dialogView.findViewById(R.id.tag_search_edit_text);
        ImageView searchIcon = dialogView.findViewById(R.id.search_icon);
        RecyclerView recyclerView = dialogView.findViewById(R.id.tag_search_results_recycler_view);
        Button applyButton = dialogView.findViewById(R.id.tag_apply_button);
        Button cancelButton = dialogView.findViewById(R.id.tag_cancel_button);

        List<User> currentData = new ArrayList<>(selectedUsers);
        TagUsersAdapter adapter = new TagUsersAdapter(currentData, selectedUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    currentData.clear();
                    currentData.addAll(selectedUsers);
                    adapter.notifyDataSetChanged();
                    searchIcon.clearColorFilter();
                } else {
                    searchIcon.setColorFilter(getColor(R.color.black));
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    db.collection("users")
                            .orderBy("name")
                            .startAt(query)
                            .endAt(query + "\uf8ff")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                currentData.clear();
                                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                    // Skip the current user.
                                    if (currentUser != null && doc.getId().equals(currentUser.getUid())) {
                                        continue;
                                    }
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
            List<String> taggedUsernames = new ArrayList<>();
            for (User user : selectedUsers) {
                taggedUsernames.add(user.getUsername());
            }
            updateTaggedFriendsText(taggedFriendsText, taggedUsernames);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    /**
     * Fetches comments for the mood entry from Firestore.
     */
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
                        comments.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            comments.add(comment);
                        }
                        runOnUiThread(() -> {
                            commentAdapter.notifyDataSetChanged();
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

    /**
     * Posts a comment to Firestore.
     */
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
                    String profileImageUrl = documentSnapshot.getString("profilePictureUrl");
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
     * Sets up listeners for follow/unfollow actions and comment button click.
     *
     * @param currentUserId The current user's ID.
     */
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
            commentLayout.setVisibility(
                    commentLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
            if (commentLayout.getVisibility() == View.VISIBLE) {
                commentInput.requestFocus();
            }
        });
    }

    /**
     * Follows a mood by adding the current user as a follower.
     *
     * @param ownerUid      The owner's UID.
     * @param moodId        The mood's ID.
     * @param currentUserId The current user's ID.
     * @param button        The follow button to update.
     */
    private void followMood(String ownerUid, String moodId, String currentUserId, Button button) {
        Map<String, Object> followerData = new HashMap<>();
        followerData.put("timestamp", FieldValue.serverTimestamp());

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

        Map<String, Object> followedMoodData = new HashMap<>();
        followedMoodData.put("ownerUid", ownerUid);
        followedMoodData.put("moodId", moodId);
        followedMoodData.put("timestamp", FieldValue.serverTimestamp());
        db.collection("users").document(currentUserId).collection("followedMoods")
                .document(ownerUid + "_" + moodId)
                .set(followedMoodData);
    }

    /**
     * Unfollows a mood by removing the current user from the followers.
     *
     * @param ownerUid      The owner's UID.
     * @param moodId        The mood's ID.
     * @param currentUserId The current user's ID.
     * @param button        The follow button to update.
     */
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

    /**
     * Displays a dialog showing the list of followers for a mood.
     *
     * @param ownerUid The owner's UID.
     * @param moodId   The mood's ID.
     */
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
                                        new AlertDialog.Builder(this)
                                                .setTitle("Followers")
                                                .setMessage(followersStr)
                                                .setPositiveButton("OK", null)
                                                .show();
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

    /**
     * Deletes a mood entry from Firestore and removes the associated image from Firebase Storage if available.
     *
     * @param moodId The unique identifier of the mood entry.
     */
    private void deleteMoodFromFirestore(String moodId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).collection("moods").document(moodId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Mood document deleted successfully");
                    if (isOnline()) {
                        Toast.makeText(this, "Mood deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Mood will be deleted when online", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error deleting mood document", e);
                    Toast.makeText(this, "Failed to delete mood", Toast.LENGTH_SHORT).show();
                });

        String imageUrl = selectedMood.getMoodImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
            imageRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d("Storage", "Mood image deleted successfully"))
                    .addOnFailureListener(e -> Log.e("Storage", "Error deleting mood image", e));
        }
    }

    /**
     * Checks if the device is online.
     *
     * @return {@code true} if online, {@code false} otherwise.
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // --- TagUsersAdapter Inner Class ---

    /**
     * RecyclerView Adapter for displaying user search results in the tagging dialog.
     */
    private class TagUsersAdapter extends RecyclerView.Adapter<TagUsersAdapter.ViewHolder> {
        private List<User> users;
        private List<User> selectedUsers;

        /**
         * Constructs a new TagUsersAdapter.
         *
         * @param users         List of users to display.
         * @param selectedUsers List of currently selected users.
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
            // Load profile image using Glide.
            Glide.with(holder.profileImage.getContext())
                    .load(user.getProfilePictureUrl())
                    .placeholder(R.drawable.default_profile)
                    .into(holder.profileImage);
            // Highlight selection state.
            if (isUserSelected(user)) {
                holder.itemView.setBackgroundColor(getColor(android.R.color.darker_gray));
            } else {
                holder.itemView.setBackgroundColor(getColor(android.R.color.transparent));
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
         * Checks if a user is selected.
         *
         * @param user The user to check.
         * @return {@code true} if selected, {@code false} otherwise.
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
         * ViewHolder class for TagUsersAdapter.
         */
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
