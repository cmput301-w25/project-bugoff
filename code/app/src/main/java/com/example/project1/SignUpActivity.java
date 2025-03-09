/**
 * SignUpActivity handles the user registration process,
 * including input validation, Firebase Authentication for account creation,
 * and Firestore for saving user details.
 *
 * This activity provides real-time validation for email and password fields,
 * and visual feedback for errors using animations and Toast messages.
 *
 * Outstanding Issues:
 * - No handling for password strength beyond length check.
 * - No checks for duplicate users in Firestore.
 * - No retry logic for network or Firebase errors.
 */
package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText;
    private Button signUpButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     * Initializes UI elements and sets up event listeners.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up); // Set the layout for this activity

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        nameEditText = findViewById(R.id.name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        signUpButton = findViewById(R.id.signup_btn);
        progressBar = findViewById(R.id.progressBar);

        // Set up redirect to SignInActivity
        TextView loginRedirect = findViewById(R.id.login_redirect);
        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent); // Navigate to SignInActivity
        });

        // Set up sign up button click listener
        signUpButton.setOnClickListener(v -> registerUser());

        // Real-time password matching
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Check if passwords match in real-time
                if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
                    confirmPasswordEditText.setError("Passwords do not match!");
                    shakeView(confirmPasswordEditText); // Trigger shake animation on mismatch
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    /**
     * Registers the user by validating inputs, creating an account via Firebase Auth,
     * updating the user's profile, and saving details to Firestore.
     */
    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // 🔹 Input Validations
        if (name.isEmpty()) {
            nameEditText.setError("Full Name is required");
            shakeView(nameEditText); // Trigger shake animation on invalid input
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            shakeView(emailEditText); // Trigger shake animation on invalid email
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            shakeView(passwordEditText); // Trigger shake animation on invalid password
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match!");
            shakeView(confirmPasswordEditText); // Trigger shake animation on password mismatch
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Show progress bar while processing
        signUpButton.setEnabled(false); // Disable sign-up button during the process

        // 🔹 Create user in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 🔹 Update Firebase User Profile (Display Name)
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // 🔹 Save user details to Firestore
                                            saveUserToFirestore(user.getUid(), name, email);
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            signUpButton.setEnabled(true);
                                            Toast.makeText(SignUpActivity.this, "Profile update failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        signUpButton.setEnabled(true);
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Saves the user details to Firestore after successful account creation and profile update.
     *
     * @param userId The user ID generated by Firebase Authentication.
     * @param name The full name of the user.
     * @param email The email address of the user.
     */
    private void saveUserToFirestore(String userId, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);

        // Save user details to Firestore
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar after saving
                    Toast.makeText(SignUpActivity.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class)); // Redirect to SignInActivity
                    finish(); // Finish the SignUpActivity
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar on error
                    signUpButton.setEnabled(true); // Re-enable sign-up button
                    Toast.makeText(SignUpActivity.this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Applies a shaking animation to a view when an invalid input is detected.
     *
     * @param view The view that will be animated.
     */
    private void shakeView(View view) {
        Animation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(300); // Duration of the shake animation
        shake.setRepeatCount(3); // Number of shakes
        shake.setRepeatMode(Animation.REVERSE); // Revert back to original position
        view.startAnimation(shake); // Start the shake animation
    }
}
