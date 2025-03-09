package com.example.project1;


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
import com.example.project1.OtherProfileActivity;
import com.example.project1.ProfileActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder> {

    private List<User> userList;
    private Context context;

    public SearchResultAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    public class ResultViewHolder extends RecyclerView.ViewHolder {
        TextView displayNameText;
        TextView usernameText;
        ImageView profileImageView;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            displayNameText = itemView.findViewById(R.id.text_display_name);
            usernameText = itemView.findViewById(R.id.text_username);
            profileImageView = itemView.findViewById(R.id.image_profile);

            // Set an OnClickListener to open the user's profile when tapped
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    User clickedUser = userList.get(position);
                    openUserProfile(clickedUser);
                }
            });
        }

        private void openUserProfile(User user) {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("USER_ID", user.getId());
            intent.putExtra("name", user.getName());
            intent.putExtra("username", user.getUsername());
            context.startActivity(intent);
        }
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        User user = userList.get(position);
        holder.displayNameText.setText(user.getUsername());
        fetchDisplayName(user.getId(), holder.usernameText);

        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(context).load(user.getProfilePictureUrl()).into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.ic_profile); // Set default image if no profile picture
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OtherProfileActivity.class);
            intent.putExtra("USER_ID", user.getId());  // ✅ Ensure the correct user ID is passed
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void fetchDisplayName(String userId, TextView usernameText) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String displayName = documentSnapshot.getString("email");
                        usernameText.setText(displayName);
                    } else {
                        usernameText.setText("Unknown");
                    }
                })
                .addOnFailureListener(e -> {
                    usernameText.setText("Error");
                });
    }
}