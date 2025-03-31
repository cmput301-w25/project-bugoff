package com.example.whimsy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
/**
 * SearchResultAdapter is a custom RecyclerView adapter
 * for displaying search results with user information.
 * This adapter binds user data, including usernames and profile pictures,
 * to the RecyclerView items and handles item click events to open user profiles.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder> {

    private List<User> userList;  // List of users to display in the RecyclerView
    private Context context;  // Context used for starting activities

    /**
     * Constructor to initialize the adapter with a context and a list of users.
     *
     * @param context The context for starting activities.
     * @param userList The list of users to display.
     */
    public SearchResultAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    /**
     * ViewHolder class for holding references to the views of each RecyclerView item.
     */
    public class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView displayNameText;  // TextView for displaying the user's full name
        TextView usernameText;  // TextView for displaying the user's username
        ImageView profileImageView;  // ImageView for displaying the user's profile picture

        /**
         * Constructor to initialize the ViewHolder with item views.
         *
         * @param itemView The item view to hold references to.
         */
        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            displayNameText = itemView.findViewById(R.id.text_display_name);  // Initialize the display name TextView
            usernameText = itemView.findViewById(R.id.text_username);  // Initialize the username TextView
            profileImageView = itemView.findViewById(R.id.image_profile);  // Initialize the profile image view

            // Set an OnClickListener for the entire item to open the user's profile
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();  // Get the position of the clicked item
                if (position != RecyclerView.NO_POSITION) {
                    User clickedUser = userList.get(position);  // Get the clicked user
                    openUserProfile(clickedUser);  // Open the user's profile
                }
            });
        }

        /**
         * Opens the user's profile by starting a new activity.
         *
         * @param user The user whose profile to open.
         */
        private void openUserProfile(User user) {
            Intent intent = new Intent(context, ProfileActivity.class);  // Start ProfileActivity
            intent.putExtra("USER_ID", user.getId());  // Pass the user's ID to the activity
            intent.putExtra("name", user.getName());  // Pass the user's name
            intent.putExtra("username", user.getUsername());  // Pass the user's username
            context.startActivity(intent);  // Start the ProfileActivity
        }
    }

    /**
     * Creates and returns a ViewHolder for each item in the RecyclerView.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the item.
     * @return A new ViewHolder for the RecyclerView item.
     */
    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each search result item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ResultViewHolder(itemView);  // Return a new ViewHolder with the inflated view
    }

    /**
     * Binds the user data to the views in the ViewHolder at the given position.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item to bind.
     */
    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        User user = userList.get(position);  // Get the user at the current position
        holder.displayNameText.setText(user.getUsername());  // Set the username text
        fetchDisplayName(user.getId(), holder.usernameText);  // Fetch and set the user's full name

        // Load the user's profile picture using Glide if available
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(context).load(user.getProfilePictureUrl()).into(holder.profileImageView);  // Load profile picture with Glide
        } else {
            holder.profileImageView.setImageResource(R.drawable.ic_profile);  // Set a default profile picture if not available
        }

        // Set an OnClickListener to navigate to the OtherProfileActivity
        holder.itemView.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (user.getId().equals(currentUser.getUid())) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent(context, OtherProfileActivity.class);  // Start OtherProfileActivity
                intent.putExtra("USER_ID", user.getId());  // Pass the user's ID to the activity
                context.startActivity(intent);  // Start the activity
            }
        });
    }

    /**
     * Returns the total number of items in the user list.
     *
     * @return The number of items in the list.
     */
    @Override
    public int getItemCount() {
        return userList.size();  // Return the size of the user list
    }

    /**
     * Fetches the display name (email) of the user from Firestore
     * and sets it in the username TextView.
     *
     * @param userId The ID of the user to fetch.
     * @param usernameText The TextView to display the user's name.
     */
    private void fetchDisplayName(String userId, TextView usernameText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();  // Get Firestore instance
        db.collection("users").document(userId).get()  // Query Firestore for the user's data
                .addOnSuccessListener(documentSnapshot -> {  // On success, handle the data
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");  // Get the display name (email)
                        usernameText.setText("@"+username);  // Set the display name in the TextView
                    } else {
                        usernameText.setText("Unknown");  // Set a default value if the user doesn't exist
                    }
                })
                .addOnFailureListener(e -> {
                    usernameText.setText("Error");  // Set "Error" if the query fails
                });
    }
}
