/**
 * The {@code ImageCompressor} class provides utility methods for compressing Bitmap images.
 * It allows reducing the JPEG quality and scaling down the image to ensure the resulting
 * byte array does not exceed a specified maximum size.
 *
 * Key Features:
 *
 *     Compresses Bitmap images to a JPEG byte array with a specified maximum size.
 *     Reduces JPEG quality incrementally to fit the size limit.
 *     Scales down the image dimensions if quality reduction alone is insufficient.
 *     Ensures efficient compression while maintaining acceptable image quality.
 *
 */
package com.example.whimsy;

import android.graphics.Bitmap;
import java.io.ByteArrayOutputStream;

public class ImageCompressor {

    /**
     * Compresses the given Bitmap image to a JPEG byte array with a maximum size.
     * The algorithm first reduces the JPEG quality and, if needed, scales down the image
     * to ensure the resulting byte array does not exceed maxSizeInBytes.
     *
     * @param bitmap         The Bitmap image to compress.
     * @param maxSizeInBytes The maximum allowed size in bytes (e.g., 65536 for 64KB).
     * @return A byte array representing the compressed image, or null if bitmap is null.
     */
    public static byte[] compressImage(Bitmap bitmap, int maxSizeInBytes) {
        if (bitmap == null) return null;

        int quality = 100;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

        // Reduce quality until the image size is within the limit (or quality reaches 10)
        while (outputStream.toByteArray().length > maxSizeInBytes && quality > 10) {
            outputStream.reset();
            quality -= 5;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        }

        // If quality reduction alone doesn't suffice, scale down the image and try again.
        if (outputStream.toByteArray().length > maxSizeInBytes) {
            int newWidth = (int) (bitmap.getWidth() * 0.9);
            int newHeight = (int) (bitmap.getHeight() * 0.9);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            return compressImage(scaledBitmap, maxSizeInBytes);
        }

        return outputStream.toByteArray();
    }
}
