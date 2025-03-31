/**
 * SignInActivity handles user authentication via Firebase,
 * allowing the user to sign in with their email and password.
 * It also provides navigation for users who need to sign up or reset their password.
 * The activity includes:
 * - Firebase authentication for signing in.
 * - Network connectivity check before attempting to log in.
 * - A dialog to reset the password.
 * Outstanding Issues:
 * - No handling of different error states for network or Firebase sign-in failures.
 * - Does not provide feedback for wrong credentials apart from a generic failure message.
 */

package com.example.whimsy;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * SignInActivity allows users to sign in with their email and password.
 */
public class SignInActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;  // Input fields for email and password
    private Button loginButton;  // Button for triggering login
    private ProgressBar progressBar;  // Progress bar shown during login attempt
    private FirebaseAuth mAuth;  // Firebase authentication instance
    private FirebaseFirestore db;  // Firestore database instance

    /**
     * Called when the activity is first created.
     * Initializes the necessary views and sets up click listeners.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);  // Set the layout for the activity

        mAuth = FirebaseAuth.getInstance();  // Initialize FirebaseAuth instance
        db = FirebaseFirestore.getInstance();  // Initialize Firestore instance

        // Find UI components by their IDs
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        loginButton = findViewById(R.id.signIn_btn);

        // Setup redirect to SignUpActivity
        TextView loginRedirect = findViewById(R.id.login_redirect);
        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);  // Redirect to SignUpActivity
        });

        // Set click listener to attempt login
        loginButton.setOnClickListener(v -> loginUser());

        // Setup "Forgot password" link to show dialog
        TextView forgotPasswordText = findViewById(R.id.forgot_password);
        forgotPasswordText.setOnClickListener(v -> showForgotPasswordDialog());
    }
  
    /**
     * Attempts to sign in the user with the email and password provided.
     */
    void loginUser() {
        String identifier = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Check for network availability before attempting login
        if (!isNetworkAvailable()) {
            Snackbar.make(findViewById(android.R.id.content),
                            "No internet connection", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
            return;
        }

        // For empty identifier or password, set errors and apply shake animation.
        if (TextUtils.isEmpty(identifier) || TextUtils.isEmpty(password)) {
            if (TextUtils.isEmpty(identifier)) {
                emailEditText.setError("Email or username is required");
                shakeView(emailEditText);
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
                shakeView(passwordEditText);
            }
            return;
        }

        progressBar.setVisibility(View.VISIBLE);  // Show progress bar during login attempt
      
        // Check if the identifier is a valid email address.
        if (Patterns.EMAIL_ADDRESS.matcher(identifier).matches()) {
            // Use identifier as email.
            mAuth.signInWithEmailAndPassword(identifier, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                fetchUserData(user.getUid());
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Snackbar.make(findViewById(android.R.id.content),
                                            "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                            Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                        }
                    });
        } else {
            // Otherwise, treat the identifier as a username.
            db.collection("users")
                    .whereEqualTo("username", identifier)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                // Get the associated email from the first matching document.
                                String email = task.getResult().getDocuments().get(0).getString("email");
                                if (email == null) {
                                    progressBar.setVisibility(View.GONE);
                                    Snackbar.make(findViewById(android.R.id.content),
                                                    "No email associated with this username", Snackbar.LENGTH_SHORT)
                                            .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                                    return;
                                }
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(authTask -> {
                                            if (authTask.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (user != null) {
                                                    fetchUserData(user.getUid());
                                                }
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                Snackbar.make(findViewById(android.R.id.content),
                                                                "Login failed: " + Objects.requireNonNull(authTask.getException()).getMessage(),
                                                                Snackbar.LENGTH_SHORT)
                                                        .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                                            }
                                        });
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Snackbar.make(findViewById(android.R.id.content),
                                                "Username not found", Snackbar.LENGTH_SHORT)
                                        .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Snackbar.make(findViewById(android.R.id.content),
                                            "Error checking username: " + Objects.requireNonNull(task.getException()).getMessage(),
                                            Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                        }
                    });
        }
    }

    void fetchUserData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);  // Hide progress bar after fetching user data
                    if (documentSnapshot.exists()) {
                        Intent intent = new Intent(SignInActivity.this, HomePageActivity.class);
                        startActivity(intent);  // Redirect to HomePageActivity
                        finish();  // Close this activity
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content),
                                    "Error fetching data: " + e.getMessage(), Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                });
    }

    /**
     * Displays a dialog for the user to enter their email and reset their password.
     */
    private void showForgotPasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);  // Remove title from dialog
        dialog.setContentView(R.layout.dialog_forgot_password);  // Set the content view for the dialog
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);  // Transparent background

        EditText emailEditText = dialog.findViewById(R.id.emailEditText);
        Button sendButton = dialog.findViewById(R.id.sendButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        // Send password reset email when button is clicked
        sendButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content),
                                "Please enter your email", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                return;
            }

            // Check if the email exists in the database before sending the reset email.
            db.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null && !task.getResult().isEmpty()) {
                                // Email exists, send password reset email.
                                mAuth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener(resetTask -> {
                                            if (resetTask.isSuccessful()) {
                                                Snackbar.make(findViewById(android.R.id.content),
                                                                "Password reset email sent", Snackbar.LENGTH_LONG)
                                                        .setBackgroundTint(Color.parseColor("#006400")).setTextColor(Color.WHITE).show();
                                                dialog.dismiss();
                                            } else {
                                                Snackbar.make(findViewById(android.R.id.content),
                                                                "Error: " + Objects.requireNonNull(resetTask.getException()).getMessage(),
                                                                Snackbar.LENGTH_LONG)
                                                        .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                                            }
                                        });
                            } else {
                                // Email not found.
                                Snackbar.make(findViewById(android.R.id.content),
                                                "Email not found", Snackbar.LENGTH_SHORT)
                                        .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                            }
                        } else {
                            Snackbar.make(findViewById(android.R.id.content),
                                            "Error checking email: " + Objects.requireNonNull(task.getException()).getMessage(),
                                            Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                        }
                    });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());  // Close the dialog when cancel is clicked

        dialog.show();  // Show the dialog
    }

    /**
     * Checks whether there is an active network connection.
     *
     * @return True if the device is connected to the internet, otherwise false.
     */
    boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();  // Check network connection
    }

    // Shake animation method similar to SignUpActivity
    void shakeView(View view) {
        Animation shake = new TranslateAnimation(0, 15, 0, 0);
        shake.setDuration(120);
        shake.setRepeatCount(5);
        shake.setRepeatMode(Animation.REVERSE);
        view.startAnimation(shake);
    }
}
