package com.example.whimsy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

/**
 * Adapter for displaying comments in a RecyclerView.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private int textColor;

    /**
     * Constructs a new CommentAdapter with the specified list of comments and text color.
     *
     * @param comments The list of comments to display.
     * @param textColor The color to apply to the text of the comments.
     */
    public CommentAdapter(List<Comment> comments, int textColor) {
        this.comments = comments;
        this.textColor = textColor;
    }

    /**
     * Called when RecyclerView needs a new {@link CommentViewHolder} of the given type to represent an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new CommentViewHolder that holds a View of the given view type.
     */
    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_fragment, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the {@link CommentViewHolder#itemView} to reflect the item at the given position.
     *
     * @param holder The CommentViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.commenterName.setText(comment.getCommenterName());
        holder.commentContent.setText(comment.getCommentText());

        // Apply text color only
        holder.commenterName.setTextColor(textColor);
        holder.commentContent.setTextColor(textColor);

        // Load profile image (unchanged)
        String profileImageUrl = comment.getProfileImageUrl();
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(profileImageUrl)
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * ViewHolder class for holding the views for each comment item.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commenterName, commentContent;
        ImageView profileImage;

        /**
         * Constructs a new CommentViewHolder with the specified itemView.
         *
         * @param itemView The view of the comment item.
         */
        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commenterName = itemView.findViewById(R.id.commenter_name);
            commentContent = itemView.findViewById(R.id.comment_content);
            profileImage = itemView.findViewById(R.id.image_profile);
        }
    }
}