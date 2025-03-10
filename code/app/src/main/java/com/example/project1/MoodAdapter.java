package com.example.project1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
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
        return (imageUrl != null && !imageUrl.isEmpty()) ? VIEW_TYPE_WITH_IMAGE : VIEW_TYPE_NO_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_WITH_IMAGE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mood_fragment_with_image, parent, false);
            return new MoodWithImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mood_fragment_no_image, parent, false);
            return new MoodNoImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mood mood = moodList.get(position);
        if (holder instanceof MoodWithImageViewHolder) {
            ((MoodWithImageViewHolder) holder).bind(mood);
        } else {
            ((MoodNoImageViewHolder) holder).bind(mood);
        }
    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    static class MoodWithImageViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, userLocation, userTime, userGatheringStatus, moodStatus, moodTrigger, moodReason;
        private ImageView moodImage, profileImage;

        MoodWithImageViewHolder(@NonNull View itemView) {
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

        void bind(Mood mood) {
            userName.setText(mood.getUserName());
            userId.setText(mood.getUserId());
            userLocation.setText(mood.getUserLocation());
            userTime.setText(mood.getUserTime());
            userGatheringStatus.setText(mood.getUserGatheringStatus());
            moodStatus.setText(mood.getMoodStatus());
            moodTrigger.setText(mood.getMoodTrigger());
            moodReason.setText(mood.getMoodReason());

            String pUrl = mood.getProfileImageUrl();
            if (pUrl != null && !pUrl.isEmpty()) {
                Glide.with(itemView.getContext()).load(pUrl).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile);
            }

            String mUrl = mood.getMoodImage();
            if (mUrl != null && !mUrl.isEmpty()) {
                Glide.with(itemView.getContext()).load(mUrl).into(moodImage);
            } else {
                moodImage.setImageResource(R.drawable.ic_profile);
            }

            userGatheringStatus.setOnClickListener(v -> {
                List<String> taggedUsers = mood.getTaggedUserNames();
                if (taggedUsers != null && !taggedUsers.isEmpty()) {
                    String taggedUsersStr = String.join("\n", taggedUsers);
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

        MoodNoImageViewHolder(@NonNull View itemView) {
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

        void bind(Mood mood) {
            userName.setText(mood.getUserName());
            userId.setText(mood.getUserId());
            userLocation.setText(mood.getUserLocation());
            userTime.setText(mood.getUserTime());
            userGatheringStatus.setText(mood.getUserGatheringStatus());
            moodStatus.setText(mood.getMoodStatus());
            moodTrigger.setText(mood.getMoodTrigger());
            moodReason.setText(mood.getMoodReason());

            String pUrl = mood.getProfileImageUrl();
            if (pUrl != null && !pUrl.isEmpty()) {
                Glide.with(itemView.getContext()).load(pUrl).into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.ic_profile);
            }

            userGatheringStatus.setOnClickListener(v -> {
                List<String> taggedUsers = mood.getTaggedUserNames();
                if (taggedUsers != null && !taggedUsers.isEmpty()) {
                    String taggedUsersStr = String.join("\n", taggedUsers);
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