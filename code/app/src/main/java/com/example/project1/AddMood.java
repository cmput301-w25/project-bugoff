package com.example.project1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddMood extends ActivityBase{

    private Spinner moodSpinner;
    private EditText triggerInput, reasonInput;
    private TextView timestampText;
    private Button addMoodButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.add_mood, findViewById(R.id.content_frame), true);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        moodSpinner = findViewById(R.id.moodSpinner);
        triggerInput = findViewById(R.id.triggerInput);
        reasonInput = findViewById(R.id.reasonInput);
        timestampText = findViewById(R.id.timestampText);
        addMoodButton = findViewById(R.id.addMoodButton);

        // Set up Spinner with mood options
        String[] moods = {"Happy", "Sad", "Angry", "Excited", "Calm"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodSpinner.setAdapter(adapter);

        // Set current timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a - MMMM dd, yyyy", Locale.getDefault());
        timestampText.setText(sdf.format(new Date()));

        // Add Mood button click listener
        addMoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMoodToFirebase();
            }
        });
    }

    private void saveMoodToFirebase() {
        String mood = moodSpinner.getSelectedItem().toString();
        String trigger = triggerInput.getText().toString().trim();
        String reason = reasonInput.getText().toString().trim();
        String timestamp = timestampText.getText().toString();

        // Get current user ID
        String userId = auth.getCurrentUser().getUid();

        // Create a map to store mood data
        Map<String, Object> moodData = new HashMap<>();
        moodData.put("mood", mood);
        moodData.put("trigger", trigger);
        moodData.put("reason", reason);
        moodData.put("timestamp", timestamp);

        // Save to Firestore under user's document in a "moods" subcollection
        db.collection("users").document(userId).collection("moods")
                .add(moodData)
                .addOnSuccessListener(documentReference -> {
                    // Clear fields after successful save
                    triggerInput.setText("");
                    reasonInput.setText("");
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., show a toast or log error)
                });
    }
}