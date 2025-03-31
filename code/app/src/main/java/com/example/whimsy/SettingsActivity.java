package com.example.whimsy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * SettingsActivity handles the user settings, including privacy settings and logout functionality.
 * It interacts with Firebase Firestore to fetch and update user settings.
 */

public class SettingsActivity extends AppCompatActivity {

    private Switch privacySwitch;
    private Button logoutButton;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI components and Firebase
        privacySwitch = findViewById(R.id.privacy_switch);
        logoutButton = findViewById(R.id.logoutButton);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load current privacy setting from Firestore
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isPrivate = documentSnapshot.getBoolean("isPrivate") != null
                            ? documentSnapshot.getBoolean("isPrivate") : false;
                    privacySwitch.setChecked(isPrivate);
                    // Set listener after initial state is loaded
                    privacySwitch.setOnCheckedChangeListener(switchListener);
                })
                .addOnFailureListener(e -> {
                    Log.e("SettingsActivity", "Error fetching privacy setting", e);
                    Toast.makeText(this, "Failed to load settings", Toast.LENGTH_SHORT).show();
                    privacySwitch.setChecked(false);
                    privacySwitch.setOnCheckedChangeListener(switchListener);
                });

        // Set logout button listener with confirmation
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(SettingsActivity.this, SignInActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Do nothing, dialog dismisses automatically
                    })
                    .show();
        });
    }
    /**
     * Listener for privacy switch changes.
     * Prompts the user for confirmation before updating the privacy setting in Firestore.
     */
    // Listener for privacy switch changes
    private final CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String message = isChecked ? "Are you sure you want to make your profile private?" :
                    "Are you sure you want to make your profile public?";
            new AlertDialog.Builder(SettingsActivity.this)
                    .setMessage(message)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Update Firestore
                        db.collection("users").document(userId)
                                .update("isPrivate", isChecked)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SettingsActivity.this, "Privacy setting updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SettingsActivity.this, "Failed to update privacy", Toast.LENGTH_SHORT).show();
                                    // Revert switch on failure
                                    privacySwitch.setOnCheckedChangeListener(null);
                                    privacySwitch.setChecked(!isChecked);
                                    privacySwitch.setOnCheckedChangeListener(switchListener);
                                });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Revert switch to previous state
                        privacySwitch.setOnCheckedChangeListener(null);
                        privacySwitch.setChecked(!isChecked);
                        privacySwitch.setOnCheckedChangeListener(switchListener);
                    })
                    .show();
        }
    };
}