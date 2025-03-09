package com.example.project1;

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

public class FollowingAdapter extends RecyclerView.Adapter<FollowingAdapter.UserViewHolder> {

    private final List<User> userList;
    private final String currentUserId;

    public FollowingAdapter(List<User> userList, String currentUserId) {
        this.userList = userList;
        this.currentUserId = currentUserId;
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

        // Load profile picture (assuming User class has getProfilePictureUrl())
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
            User currentUser = userList.get(currentPosition);
            db.collection("users")
                    .document(currentUserId)
                    .collection("following")
                    .document(currentUser.getId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Unfollow
                            db.collection("users")
                                    .document(currentUserId)
                                    .collection("following")
                                    .document(currentUser.getId())
                                    .delete();
                            db.collection("users")
                                    .document(currentUser.getId())
                                    .collection("followers")
                                    .document(currentUserId)
                                    .delete();
                            holder.followButton.setText("Follow");
                            if (userList.contains(currentUser)) {
                                userList.remove(currentPosition);
                                notifyItemRemoved(currentPosition);
                                notifyItemRangeChanged(currentPosition, userList.size());
                            }
                        } else {
                            // Follow
                            Map<String, Object> followData = new HashMap<>();
                            followData.put("followed", true);
                            db.collection("users")
                                    .document(currentUserId)
                                    .collection("following")
                                    .document(currentUser.getId())
                                    .set(followData);
                            Map<String, Object> followerData = new HashMap<>();
                            followerData.put("follower", true);
                            db.collection("users")
                                    .document(currentUser.getId())
                                    .collection("followers")
                                    .document(currentUserId)
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

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView userName;
        TextView userHandle;
        Button followButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            userName = itemView.findViewById(R.id.userName);
            userHandle = itemView.findViewById(R.id.userHandle);
            followButton = itemView.findViewById(R.id.followButton);
        }
    }
}