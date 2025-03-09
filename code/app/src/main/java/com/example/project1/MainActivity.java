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
    private FirebaseAuth mAuth;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize views
        ImageView logo = findViewById(R.id.logo);
        TextView slogan = findViewById(R.id.slogan);

        // Load animations
        Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Set a background color transition (from white to ocean blue)
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), Color.WHITE, Color.parseColor("#0077B6"));
        colorAnimator.setDuration(3000); // Duration of color change (3 seconds)
        colorAnimator.addUpdateListener(animator -> {
            getWindow().getDecorView().setBackgroundColor((int) animator.getAnimatedValue());
        });
        colorAnimator.start();

        // Play sound of waves crashing
        mediaPlayer = MediaPlayer.create(this, R.raw.wave_sound); // Assume you have a wave_sound.mp3 in res/raw
        mediaPlayer.start();

        // Start logo bounce animation
        logo.startAnimation(bounce);

        // Slogan fade-in animation
        slogan.setVisibility(TextView.VISIBLE);
        slogan.startAnimation(fadeIn);

        // Delay to transition to next activity after splash
        new Handler().postDelayed(() -> {
            if (currentUser != null) {
                startActivity(new Intent(MainActivity.this, HomePageActivity.class));
                finish();
            } else {
                startActivity(new Intent(MainActivity.this, SignInActivity.class));
                finish();
            }
        }, 3000); // Delay to match the duration of the animations
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the sound when the activity is paused
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
