package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressBar loadingProgressBar;
    private Button loginButton;
    private Button loginManagerButton;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        signUpButton = findViewById(R.id.signup);
        loadingProgressBar = findViewById(R.id.loading);
        loginManagerButton = findViewById(R.id.login2);

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        loginManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, LoginManagerActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty()) {
                usernameEditText.setError("Username required");
                loadingProgressBar.setVisibility(View.GONE);
                return;
            }

            if (password.isEmpty()) {
                passwordEditText.setError("Password required");
                loadingProgressBar.setVisibility(View.GONE);
                return;
            }

            // Βρες το email που αντιστοιχεί στο username
            db.collection("users") // υπόθεση ότι οι χρήστες αποθηκεύονται εδώ
                    .whereEqualTo("username", username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String email = document.getString("email");

                                // Κάνε login με email
                                auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(loginTask -> {
                                            loadingProgressBar.setVisibility(View.GONE);
                                            if (loginTask.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Welcome, " + username, Toast.LENGTH_SHORT).show();
                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                if (user != null) {
                                                    // Proceed to home drawer page if user is already logged in
                                                    startActivity(new Intent(LoginActivity.this, HomeDrawerActivity.class));
                                                }

                                            } else {
                                                Toast.makeText(LoginActivity.this, "Authentication failed: " + loginTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FIRESTORE_ERROR", "Error retrieving data", e);
                                            loadingProgressBar.setVisibility(View.GONE);
                                            Toast.makeText(LoginActivity.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
                                        });

                                break;
                            }
                        } else {
                            loadingProgressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
