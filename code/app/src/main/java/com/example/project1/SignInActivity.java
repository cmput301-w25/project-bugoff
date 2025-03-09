/**
 * SignInActivity handles user authentication via Firebase,
 * allowing the user to sign in with their email and password.
 * It also provides navigation for users who need to sign up or reset their password.
 *
 * The activity includes:
 * - Firebase authentication for signing in.
 * - Network connectivity check before attempting to log in.
 * - A dialog to reset the password.
 *
 * Outstanding Issues:
 * - No handling of different error states for network or Firebase sign-in failures.
 * - Does not provide feedback for wrong credentials apart from a generic failure message.
 */

package com.example.project1;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;

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
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Check for network availability before attempting login
        if (!isNetworkAvailable()) {
            Toast.makeText(SignInActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if the email and password fields are not empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);  // Show progress bar during login attempt

        // Attempt to sign in using FirebaseAuth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserData(user.getUid());  // Fetch user data if sign-in is successful
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);  // Hide progress bar if login fails
                        Toast.makeText(SignInActivity.this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Fetches additional user data from Firestore after successful sign-in.
     *
     * @param userId The ID of the user.
     */
    private void fetchUserData(String userId) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);  // Hide progress bar after fetching user data
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        Intent intent = new Intent(SignInActivity.this, HomePageActivity.class);
                        startActivity(intent);  // Redirect to HomePageActivity
                        finish();  // Close this activity
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);  // Hide progress bar on failure
                    Toast.makeText(SignInActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SignInActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Password reset email sent", Toast.LENGTH_LONG).show();
                            dialog.dismiss();  // Close the dialog on success
                        } else {
                            Toast.makeText(SignInActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();  // Check network connection
    }
}
