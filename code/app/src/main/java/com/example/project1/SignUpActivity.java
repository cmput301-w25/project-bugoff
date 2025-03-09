package com.example.project1;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText, usernameEditText, dobEditText;
    private Button signUpButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    // Date format used for DOB display and parsing
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameEditText = findViewById(R.id.name);
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        dobEditText = findViewById(R.id.dob);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        signUpButton = findViewById(R.id.signup_btn);
        progressBar = findViewById(R.id.progressBar);
        TextView loginRedirect = findViewById(R.id.login_redirect);
        loginRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Make DOB field non-editable and show date picker on click
        dobEditText.setFocusable(false);
        dobEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    SignUpActivity.this,
                    R.style.MyDatePickerDialogTheme,  // Custom theme (for background/colors, etc.)
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        dobEditText.setText(selectedDate);
                    },
                    year,
                    month,
                    day
            );

            // Set limits for date selection
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            Calendar minDate = Calendar.getInstance();
            minDate.set(1940, Calendar.JANUARY, 1);
            datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

            datePickerDialog.show();

            // Use reflection / view lookup to set the header text (Year and Date) to white.
            try {
                // These identifiers might vary based on Android version.
                int yearId = getResources().getIdentifier("android:id/date_picker_header_year", null, null);
                int dateId = getResources().getIdentifier("android:id/date_picker_header_date", null, null);
                TextView yearTextView = datePickerDialog.findViewById(yearId);
                TextView dateTextView = datePickerDialog.findViewById(dateId);
                if (yearTextView != null) {
                    yearTextView.setTextColor(getResources().getColor(android.R.color.white));
                }
                if (dateTextView != null) {
                    dateTextView.setTextColor(getResources().getColor(android.R.color.white));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        signUpButton.setOnClickListener(v -> registerUser());

        // Real-time password matching
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
                    confirmPasswordEditText.setError("Passwords do not match!");
                    shakeView(confirmPasswordEditText);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String dobString = dobEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Input Validations
        if (name.isEmpty()) {
            nameEditText.setError("Full Name is required");
            shakeView(nameEditText);
            return;
        }
        if (username.isEmpty()) {
            usernameEditText.setError("Username is required");
            shakeView(usernameEditText);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address");
            shakeView(emailEditText);
            return;
        }
        if (dobString.isEmpty()) {
            dobEditText.setError("Date of Birth is required");
            shakeView(dobEditText);
            return;
        }
        // Validate age (must be at least 16 years old)
        try {
            Date dobDate = sdf.parse(dobString);
            Calendar dobCal = Calendar.getInstance();
            dobCal.setTime(dobDate);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            if (age < 16) {
                dobEditText.setError("You must be at least 16 years old");
                shakeView(dobEditText);
                return;
            }
        } catch (ParseException e) {
            dobEditText.setError("Invalid Date");
            shakeView(dobEditText);
            return;
        }
        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            shakeView(passwordEditText);
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match!");
            shakeView(confirmPasswordEditText);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        signUpButton.setEnabled(false);

        // Check if username is unique
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot query = task.getResult();
                        if (query != null && !query.isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            signUpButton.setEnabled(true);
                            usernameEditText.setError("Username is already taken!");
                            shakeView(usernameEditText);
                        } else {
                            // Create user in Firebase Auth
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if (user != null) {
                                                // Update Firebase User Profile (Display Name)
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(name)
                                                        .build();

                                                user.updateProfile(profileUpdates)
                                                        .addOnCompleteListener(updateTask -> {
                                                            if (updateTask.isSuccessful()) {
                                                                // Save user details to Firestore, including username and DOB
                                                                saveUserToFirestore(user.getUid(), name, username, email, dobString);
                                                            } else {
                                                                progressBar.setVisibility(View.GONE);
                                                                signUpButton.setEnabled(true);
                                                                Snackbar.make(findViewById(android.R.id.content),
                                                                        "Profile update failed",
                                                                        Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                                                            }
                                                        });
                                            }
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            signUpButton.setEnabled(true);
                                            Snackbar.make(findViewById(android.R.id.content),
                                                    "Registration failed: " + Objects.requireNonNull(authTask.getException()).getMessage(),
                                                    Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                                        }
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        signUpButton.setEnabled(true);
                        Snackbar.make(findViewById(android.R.id.content),
                                "Error checking username uniqueness: " + Objects.requireNonNull(task.getException()).getMessage(),
                                Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String username, String email, String dob) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("username", username);
        user.put("email", email);
        user.put("dob", dob);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(findViewById(android.R.id.content),
                            "User registered successfully!",
                            Snackbar.LENGTH_SHORT).setBackgroundTint(getResources().getColor(R.color.dark_green)).setTextColor(Color.WHITE).show();
                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    signUpButton.setEnabled(true);
                    Snackbar.make(findViewById(android.R.id.content),
                            "Firestore error: " + e.getMessage(),
                            Snackbar.LENGTH_SHORT).setBackgroundTint(Color.RED).setTextColor(Color.WHITE).show();
                });
    }

    // Shake animation for invalid fields
    private void shakeView(View view) {
        Animation shake = new TranslateAnimation(0, 15, 0, 0);
        shake.setDuration(120);
        shake.setRepeatCount(5);
        shake.setRepeatMode(Animation.REVERSE);
        view.startAnimation(shake);
    }
}
