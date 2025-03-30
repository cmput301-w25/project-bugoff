/**
 * SettingsActivity handles the user settings screen,
 * allowing the user to log out from the app.
 *
 * This activity includes a button that, when clicked, signs the user out
 * using Firebase Authentication and redirects the user to the sign-in screen.
 *
 * Outstanding Issues:
 * - No confirmation dialog before logging out.
 * - Does not handle potential errors during sign-out process (e.g., network issues).
 */

package com.example.whimsy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     * Initializes the logout button and sets the click listener.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);  // Set the layout for this activity

        // Initialize the privacy switch
        Switch privacySwitch = findViewById(R.id.privacy_switch);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load current privacy setting from Firestore
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isPrivate = documentSnapshot.getBoolean("isPrivate") != null
                            ? documentSnapshot.getBoolean("isPrivate") : false;
                    privacySwitch.setChecked(isPrivate);
                })
                .addOnFailureListener(e -> Log.e("SettingsActivity", "Error fetching privacy setting", e));

        // Listen for changes on the switch and update Firestore
        privacySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.collection("users").document(userId)
                    .update("isPrivate", isChecked)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(SettingsActivity.this, "Privacy setting updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(SettingsActivity.this, "Failed to update privacy", Toast.LENGTH_SHORT).show());
        });

        // Initialize logout button and set click listener
        Button logoutButton = findViewById(R.id.logoutButton);  // Find the logout button in the layout
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();  // Sign out the user from Firebase Authentication
            startActivity(new Intent(SettingsActivity.this, SignInActivity.class));  // Navigate to SignInActivity
            finish();  // Finish the current activity to remove it from the activity stack
        });
    }
}