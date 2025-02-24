package com.example.project1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        return moodList.get(position).getMoodImage() != null ? VIEW_TYPE_WITH_IMAGE : VIEW_TYPE_NO_IMAGE;
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
            moodImage.setImageResource(mood.getMoodImage());
        }
    }

    static class MoodNoImageViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, userLocation, userTime, userGatheringStatus, moodStatus, moodTrigger, moodReason;

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
        }
    }
}