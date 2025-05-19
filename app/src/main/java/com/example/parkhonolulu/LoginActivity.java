package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

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

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        signUpButton = findViewById(R.id.signup);
        loginManagerButton = findViewById(R.id.login2);
        loadingProgressBar = findViewById(R.id.loading);

        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        loginManagerButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, LoginManagerActivity.class));
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

            User.loginWithUsername(username, password,
                    user -> {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Welcome, " + username, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeDrawerActivity.class));
                        finish();
                    },
                    e -> {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
