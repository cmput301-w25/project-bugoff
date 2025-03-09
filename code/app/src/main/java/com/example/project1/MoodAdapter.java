package com.example.project1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_WITH_IMAGE = 1;
    private static final int VIEW_TYPE_NO_IMAGE = 2;
    private List<Mood> moodList;

    public MoodAdapter(List<Mood> moodList) {
        this.moodList = moodList;
    }

    @Override
    public int getItemViewType(int position) {
        String imageUrl = moodList.get(position).getMoodImage();
        // Check if image URL is not null and not empty
        return (imageUrl != null && !imageUrl.isEmpty()) ? VIEW_TYPE_WITH_IMAGE : VIEW_TYPE_NO_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_WITH_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mood_fragment_with_image, parent, false);
            return new MoodWithImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mood_fragment_no_image, parent, false);
            return new MoodNoImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mood mood = moodList.get(position);
        if (holder instanceof MoodWithImageViewHolder) {
            ((MoodWithImageViewHolder) holder).bind(mood);
        } else if (holder instanceof MoodNoImageViewHolder) {
            ((MoodNoImageViewHolder) holder).bind(mood);
        }
    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    static class MoodWithImageViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, userLocation, userTime, userGatheringStatus, moodStatus, moodTrigger, moodReason;
        private ImageView moodImage;
        private ImageView profileImage; // Add profile image view


        public MoodWithImageViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userId = itemView.findViewById(R.id.user_ID);
            userLocation = itemView.findViewById(R.id.user_location);
            userTime = itemView.findViewById(R.id.user_time);
            userGatheringStatus = itemView.findViewById(R.id.user_gathering_status);
            moodStatus = itemView.findViewById(R.id.mood_status);
            moodTrigger = itemView.findViewById(R.id.mood_trigger);
            moodReason = itemView.findViewById(R.id.mood_reason);
            moodImage = itemView.findViewById(R.id.mood_image);
            profileImage = itemView.findViewById(R.id.profile_image);
        }

        public void bind(Mood mood) {
            userName.setText(mood.getUserName());
            userId.setText(mood.getUserId());
            userLocation.setText(mood.getUserLocation());
            userTime.setText(mood.getUserTime());
            userGatheringStatus.setText(mood.getUserGatheringStatus());
            moodStatus.setText(mood.getMoodStatus());
            moodTrigger.setText(mood.getMoodTrigger());
            moodReason.setText(mood.getMoodReason());

            String profileImageUrl = mood.getProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(profileImageUrl)
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile); // Set a default image if profileImageUrl is null or empty
            }

            String moodImageUrl = mood.getMoodImage();
            if (moodImageUrl != null && !moodImageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(moodImageUrl)
                        .placeholder(R.drawable.circle_background) // Optional placeholder
                        .error(R.drawable.ic_profile)              // Optional fallback on error
                        .into(moodImage);
            } else {
                moodImage.setImageResource(R.drawable.ic_profile); // Fallback if no image
            }

            userGatheringStatus.setOnClickListener(v -> {
                List<String> taggedUsers = mood.getTaggedUserNames();
                if (taggedUsers != null && !taggedUsers.isEmpty()) {
                    String taggedUsersStr = String.join("\n", taggedUsers); // Simple newline-separated list
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Tagged Users")
                            .setMessage(taggedUsersStr)
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
        }
    }

    static class MoodNoImageViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, userLocation, userTime, userGatheringStatus, moodStatus, moodTrigger, moodReason;
        private ImageView profileImage;


        public MoodNoImageViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userId = itemView.findViewById(R.id.user_ID);
            userLocation = itemView.findViewById(R.id.user_location);
            userTime = itemView.findViewById(R.id.user_time);
            userGatheringStatus = itemView.findViewById(R.id.user_gathering_status);
            moodStatus = itemView.findViewById(R.id.mood_status);
            moodTrigger = itemView.findViewById(R.id.mood_trigger);
            moodReason = itemView.findViewById(R.id.mood_reason);
            profileImage = itemView.findViewById(R.id.image_profile);
        }

        public void bind(Mood mood) {
            userName.setText(mood.getUserName());
            userId.setText(mood.getUserId());
            userLocation.setText(mood.getUserLocation());
            userTime.setText(mood.getUserTime());
            userGatheringStatus.setText(mood.getUserGatheringStatus());
            moodStatus.setText(mood.getMoodStatus());
            moodTrigger.setText(mood.getMoodTrigger());
            moodReason.setText(mood.getMoodReason());
            String imageUrl = mood.getProfileImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile); // Set a default image if imageUrl is null or empty
            }

            userGatheringStatus.setOnClickListener(v -> {
                List<String> taggedUsers = mood.getTaggedUserNames();
                if (taggedUsers != null && !taggedUsers.isEmpty()) {
                    String taggedUsersStr = String.join("\n", taggedUsers); // Simple newline-separated list
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Tagged Users")
                            .setMessage(taggedUsersStr)
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
        }
    }
}