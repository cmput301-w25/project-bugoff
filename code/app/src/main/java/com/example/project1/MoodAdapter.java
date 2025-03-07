package com.example.project1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_WITH_IMAGE = 1;
    private static final int VIEW_TYPE_NO_IMAGE = 2;
    private static final int VIEW_TYPE_FOLLOW_REQUEST = 3;

    private List<Mood> moodList;

    public MoodAdapter(List<Mood> moodList) {
        this.moodList = moodList;
    }

    @Override
    public int getItemViewType(int position) {
        Mood mood = moodList.get(position);
        if (mood.isFollowRequest()) {
            return VIEW_TYPE_FOLLOW_REQUEST;
        } else {
            return mood.getMoodImage() != null ? VIEW_TYPE_WITH_IMAGE : VIEW_TYPE_NO_IMAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_WITH_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mood_fragment_with_image, parent, false);
            return new MoodWithImageViewHolder(view);
        } else if (viewType == VIEW_TYPE_NO_IMAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mood_fragment_no_image, parent, false);
            return new MoodNoImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_request_item, parent, false);
            return new FollowRequestViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mood mood = moodList.get(position);
        if (holder instanceof MoodWithImageViewHolder) {
            ((MoodWithImageViewHolder) holder).bind(mood);
        } else if (holder instanceof MoodNoImageViewHolder) {
            ((MoodNoImageViewHolder) holder).bind(mood);
        } else if (holder instanceof FollowRequestViewHolder) {
            ((FollowRequestViewHolder) holder).bind(mood);
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

    static class FollowRequestViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, followRequestText, requestTimestamp;
        private Button allowButton;

        public FollowRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userId = itemView.findViewById(R.id.user_ID);
            followRequestText = itemView.findViewById(R.id.follow_request_text);
            requestTimestamp = itemView.findViewById(R.id.request_timestamp);
            allowButton = itemView.findViewById(R.id.allow_button);
        }

        public void bind(Mood mood) {
            userName.setText(mood.getUserName());
            userId.setText(mood.getUserId());
            followRequestText.setText("Requested to follow your " + mood.getMoodStatus());
            requestTimestamp.setText(mood.getUserTime());

            allowButton.setOnClickListener(v -> {
                // Handle follow request acceptance logic
                allowButton.setText("Accepted");
                allowButton.setEnabled(false);
            });
        }
    }
}