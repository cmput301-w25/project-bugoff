/**
 * SplashActivity displays a splash screen with logo animations
 * before transitioning to the MainActivity.
 *
 * This activity loads and applies two animations (scale-up and fade-in)
 * to the logo image and waits for a brief delay before launching
 * the MainActivity.
 *
 * Outstanding Issues:
 * - No checks for user login status; could be useful for redirecting based on authentication state.
 * - Hardcoded delay for splash duration; should ideally be based on animation duration or user preference.
 */
package com.example.project1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SplashActivity extends Activity {
    /**
     * Called when the activity is first created.
     * Initializes the splash screen, applies animations to the logo,
     * and sets a delay before transitioning to the MainActivity.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Set the splash screen layout

        // 🔹 Get the logo ImageView from the layout
        ImageView logo = findViewById(R.id.logo);

        // 🔹 Load the animations
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up); // Scale-up animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);   // Fade-in animation

        // 🔹 Apply both animations to the logo
        logo.startAnimation(scaleUp); // Start the scale-up animation
        logo.startAnimation(fadeIn);  // Start the fade-in animation

        // 🔹 Set a delay before launching MainActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class)); // Start MainActivity
            finish(); // Close the SplashActivity
        }, 2000); // 2 seconds delay before transitioning
    }
}
