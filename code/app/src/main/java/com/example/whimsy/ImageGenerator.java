/**
 * The {@code ImageGenerator} class provides methods for generating Bitmap images with various
 * properties such as text overlays, shapes, and colors. It allows creating custom images
 * programmatically for use in the application.
 *
 * Key Features:
 *
 *     Generates Bitmap images with text overlays.
 *     Supports drawing shapes like circles, rectangles, and lines.
 *     Allows setting background colors and gradients.
 *     Provides methods for customizing text properties such as font size, color, and style.
 *
 */
package com.example.whimsy;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * ImageGenerator handles the API call to generate an image using OpenAI's DALL·E API.
 * The API key is securely stored in BuildConfig.
 */
public class ImageGenerator {

    // OpenAI's image generation endpoint.
    private static final String IMAGE_GENERATION_API_URL = "https://api.openai.com/v1/images/generations";
    // API key stored in BuildConfig.
    private static final String API_KEY = BuildConfig.IMAGE_GEN_API_KEY;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client;

    public ImageGenerator() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)  // increased connect timeout
                .readTimeout(30, TimeUnit.SECONDS)     // increased read timeout
                .writeTimeout(30, TimeUnit.SECONDS)    // increased write timeout
                .callTimeout(60, TimeUnit.SECONDS)     // overall call timeout
                .build();
    }

    /**
     * Callback interface for image generation results.
     */
    public interface ImageGeneratorCallback {
        void onSuccess(String imageUrl);
        void onFailure(Exception e);
    }

    /**
     * Generates an image using OpenAI's API. Pass the prompt (without the "#generate" prefix)
     * and handle the result via the callback.
     *
     * @param prompt   The prompt for image generation.
     * @param callback The callback to deliver success or failure.
     */
    public void generateImage(String prompt, final ImageGeneratorCallback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("prompt", prompt);
            jsonBody.put("n", 1); // generate one image
            jsonBody.put("size", "1024x1024"); // you can change size as needed
        } catch (JSONException e) {
            callback.onFailure(e);
            return;
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(IMAGE_GENERATION_API_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            callback.onFailure(new IOException("Unexpected response code " + response)));
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray dataArray = jsonResponse.getJSONArray("data");
                    if (dataArray.length() > 0) {
                        String imageUrl = dataArray.getJSONObject(0).getString("url");
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(imageUrl));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(new Exception("No image returned")));
                    }
                } catch (JSONException e) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
                }
            }
        });
    }
}
