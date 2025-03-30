package com.example.whimsy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FollowingAdapter is responsible for displaying a list of users
 * in a RecyclerView, allowing interactions such as following and unfollowing.
 */
public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.UserViewHolder> {

    private final List<User> userList;
    private final String currentUserId;
    private final boolean isSelf;

    /**
     * Constructor to initialize the adapter with user list and current user ID.
     *
     * @param userList      List of users to be displayed
     * @param currentUserId ID of the logged-in user
     * @param isSelf
     */
    public FollowingAdapter(List<User> userList, String currentUserId, boolean isSelf) {
        this.userList = userList;
        this.currentUserId = currentUserId;
        this.isSelf = isSelf;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());
        holder.userHandle.setText(user.getUsername());

        // Load profile picture from URL
        Glide.with(holder.itemView.getContext())
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.ic_profile)
                .into(holder.profilePicture);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(user.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        holder.followButton.setText("Following");
                    } else {
                        holder.followButton.setText("Follow");
                    }
                });

        holder.followButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;
            User currentItem = userList.get(currentPosition);

            db.collection("users")
                    .document(currentUserId)
                    .collection("following")
                    .document(currentItem.getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Unfollow logic
                            db.collection("users")
                                    .document(currentUserId)
                                    .collection("following")
                                    .document(currentItem.getId())
                                    .delete();
                            db.collection("users")
                                    .document(currentItem.getId())
                                    .collection("followers")
                                    .document(currentUserId)
                                    .delete();
                            holder.followButton.setText("Follow");

                            // Remove user from the list and update UI
                            if (isSelf) {
                                if (userList.contains(currentItem)) {
                                    userList.remove(currentPosition);
                                    notifyItemRemoved(currentPosition);
                                    notifyItemRangeChanged(currentPosition, userList.size());
                                }
                            }
                        } else {
                            // Follow: Add to following and followers subcollections
                            Map<String, Object> followData = new HashMap<>();
                            followData.put("followed", true);
                            db.collection("users")
                                    .document(currentUserId)  // NEW: using currentUserId here
                                    .collection("following")
                                    .document(currentItem.getId())
                                    .set(followData);
                            Map<String, Object> followerData = new HashMap<>();
                            followerData.put("follower", true);
                            db.collection("users")
                                    .document(currentItem.getId())
                                    .collection("followers")
                                    .document(currentUserId)  // NEW: using currentUserId here
                                    .set(followerData);
                            holder.followButton.setText("Following");
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder class for representing individual user items in the RecyclerView.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView userName;
        TextView userHandle;
        Button followButton;

        /**
         * Initializes UI elements for each item.
         *
         * @param itemView The view associated with each item
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            userName = itemView.findViewById(R.id.userName);
            userHandle = itemView.findViewById(R.id.userHandle);
            followButton = itemView.findViewById(R.id.followButton);
        }
    }
}
