package com.example.project1;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get logo ImageView
        ImageView logo = findViewById(R.id.logo);

        // Load animations
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Start animations
        logo.startAnimation(scaleUp);
        logo.startAnimation(fadeIn);

        // Delay before switching to main activity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000); // 2 seconds delay
    }
}

