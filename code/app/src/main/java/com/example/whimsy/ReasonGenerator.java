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
 * ReasonGenerator handles text completion using OpenAI's completions endpoint.
 * It generates AI-completed text based on a prompt, ensuring the output is under 185 characters.
 */
public class ReasonGenerator {
    // OpenAI completions endpoint.
    private static final String COMPLETIONS_API_URL = "https://api.openai.com/v1/completions";
    // Using a model like "text-davinci-003" for text completion.
    private static final String MODEL = "text-davinci-003";
    // Use the same API key from BuildConfig (or store a separate one if needed).
    private static final String API_KEY = BuildConfig.IMAGE_GEN_API_KEY;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public ReasonGenerator() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public interface ReasonGeneratorCallback {
        void onSuccess(String generatedText);
        void onFailure(Exception e);
    }

    /**
     * Generates AI-completed text from the given prompt.
     * @param prompt The prompt text (after "#generate-reason" is stripped).
     * @param callback Callback to return the generated text.
     */
    public void generateReason(String prompt, final ReasonGeneratorCallback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", MODEL);
            jsonBody.put("prompt", prompt);
            jsonBody.put("max_tokens", 50); // Adjust as needed.
            jsonBody.put("temperature", 0.7);
        } catch (JSONException e) {
            callback.onFailure(e);
            return;
        }
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(COMPLETIONS_API_URL)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .post(requestBody)
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
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        String generatedText = choices.getJSONObject(0).getString("text").trim();
                        if (generatedText.length() > 185) {
                            generatedText = generatedText.substring(0, 185);
                        }
                        String finalGeneratedText = generatedText;
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(finalGeneratedText));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(new Exception("No text generated")));
                    }
                } catch (JSONException e) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e));
                }
            }
        });
    }
}
