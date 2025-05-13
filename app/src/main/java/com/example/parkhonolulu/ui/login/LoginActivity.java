package com.example.parkhonolulu.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkhonolulu.HomePage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.example.parkhonolulu.R;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressBar loadingProgressBar;
    private Button loginButton;
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

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
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
                                                FirebaseUser user = auth.getCurrentUser();
                                                Toast.makeText(LoginActivity.this, "Welcome, " + username, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(LoginActivity.this, HomePage.class));
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Authentication failed: " + loginTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
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
