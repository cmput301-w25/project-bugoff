// ProfileActivity.java
package com.example.project1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private TextView profileName, profileEmail;
    private Button editProfileButton, logoutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        profileImage = findViewById(R.id.profile_image);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        editProfileButton = findViewById(R.id.edit_profile_btn);
        logoutButton = findViewById(R.id.logout_btn);
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            finish();
        });


        // Load user details
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            profileName.setText(user.getDisplayName() != null ? user.getDisplayName() : "User Name");
            profileEmail.setText(user.getEmail());
        }

        // Edit Profile Action
        editProfileButton.setOnClickListener(v -> {
            // Open Edit Profile Activity
        });
    }
}