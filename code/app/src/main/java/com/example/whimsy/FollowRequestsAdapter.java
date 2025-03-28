// NEW: FollowRequestsAdapter.java
package com.example.whimsy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.List;

public class FollowRequestsAdapter extends RecyclerView.Adapter<FollowRequestsAdapter.RequestViewHolder> {

    // NEW: Interface to handle accept/reject actions.
    public interface RequestActionListener {
        void onAccept(DocumentSnapshot requestDoc);
        void onReject(DocumentSnapshot requestDoc);
    }

    private List<DocumentSnapshot> requests;
    private RequestActionListener listener;

    // NEW: Constructor for FollowRequestsAdapter.
    public FollowRequestsAdapter(List<DocumentSnapshot> requests, RequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // NEW: Inflate the follow request item layout.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        DocumentSnapshot requestDoc = requests.get(position);
        // NEW: Display the requester ID (you might replace this with the actual name if available)
        holder.requesterIdText.setText("User: " + requestDoc.getId());
        holder.acceptButton.setOnClickListener(v -> listener.onAccept(requestDoc));
        holder.rejectButton.setOnClickListener(v -> listener.onReject(requestDoc));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    // NEW: ViewHolder class for follow requests.
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView requesterIdText;
        Button acceptButton, rejectButton;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            requesterIdText = itemView.findViewById(R.id.requester_id_text);
            acceptButton = itemView.findViewById(R.id.accept_button);
            rejectButton = itemView.findViewById(R.id.reject_button);
        }
    }
}
