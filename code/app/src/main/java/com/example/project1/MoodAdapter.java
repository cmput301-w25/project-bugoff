/**
 * The MoodAdapter class is an implementation of RecyclerView.Adapter that
 * binds a list of Mood objects to a RecyclerView. It handles different view types:
 * one for moods with an image and one for moods without an image.
 *
 * This adapter is responsible for inflating the appropriate layouts, binding
 * the data to the views, and determining the appropriate view type for each
 * mood item.
 */
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

    private static final int VIEW_TYPE_WITH_IMAGE = 1;  // Constant for moods with images
    private static final int VIEW_TYPE_NO_IMAGE = 2;   // Constant for moods without images
    private List<Mood> moodList;  // List of Mood objects to be displayed

    /**
     * Constructor for the MoodAdapter.
     *
     * @param moodList The list of Mood objects that will be bound to the RecyclerView.
     */
    public MoodAdapter(List<Mood> moodList) {
        this.moodList = moodList;
    }

    /**
     * Determines the view type for the item at the given position.
     *
     * @param position The position of the item in the list.
     * @return The view type for the item, either with or without an image.
     */
    @Override
    public int getItemViewType(int position) {
        return moodList.get(position).getMoodImage() != null ? VIEW_TYPE_WITH_IMAGE : VIEW_TYPE_NO_IMAGE;
    }

    /**
     * Creates a ViewHolder based on the view type (with or without image).
     *
     * @param parent   The parent ViewGroup to which the new View will be attached.
     * @param viewType The type of the view (with or without image).
     * @return A new ViewHolder to hold the views for the corresponding mood item.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_WITH_IMAGE) {
            // Inflate the layout for moods with an image
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mood_fragment_with_image, parent, false);
            return new MoodWithImageViewHolder(view);
        } else {
            // Inflate the layout for moods without an image
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mood_fragment_no_image, parent, false);
            return new MoodNoImageViewHolder(view);
        }
    }

    /**
     * Binds the data to the ViewHolder based on the item at the given position.
     *
     * @param holder   The ViewHolder that will display the mood data.
     * @param position The position of the mood item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mood mood = moodList.get(position);
        if (holder instanceof MoodWithImageViewHolder) {
            ((MoodWithImageViewHolder) holder).bind(mood);  // Bind data to the mood view with image
        } else if (holder instanceof MoodNoImageViewHolder) {
            ((MoodNoImageViewHolder) holder).bind(mood);  // Bind data to the mood view without image
        }
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return The number of mood items in the list.
     */
    @Override
    public int getItemCount() {
        return moodList.size();
    }

    /**
     * ViewHolder for moods with an image.
     */
    static class MoodWithImageViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, userLocation, userTime, userGatheringStatus, moodStatus, moodTrigger, moodReason;
        private ImageView moodImage;

        /**
         * Constructor for the ViewHolder that holds views for moods with images.
         *
         * @param itemView The view for the individual item.
         */
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

        /**
         * Binds the data from the given Mood object to the views in this ViewHolder.
         *
         * @param mood The Mood object containing the data to bind.
         */
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

    /**
     * ViewHolder for moods without an image.
     */
    static class MoodNoImageViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, userLocation, userTime, userGatheringStatus, moodStatus, moodTrigger, moodReason;

        /**
         * Constructor for the ViewHolder that holds views for moods without images.
         *
         * @param itemView The view for the individual item.
         */
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

        /**
         * Binds the data from the given Mood object to the views in this ViewHolder.
         *
         * @param mood The Mood object containing the data to bind.
         */
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
