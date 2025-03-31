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
 * ReasonGenerator handles AI text generation using OpenAI's Chat Completions API.
 * This version uses the GPT‑4 model. It sends the prompt as a user message and retrieves a response.
 * The generated text is trimmed to 185 characters if necessary.
 */
public class ReasonGenerator {

    // Chat completions endpoint for GPT‑4.
    private static final String CHAT_COMPLETIONS_API_URL = "https://api.openai.com/v1/chat/completions";
    // Specify the model you want to use; adjust if needed.
    private static final String MODEL = "gpt-4"; // Or "gpt-4-0314", etc.
    // API key stored via BuildConfig.
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
     * Generates AI-completed text from the given prompt using GPT‑4 via the Chat Completions API.
     * The request sends a system message plus the user's prompt.
     *
     * @param prompt   The prompt text (after stripping "#generate-reason").
     * @param callback Callback to receive the generated text or an error.
     */
    public void generateReason(String prompt, final ReasonGeneratorCallback callback) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", MODEL);
            // Create the messages array required for the Chat API.
            JSONArray messages = new JSONArray();
            // A system message to set context (adjust as needed).
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", "You are an assistant that completes prompts in proper English with a concise answer.");
            messages.put(systemMsg);
            // The user's prompt message.
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.put(userMsg);
            jsonBody.put("messages", messages);
            jsonBody.put("max_tokens", 50);  // Adjust token count as needed.
            jsonBody.put("temperature", 0.7);
        } catch (JSONException e) {
            callback.onFailure(e);
            return;
        }
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(CHAT_COMPLETIONS_API_URL)
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
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject message = firstChoice.getJSONObject("message");
                        String generatedText = message.getString("content").trim();
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
