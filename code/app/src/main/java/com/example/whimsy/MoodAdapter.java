package com.example.whimsy;

import static android.view.View.GONE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
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
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Set;

/**
 * <h1>MoodAdapter Class</h1>
 * <p>
 * The {@code MoodAdapter} class is an implementation of {@link RecyclerView.Adapter} that binds a list of
 * {@link Mood} objects to a {@link RecyclerView}. It supports two view types:
 * one for moods with an image and one for moods without an image.
 * </p>
 * <p>
 * <strong>Usage:</strong> Instantiate the adapter with a list of {@code Mood} objects and set any necessary
 * listeners (follow, comment, or show followers). Then, attach the adapter to a {@code RecyclerView} to display
 * the mood items.
 * </p>
 * <p>
 * <strong>Outstanding Issues:</strong>
 * <ul>
 *   <li>
 *     In {@link MoodNoImageViewHolder#bind(Mood, MoodAdapter)}, the formatted date is set via
 *     {@code adapter.formatDateString(mood.getUserTime())} but is immediately overwritten by the original
 *     {@code mood.getUserTime()}. This should be fixed to ensure consistent date formatting.
 *   </li>
 * </ul>
 * </p>
 *
 * @see RecyclerView.Adapter
 * @see Mood
 * @version 1.0
 */
public class MoodAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_WITH_IMAGE = 1;  // Constant for moods with images
    private static final int VIEW_TYPE_NO_IMAGE = 2;    // Constant for moods without images

    /**
     * List of {@link Mood} objects to be displayed.
     */
    public List<Mood> moodList;

    /**
     * Set of keys representing moods that are followed.
     */
    private Set<String> followedMoodsSet = new HashSet<>();

    /**
     * Listener for follow button click events.
     */
    public OnFollowClickListener onFollowClickListener;

    /**
     * Listener for showing followers on long-click events.
     */
    private OnShowFollowersListener onShowFollowersListener;

    /**
     * Listener for comment button click events.
     */
    private OnCommentButtonClickListener onCommentButtonClickListener;

    /**
     * Sets the set of followed mood keys and refreshes the view.
     *
     * @param followedMoodsSet A {@code Set<String>} containing keys of followed moods.
     */
    public void setFollowedMoodsSet(Set<String> followedMoodsSet) {
        this.followedMoodsSet = followedMoodsSet;
        notifyDataSetChanged();
    }

    /**
     * Updates the first mood in the list with a new {@link Mood} object and refreshes the view.
     *
     * @param newMood The new {@code Mood} object to replace the first element.
     */
    public void updateMood(Mood newMood) {
        if (!moodList.isEmpty()) {
            moodList.set(0, newMood);
            notifyDataSetChanged();
        }
    }

    /**
     * Sets the listener for follow button click events.
     *
     * @param listener An implementation of {@link OnFollowClickListener}.
     */
    public void setOnFollowClickListener(OnFollowClickListener listener) {
        this.onFollowClickListener = listener;
    }

    /**
     * Sets the listener for showing followers on long-click events.
     *
     * @param listener An implementation of {@link OnShowFollowersListener}.
     */
    public void setOnShowFollowersListener(OnShowFollowersListener listener) {
        this.onShowFollowersListener = listener;
    }

    /**
     * Sets the listener for comment button click events.
     *
     * @param listener An implementation of {@link OnCommentButtonClickListener}.
     */
    public void setOnCommentButtonClickListener(OnCommentButtonClickListener listener) {
        this.onCommentButtonClickListener = listener;
    }

    /**
     * Listener interface for handling follow button click events.
     */
    public interface OnFollowClickListener {
        /**
         * Called when the follow button is clicked.
         *
         * @param mood        The {@link Mood} object associated with the follow button.
         * @param isFollowing {@code true} if the mood is already followed, {@code false} otherwise.
         * @param button      The {@link Button} that was clicked.
         */
        void onFollowClick(Mood mood, boolean isFollowing, Button button);
    }

    /**
     * Listener interface for handling comment button click events.
     */
    public interface OnCommentButtonClickListener {
        /**
         * Called when the comment button is clicked.
         */
        void onCommentButtonClick();
    }

    /**
     * Listener interface for handling events to show followers.
     */
    public interface OnShowFollowersListener {
        /**
         * Called when a long-click on the follow button triggers the show followers event.
         *
         * @param mood The {@link Mood} object associated with the event.
         */
        void onShowFollowers(Mood mood);
    }

    /**
     * Constructs a new {@code MoodAdapter} with the provided list of {@link Mood} objects.
     *
     * @param moodList The list of {@code Mood} objects to be bound to the RecyclerView.
     */
    public MoodAdapter(List<Mood> moodList) {
        this.moodList = moodList;
    }

    /**
     * Determines the view type for the item at the specified position.
     * Returns {@code VIEW_TYPE_WITH_IMAGE} if the {@link Mood} has a non-null and non-empty mood image URL;
     * otherwise, returns {@code VIEW_TYPE_NO_IMAGE}.
     *
     * @param position The position of the item in the list.
     * @return An integer representing the view type.
     */
    @Override
    public int getItemViewType(int position) {
        String imageUrl = moodList.get(position).getMoodImage();
        return (imageUrl != null && !imageUrl.isEmpty()) ? VIEW_TYPE_WITH_IMAGE : VIEW_TYPE_NO_IMAGE;
    }

    /**
     * Creates a new {@link RecyclerView.ViewHolder} for the given view type.
     * Inflates the appropriate layout based on whether the mood has an image.
     *
     * @param parent   The parent {@link ViewGroup} to which the new view will be attached.
     * @param viewType The view type, either {@code VIEW_TYPE_WITH_IMAGE} or {@code VIEW_TYPE_NO_IMAGE}.
     * @return A new instance of {@link RecyclerView.ViewHolder} for the specified view type.
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
     * Binds the data from the {@link Mood} object at the specified position to the provided ViewHolder.
     *
     * @param holder   The {@link RecyclerView.ViewHolder} which should be updated.
     * @param position The position of the item in the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Mood mood = moodList.get(position);
        if (holder instanceof MoodWithImageViewHolder) {
            ((MoodWithImageViewHolder) holder).bind(mood, this);
        } else {
            ((MoodNoImageViewHolder) holder).bind(mood, this);
        }
    }

    /**
     * Formats the input date string from the format "h:mm a - MMMM dd, yyyy" to "h:mm a • yyyy-MM-dd".
     * If parsing fails, the original date string is returned.
     *
     * @param dateString The date string to format.
     * @return A formatted date string.
     */
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

    /**
     * Applies a style to the provided view based on the mood feeling.
     * The style is determined from a set of predefined styles in resources.
     *
     * @param itemView    The view to which the style should be applied.
     * @param moodFeeling The mood status string which determines the style.
     */
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
            // Add more cases for other moods as needed
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
        trackMoodButton.setTextColor(textColor);
        trackMoodButton.setBackgroundTintList(ColorStateList.valueOf(buttonBackgroundColor));
        trackMoodButton.setCompoundDrawableTintList(ColorStateList.valueOf(iconTint));

        Button commentButton = itemView.findViewById(R.id.comment_button);
        commentButton.setTextColor(textColor);
        commentButton.setBackgroundTintList(ColorStateList.valueOf(buttonBackgroundColor));
        commentButton.setCompoundDrawableTintList(ColorStateList.valueOf(iconTint));
    }

    /**
     * Sets an emoji drawable on the provided TextView based on the mood feeling.
     *
     * @param moodFeeling The mood status string which determines the emoji.
     * @param moodStatus  The {@link TextView} where the emoji should be set.
     */
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
     * Returns the total number of mood items in the list.
     *
     * @return The size of the {@code moodList}.
     */
    @Override
    public int getItemCount() {
        return moodList.size();
    }

    /**
     * <h2>ViewHolder for Moods with an Image</h2>
     * <p>
     * This ViewHolder holds and binds views for mood items that include an image.
     * </p>
     */
    static class MoodWithImageViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, userLocation, userTime, userGatheringStatus, moodStatus, moodTrigger, moodReason;
        private ImageView moodImage, profileImage;
        private View moodVisibilityBg;
        private Button trackMoodButton;
        private Button commentButton;

        /**
         * Constructs a new {@code MoodWithImageViewHolder}.
         *
         * @param itemView The view representing the individual mood item with an image.
         */
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
            trackMoodButton = itemView.findViewById(R.id.track_mood_button);
            commentButton = itemView.findViewById(R.id.comment_button);
        }

        /**
         * Binds the data from the given {@link Mood} object to the views in this ViewHolder.
         *
         * @param mood    The {@code Mood} object containing the data.
         * @param adapter The {@link MoodAdapter} instance to access adapter methods and listeners.
         */
        void bind(Mood mood, MoodAdapter adapter) {
            userName.setText(mood.getUserName());
            userId.setText("@" + mood.getUserId());
            userLocation.setText(mood.getUserLocation());
            userTime.setText(adapter.formatDateString(mood.getUserTime())); // Format the date string
            userGatheringStatus.setText(mood.getUserGatheringStatus());
            moodStatus.setText(mood.getMoodStatus());
            moodTrigger.setText(mood.getMoodTrigger());
            moodReason.setText(mood.getMoodReason());

            if (mood.getUserLocation().equals("No location")) {
                userLocation.setVisibility(GONE);
            } else {
                userLocation.setText(mood.getUserLocation());
            }

            if (mood.isPrivateMood()) {
                moodVisibilityBg.setVisibility(View.VISIBLE);
            } else {
                moodVisibilityBg.setVisibility(View.GONE);
            }

            commentButton.setOnClickListener(v -> {
                if (adapter.onCommentButtonClickListener != null) {
                    adapter.onCommentButtonClickListener.onCommentButtonClick();
                }
            });
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

            String key = mood.getOwnerUid() + "_" + mood.getMoodId();
            boolean isFollowing = adapter.followedMoodsSet.contains(key);
            trackMoodButton.setText(isFollowing ? "Following" : "Follow");

            trackMoodButton.setOnClickListener(v -> {
                if (adapter.onFollowClickListener != null) {
                    adapter.onFollowClickListener.onFollowClick(mood, isFollowing, trackMoodButton);
                }
            });

            trackMoodButton.setOnLongClickListener(v -> {
                if (adapter.onShowFollowersListener != null) {
                    adapter.onShowFollowersListener.onShowFollowers(mood);
                    return true;
                }
                return false;
            });

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
     * <h2>ViewHolder for Moods without an Image</h2>
     * <p>
     * This ViewHolder holds and binds views for mood items that do not include an image.
     * </p>
     */
    static class MoodNoImageViewHolder extends RecyclerView.ViewHolder {
        private TextView userName, userId, userLocation, userTime, userGatheringStatus, moodStatus, moodTrigger, moodReason;
        private ImageView profileImage;
        private View moodVisibilityBg;
        private Button trackMoodButton;
        private Button commentButton;

        /**
         * Constructs a new {@code MoodNoImageViewHolder}.
         *
         * @param itemView The view representing the individual mood item without an image.
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
            trackMoodButton = itemView.findViewById(R.id.track_mood_button);
            commentButton = itemView.findViewById(R.id.comment_button);
        }

        /**
         * Binds the data from the given {@link Mood} object to the views in this ViewHolder.
         * <p>
         * <strong>Outstanding Issue:</strong> The formatted date set by
         * {@code adapter.formatDateString(mood.getUserTime())} is immediately overwritten by the unformatted
         * {@code mood.getUserTime()}. This should be fixed to ensure consistent date formatting.
         * </p>
         *
         * @param mood    The {@code Mood} object containing the data.
         * @param adapter The {@link MoodAdapter} instance to access adapter methods and listeners.
         */
        void bind(Mood mood, MoodAdapter adapter) {
            userName.setText(mood.getUserName());
            userId.setText("@" + mood.getUserId());
            if (mood.getUserLocation().equals("No location")) {
                userLocation.setVisibility(GONE);
            } else {
                userLocation.setText(mood.getUserLocation());
            }
            userTime.setText(adapter.formatDateString(mood.getUserTime())); // Format the date string
            userTime.setText(mood.getUserTime()); // Overwrites formatted date (Outstanding Issue)
            userGatheringStatus.setText(mood.getUserGatheringStatus());
            moodTrigger.setText(mood.getMoodTrigger());
            moodReason.setText(mood.getMoodReason());
            adapter.applyStyle(itemView, mood.getMoodStatus());
            moodStatus.setText(mood.getMoodStatus());

            commentButton.setOnClickListener(v -> {
                if (adapter.onCommentButtonClickListener != null) {
                    adapter.onCommentButtonClickListener.onCommentButtonClick();
                }
            });

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

            String key = mood.getOwnerUid() + "_" + mood.getMoodId();
            boolean isFollowing = adapter.followedMoodsSet.contains(key);
            trackMoodButton.setText(isFollowing ? "Following" : "Follow");

            trackMoodButton.setOnClickListener(v -> {
                if (adapter.onFollowClickListener != null) {
                    adapter.onFollowClickListener.onFollowClick(mood, isFollowing, trackMoodButton);
                }
            });

            trackMoodButton.setOnLongClickListener(v -> {
                if (adapter.onShowFollowersListener != null) {
                    adapter.onShowFollowersListener.onShowFollowers(mood);
                    return true;
                }
                return false;
            });

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
