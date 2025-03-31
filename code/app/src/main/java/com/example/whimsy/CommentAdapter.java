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

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private int textColor;

    public CommentAdapter(List<Comment> comments, int textColor, int cardBg) {
        this.comments = comments;
        this.textColor = textColor;
    }
    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_fragment, parent, false);
        return new CommentViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commenterName, commentContent;
        ImageView profileImage;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commenterName = itemView.findViewById(R.id.commenter_name);
            commentContent = itemView.findViewById(R.id.comment_content);
            profileImage = itemView.findViewById(R.id.image_profile);
        }
    }
}