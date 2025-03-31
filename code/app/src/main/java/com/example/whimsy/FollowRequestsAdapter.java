/**
 * The {@code FollowRequestsAdapter} class is a custom adapter for displaying follow requests
 * in a {@link RecyclerView}. Each item in the list represents a follow request, displaying the
 * requester's name, profile picture, and buttons for accepting or rejecting the request.
 *
 * Key Features:
 *
 *     Displays follow request details including the requester's name and profile picture.
 *     Handles accept and reject actions for each follow request, triggering corresponding
 *     actions through the {@link RequestActionListener}.
 *     Integrates with Firebase Firestore to retrieve user data such as name and profile picture.
 *     Utilizes Glide for image loading, falling back to a default profile image if necessary.
 *     Provides an efficient way to display a list of follow requests and update the UI accordingly.
 *
 */

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

/**
 * Adapter for displaying follow requests in a RecyclerView.
 */
public class FollowRequestsAdapter extends RecyclerView.Adapter<FollowRequestsAdapter.RequestViewHolder> {

    /**
     * Interface to handle accept/reject actions for follow requests.
     */
    public interface RequestActionListener {
        /**
         * Called when a follow request is accepted.
         *
         * @param requestDoc The document snapshot of the follow request.
         */
        void onAccept(DocumentSnapshot requestDoc);

        /**
         * Called when a follow request is rejected.
         *
         * @param requestDoc The document snapshot of the follow request.
         */
        void onReject(DocumentSnapshot requestDoc);
    }

    private List<DocumentSnapshot> requests;
    private RequestActionListener listener;
    private FirebaseFirestore db;

    /**
     * Constructs a new FollowRequestsAdapter with the specified list of requests and action listener.
     *
     * @param requests The list of follow request document snapshots.
     * @param listener The listener for handling accept/reject actions.
     */
    public FollowRequestsAdapter(List<DocumentSnapshot> requests, RequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Called when RecyclerView needs a new {@link RequestViewHolder} of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new RequestViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow_request, parent, false);
        return new RequestViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the {@link RequestViewHolder#itemView} to reflect the item at the given position.
     *
     * @param holder The RequestViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        DocumentSnapshot requestDoc = requests.get(position);
        String requesterId = requestDoc.getId();
        db.collection("users").document(requesterId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");
                        holder.requesterNameText.setText(name);
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
        holder.acceptButton.setOnClickListener(v -> listener.onAccept(requestDoc));
        holder.rejectButton.setOnClickListener(v -> listener.onReject(requestDoc));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return requests.size();
    }

    /**
     * ViewHolder class for holding the views for each follow request item.
     */
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView requesterImage;
        TextView requesterNameText;
        Button acceptButton, rejectButton;

        /**
         * Constructs a new RequestViewHolder with the specified itemView.
         *
         * @param itemView The view of the follow request item.
         */
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requesterImage = itemView.findViewById(R.id.requester_image);
            requesterNameText = itemView.findViewById(R.id.requester_name_text);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}