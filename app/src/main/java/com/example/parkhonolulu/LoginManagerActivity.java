package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginManagerActivity extends AppCompatActivity {

    private EditText EmailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LoginManagerActivity", "Activity started");
        setContentView(R.layout.activity_loginmanager);

        // Initialize views
        EmailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);

        // Adding text watchers to validate input fields
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateLoginForm();
            }
        };

        // Attach listeners to the email and password fields
        EmailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        // Handle the action when the 'Done' key is pressed on the keyboard
        passwordEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        // Handle login button click
        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void validateLoginForm() {
        String email = EmailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        // Enable the login button only if both email and password are entered
        loginButton.setEnabled(!email.isEmpty() && !password.isEmpty());
    }

    private void attemptLogin() {
        String email = EmailEditText.getText().toString().trim().toLowerCase();
        String password = passwordEditText.getText().toString().trim();

        // Log email for debugging purposes
        Log.d("LOGIN_DEBUG", "Email entered: '" + email + "'");
        Log.d("LOGIN_DEBUG", "Trimmed: '" + email.trim().toLowerCase() + "'");

        // Show the loading spinner while the login process is ongoing
        loadingProgressBar.setVisibility(View.VISIBLE);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        // Login successful, proceed to fetch the manager's phone number
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Log.d("LoginManagerActivity", "Querying for email: " + email);

                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser == null) {
                            Log.e("LoginManagerActivity", "No user is logged in.");
                            return;  // Early return if no user is authenticated
                        }

                        db.collection("Manager")
                                .whereEqualTo("Email", email)
                                .whereEqualTo("role", "Manager")
                                .get()
                                .addOnCompleteListener(task -> {
                                    loadingProgressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().isEmpty()) {
                                            // Query returned some results
                                            Intent intent = new Intent(this, ManagerAuthActivity.class);
                                            intent.putExtra("email", "elenitsotsou2004@gmail.com"); // Pass the email to the next activity
                                            startActivity(intent);
                                        } else {
                                            Log.d("FIRESTORE_DEBUG", "No matching documents found for email: " + email);
                                            Toast.makeText(this, "No Manager role found for this email.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        // Log any errors with the query
                                        Log.e("Firestore", "Error fetching manager", task.getException());
                                        Toast.makeText(this, "Error fetching manager data.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    loadingProgressBar.setVisibility(View.GONE);
                                    Log.e("Firestore", "Error fetching manager", e);
                                    Toast.makeText(this, "Error fetching manager data.", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        loadingProgressBar.setVisibility(View.GONE);
                        // Show error message if login fails
                        Toast.makeText(this, "Login failed: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

