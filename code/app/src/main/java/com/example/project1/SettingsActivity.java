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

package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

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

        Button logoutButton = findViewById(R.id.logoutButton);  // Find the logout button in the layout
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();  // Sign out the user from Firebase Authentication
            startActivity(new Intent(SettingsActivity.this, SignInActivity.class));  // Navigate to SignInActivity
            finish();  // Finish the current activity to remove it from the activity stack
        });
    }
}
