package com.example.whimsy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * The {@code MoodRepository} class encapsulates all database operations related to mood entries.
 * It handles saving mood data to Firebase Firestore and uploading associated images to Firebase Storage.
 * This abstraction makes it easier to unit test database operations by mocking this class.
 */
public class MoodRepository {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth auth;
    private Context context;

    /**
     * Constructs a new {@code MoodRepository} instance.
     *
     * @param context the Context used for image decoding and accessing content resolvers.
     */
    public MoodRepository(Context context) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Callback interface for saving mood data.
     */
    public interface SaveMoodCallback {
        /**
         * Called when the mood data is saved successfully.
         */
        void onSuccess();

        /**
         * Called when there is an error saving the mood data.
         *
         * @param e the exception detailing the failure.
         */
        void onFailure(Exception e);
    }

    /**
     * Saves mood data to Firebase. If an image is provided, it uploads the image first
     * and then saves the mood data including the image URL.
     *
     * @param moodData        a {@code Map<String, Object>} containing mood details.
     * @param selectedImageUri the {@code Uri} of the selected image, or {@code null} if no image.
     * @param callback        the callback to notify about the success or failure of the operation.
     */
    public void saveMood(final Map<String, Object> moodData, final Uri selectedImageUri, final SaveMoodCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onFailure(new Exception("User not logged in"));
            return;
        }
        if (selectedImageUri != null) {
            uploadImage(selectedImageUri, moodData, callback, user);
        } else {
            addMoodToFirestore(moodData, callback, user);
        }
    }

    /**
     * Uploads an image to Firebase Storage and adds its download URL to the mood data.
     *
     * @param imageUri  the {@code Uri} of the image to upload.
     * @param moodData  the mood data map to update with the image URL.
     * @param callback  the callback to notify about the success or failure.
     * @param user      the current authenticated {@code FirebaseUser}.
     */
    private void uploadImage(final Uri imageUri, final Map<String, Object> moodData, final SaveMoodCallback callback, final FirebaseUser user) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri));
            if (bitmap == null) {
                if (callback != null) callback.onFailure(new Exception("Unable to decode image"));
                return;
            }

            // Compress the bitmap to a byte array under 64KB using the ImageCompressor class.
            byte[] compressedImageBytes = ImageCompressor.compressImage(bitmap, 65536);

            // Generate a timestamp-based file name.
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            StorageReference imageRef = storage.getReference("mood_images/" + user.getUid() + "/" + timeStamp + ".jpg");

            UploadTask uploadTask = imageRef.putBytes(compressedImageBytes);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    moodData.put("imageUrl", uri.toString());
                    addMoodToFirestore(moodData, callback, user);
                }).addOnFailureListener(e -> {
                    Log.e("MoodRepository", "Error getting download URL", e);
                    // Even on failure, proceed to add mood data without image URL.
                    addMoodToFirestore(moodData, callback, user);
                });
            }).addOnFailureListener(e -> {
                Log.e("MoodRepository", "Error uploading image", e);
                // On upload failure, add mood data without the image.
                addMoodToFirestore(moodData, callback, user);
            });
        } catch (IOException e) {
            if (callback != null) callback.onFailure(e);
        }
    }

    /**
     * Adds the mood data to Firestore.
     *
     * @param moodData the mood data to store.
     * @param callback the callback to notify about success or failure.
     * @param user     the current authenticated {@code FirebaseUser}.
     */
    private void addMoodToFirestore(final Map<String, Object> moodData, final SaveMoodCallback callback, FirebaseUser user) {
        db.collection("users").document(user.getUid()).collection("moods")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }
}
