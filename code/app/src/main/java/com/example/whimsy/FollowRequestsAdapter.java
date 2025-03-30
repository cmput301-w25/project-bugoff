// NEW: FollowRequestsAdapter.java
package com.example.whimsy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView; // NEW: Import ImageView
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // NEW: For image loading
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class FollowRequestsAdapter extends RecyclerView.Adapter<FollowRequestsAdapter.RequestViewHolder> {

    // NEW: Interface to handle accept/reject actions.
    public interface RequestActionListener {
        void onAccept(DocumentSnapshot requestDoc);
        void onReject(DocumentSnapshot requestDoc);
    }

    private List<DocumentSnapshot> requests;
    private RequestActionListener listener;
    private FirebaseFirestore db; // NEW: Firestore instance

    public FollowRequestsAdapter(List<DocumentSnapshot> requests, RequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
        db = FirebaseFirestore.getInstance(); // NEW: Initialize Firestore
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // NEW: Inflate our updated layout (item_follow_request.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        DocumentSnapshot requestDoc = requests.get(position);
        String requesterId = requestDoc.getId();
        // NEW: Query Firestore to retrieve the requester's details (name and profilePictureUrl)
        db.collection("users").document(requesterId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                        // EDITED: Set the name instead of user id
                        holder.requesterNameText.setText(name);
                        // NEW: Load the profile image using Glide
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            Glide.with(holder.itemView.getContext())
                                    .load(profilePictureUrl)
                                    .into(holder.requesterImage);
                        } else {
                            holder.requesterImage.setImageResource(R.drawable.ic_profile);
                        }
                    } else {
                        holder.requesterNameText.setText("Unknown");
                        holder.requesterImage.setImageResource(R.drawable.ic_profile);
                    }
                })
                .addOnFailureListener(e -> {
                    holder.requesterNameText.setText("Error");
                    holder.requesterImage.setImageResource(R.drawable.ic_profile);
                });
        // Set up button click listeners
        holder.acceptButton.setOnClickListener(v -> listener.onAccept(requestDoc));
        holder.rejectButton.setOnClickListener(v -> listener.onReject(requestDoc));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView requesterImage; // NEW: Profile image
        TextView requesterNameText; // EDITED: Shows requester name
        Button acceptButton, rejectButton;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requesterImage = itemView.findViewById(R.id.requester_image); // NEW: ID from updated layout
            requesterNameText = itemView.findViewById(R.id.requester_name_text); // EDITED: updated id
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}
