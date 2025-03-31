/**
 * MainActivity serves as the entry point of the application, displaying a splash screen
 * with animations and transitioning to the next screen based on the user's authentication status.
 *
 * This activity initializes Firebase authentication, manages splash animations, plays sound,
 * and transitions to either the HomePageActivity or SignInActivity after the splash duration.
 *
 * Outstanding Issues:
 * - None identified.
 */

package com.example.whimsy;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // FirebaseAuth instance for managing user authentication

    /**
     * Called when the activity is first created.
     * Sets up the splash screen animations, background color transition, and sound,
     * and transitions to the appropriate activity based on the user's authentication status.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the current authenticated user

        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, HomePageActivity.class));
            finish(); // Close the current activity
        } else {
            // If not authenticated, navigate to the sign-in page
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish(); // Close the current activity
        }
    }
}
