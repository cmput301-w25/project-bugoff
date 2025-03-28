/**
 * The MoodAdapter class is an implementation of RecyclerView.Adapter that
 * binds a list of Mood objects to a RecyclerView. It handles different view types:
 * one for moods with an image and one for moods without an image.
 *
 * This adapter is responsible for inflating the appropriate layouts, binding
 * the data to the views, and determining the appropriate view type for each
 * mood item.
 */
package com.example.whimsy;

import static android.view.View.GONE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
        String imageUrl = moodList.get(position).getMoodImage();
        return (imageUrl != null && !imageUrl.isEmpty()) ? VIEW_TYPE_WITH_IMAGE : VIEW_TYPE_NO_IMAGE;
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
            ((MoodWithImageViewHolder) holder).bind(mood, this);// Bind data to the mood view with image
        } else {                                          // Bind data to the mood view without image
            ((MoodNoImageViewHolder) holder).bind(mood, this);;
        }
    }

    private String formatDateString(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("h:mm a - MMMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a • yyyy-MM-dd", Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return dateString; // Return the original string if parsing fails
        }
    }
    private void applyStyle(View itemView, String moodFeeling) {
        int styleResId;
        switch (moodFeeling.toLowerCase()) {
            case "feeling angry":
                styleResId = R.style.AngerStyle;
                break;
            case "feeling happy":
                styleResId = R.style.HappyStyle;
                break;
            case "feeling disgusted":
                styleResId = R.style.DisgustStyle;
                break;
            case "feeling sad":
                styleResId = R.style.SadStyle;
                break;
            case "feeling scared":
                styleResId = R.style.ScaredStyle;
                break;
            case "feeling excited":
                styleResId = R.style.ExcitedStyle;
                break;
            case "feeling ashamed":
                styleResId = R.style.AshamedStyle;
                break;
            case "feeling confused":
                styleResId = R.style.ConfusedStyle;
                break;
            // Add more cases for other moods (e.g., "sad", "scared")
            default:
                styleResId = R.style.AngerStyle; // Default style
                break;
        }
        Context context = itemView.getContext();
        TypedArray a = context.obtainStyledAttributes(styleResId, R.styleable.MoodStyle);
        int cardBackgroundColor = a.getColor(R.styleable.MoodStyle_cardBackgroundColor, Color.WHITE);
        int textColor = a.getColor(R.styleable.MoodStyle_textColor, Color.BLACK);
        int iconTint = a.getColor(R.styleable.MoodStyle_iconTint, Color.BLACK);
        int buttonBackgroundColor = a.getColor(R.styleable.MoodStyle_buttonBackgroundColor, Color.GRAY);
        a.recycle();

        CardView cardView = itemView.findViewById(R.id.card_view);
        cardView.setCardBackgroundColor(cardBackgroundColor);

        TextView userLocation = itemView.findViewById(R.id.user_location);
        userLocation.setCompoundDrawableTintList(ColorStateList.valueOf(iconTint));

        View moodVisibilityBg = itemView.findViewById(R.id.moodVisibilityBg);
        moodVisibilityBg.setBackgroundTintList(ColorStateList.valueOf(buttonBackgroundColor));

        ImageView moodVisibility = itemView.findViewById(R.id.moodVisibility);
        moodVisibility.setColorFilter(iconTint);

        TextView userTime = itemView.findViewById(R.id.user_time);
        userTime.setCompoundDrawableTintList(ColorStateList.valueOf(iconTint));

        TextView userGatheringStatus = itemView.findViewById(R.id.user_gathering_status);
        userGatheringStatus.setCompoundDrawableTintList(ColorStateList.valueOf(iconTint));

        TextView moodStatus = itemView.findViewById(R.id.mood_status);
        moodStatus.setTextColor(textColor);

        Button trackMoodButton = itemView.findViewById(R.id.track_mood_button);
        trackMoodButton.setTextColor(textColor); // Ensure text color is set
        trackMoodButton.setBackgroundTintList(ColorStateList.valueOf(buttonBackgroundColor));
        trackMoodButton.setCompoundDrawableTintList(ColorStateList.valueOf(iconTint));

        Button commentButton = itemView.findViewById(R.id.comment_button);
        commentButton.setTextColor(textColor); // Ensure text color is set
        commentButton.setBackgroundTintList(ColorStateList.valueOf(buttonBackgroundColor));
        commentButton.setCompoundDrawableTintList(ColorStateList.valueOf(iconTint));
    }
    private void setEmojiBasedOnMood(String moodFeeling, TextView moodStatus) {
        int emojiResId;
        switch (moodFeeling.toLowerCase()) {
            case "feeling angry":
                emojiResId = R.drawable.anger_emoji;
                break;
            case "feeling happy":
                emojiResId = R.drawable.happy_emoji;
                break;
            case "feeling disgusted":
                emojiResId = R.drawable.disgust_emoji;
                break;
            case "feeling sad":
                emojiResId = R.drawable.sad_emoji;
                break;
            case "feeling scared":
                emojiResId = R.drawable.scared_emoji;
                break;
            case "feeling excited":
                emojiResId = R.drawable.excited_emoji;
                break;
            case "feeling ashamed":
                emojiResId = R.drawable.ashamed_emoji;
                break;
            case "feeling confused":
                emojiResId = R.drawable.confused_emoji;
                break;
            default:
                emojiResId = R.drawable.anger_emoji; // Default emoji
                break;
        }
        moodStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(emojiResId, 0, 0, 0);
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
        private ImageView moodImage, profileImage;
        private View moodVisibilityBg;

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
            // Corrected ID: image_profile instead of profile_image
            profileImage = itemView.findViewById(R.id.image_profile);
            moodVisibilityBg = itemView.findViewById(R.id.moodVisibilityBg);
        }


        /**
         * Binds the data from the given Mood object to the views in this ViewHolder.
         *
         * @param mood The Mood object containing the data to bind.
         */
        void bind(Mood mood, MoodAdapter adapter) {
            userName.setText(mood.getUserName());
            userId.setText("@"+ mood.getUserId());
            userLocation.setText(mood.getUserLocation());
            userTime.setText(adapter.formatDateString(mood.getUserTime())); // Format the date string
            userGatheringStatus.setText(mood.getUserGatheringStatus());
            moodStatus.setText(mood.getMoodStatus());
            moodTrigger.setText(mood.getMoodTrigger());
            moodReason.setText(mood.getMoodReason());

            if (mood.isPrivateMood()) {
                moodVisibilityBg.setVisibility(View.VISIBLE);
            } else {
                moodVisibilityBg.setVisibility(View.GONE);
            }

            adapter.applyStyle(itemView, mood.getMoodStatus());
            adapter.setEmojiBasedOnMood(mood.getMoodStatus(), moodStatus);

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

    /**
     * ViewHolder for moods without an image.
     */
    static class MoodNoImageViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, userLocation, userTime, userGatheringStatus, moodStatus, moodTrigger, moodReason;
        private ImageView profileImage;
        private View moodVisibilityBg;

        /**
         * Constructor for the ViewHolder that holds views for moods without images.
         *
         * @param itemView The view for the individual item.
         */
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
            moodVisibilityBg = itemView.findViewById(R.id.moodVisibilityBg);
        }

        /**
         * Binds the data from the given Mood object to the views in this ViewHolder.
         *
         * @param mood The Mood object containing the data to bind.
         */
        void bind(Mood mood, MoodAdapter adapter) {
            userName.setText(mood.getUserName());
            userId.setText("@"+mood.getUserId());
            if (mood.getUserLocation().equals("No location")) {
                userLocation.setVisibility(GONE);
            } else {
                userLocation.setText(mood.getUserLocation());
            }
            userTime.setText(adapter.formatDateString(mood.getUserTime())); // Format the date string
            userTime.setText(mood.getUserTime());
            userGatheringStatus.setText(mood.getUserGatheringStatus());
            moodTrigger.setText(mood.getMoodTrigger());
            moodReason.setText(mood.getMoodReason());
            adapter.applyStyle(itemView, mood.getMoodStatus());
            moodStatus.setText(mood.getMoodStatus());

            if (mood.isPrivateMood()) {
                moodVisibilityBg.setVisibility(View.VISIBLE);
            } else {
                moodVisibilityBg.setVisibility(View.GONE);
            }

            adapter.setEmojiBasedOnMood(mood.getMoodStatus(), moodStatus);

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
