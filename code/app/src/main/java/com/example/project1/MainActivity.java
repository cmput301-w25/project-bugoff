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

package com.example.project1;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // FirebaseAuth instance for managing user authentication
    private MediaPlayer mediaPlayer; // MediaPlayer instance for playing background sound

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
        setContentView(R.layout.activity_splash); // Set the splash screen layout

        // Initialize Firebase authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the current authenticated user

        // Initialize views for the logo and slogan text
        ImageView logo = findViewById(R.id.logo);
        TextView slogan = findViewById(R.id.slogan);

        // Load animations for the logo and slogan
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce); // Bounce animation for logo
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in); // Fade-in animation for slogan

        // Set a background color transition (from white to ocean blue)
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.WHITE, Color.parseColor("#0077B6"));
        colorAnimator.setDuration(3000); // Duration of color change (3 seconds)
        colorAnimator.addUpdateListener(animator -> {
            getWindow().getDecorView().setBackgroundColor((int) animator.getAnimatedValue()); // Update background color
        });
        colorAnimator.start(); // Start the background color transition

        // Play sound of waves crashing (assumes wave_sound.mp3 is in res/raw)
        mediaPlayer = MediaPlayer.create(this, R.raw.wave_sound);
        mediaPlayer.start(); // Start playing the sound

        // Start logo bounce animation
        logo.startAnimation(bounce);

        // Make the slogan visible and start the fade-in animation
        slogan.setVisibility(TextView.VISIBLE);
        slogan.startAnimation(fadeIn);

        // Delay transitioning to the next activity (matches animation duration)
        new Handler().postDelayed(() -> {
            // If the user is authenticated, navigate to the home page
            if (currentUser != null) {
                startActivity(new Intent(MainActivity.this, HomePageActivity.class));
                finish(); // Close the current activity
            } else {
                // If not authenticated, navigate to the sign-in page
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                finish(); // Close the current activity
            }
        }, 3000); // Delay set to 3 seconds to match the animation duration
    }

    /**
     * Called when the activity is paused.
     * Releases the media player to stop the sound when the activity is no longer in the foreground.
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Stop and release the sound when the activity is paused
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
