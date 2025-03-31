package com.example.whimsy;

import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ImageCompressorTest {

    @Test
    public void testCompressImageReturnsNonNull() {
        Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
        byte[] compressed = ImageCompressor.compressImage(bitmap, 65536); // 64KB
        assertNotNull(compressed);
    }

    @Test
    public void testCompressedImageIsUnderMaxSize() {
        Bitmap bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        int maxSize = 65536; // 64KB
        byte[] compressed = ImageCompressor.compressImage(bitmap, maxSize);
        assertNotNull(compressed);
        assertTrue("Compressed image should not exceed max size", compressed.length <= maxSize);
    }

    @Test
    public void testCompressionHandlesTinyLimit() {
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        int maxSize = 1024; // 1KB
        byte[] compressed = ImageCompressor.compressImage(bitmap, maxSize);
        assertNotNull(compressed);
        assertTrue("Compressed image should be within 1KB", compressed.length <= maxSize);
    }

    @Test
    public void testNullBitmapReturnsNull() {
        byte[] result = ImageCompressor.compressImage(null, 65536);
        assertNull(result);
    }
}