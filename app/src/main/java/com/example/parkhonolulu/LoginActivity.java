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
    private Button forgotpasswordButton;
    private Button loginButton;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        signUpButton = findViewById(R.id.signup);
        forgotpasswordButton = findViewById(R.id.forgotpassword);
        loadingProgressBar = findViewById(R.id.loading);

        forgotpasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, GetEmailActivity.class);
            startActivity(intent);
        });

        signUpButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
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

            User.loginWithUsername(username, password, user -> {
                Toast.makeText(LoginActivity.this, "Welcome, " + username, Toast.LENGTH_SHORT).show();

                parking_session.fetchCurrentUserActiveSession(new parking_session.OnActiveSessionChecked() {
                    @Override
                    public void onActiveSessionFound(parking_session session) {
                        session.fetchParkingLocationLatLng(new parking_session.OnLocationFetchedListener() {
                            @Override
                            public void onSuccess(double latitude, double longitude) {
                                loadingProgressBar.setVisibility(View.GONE);
                                startActivity(new Intent(LoginActivity.this, MyparklocationActivity.class));
                            }

                            @Override
                            public void onFailure(Exception e) {
                                loadingProgressBar.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, "Failed to fetch location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                            }
                        });
                    }

                    @Override
                    public void onNoActiveSession() {
                        loadingProgressBar.setVisibility(View.GONE);
                        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                    }

                    @Override
                    public void onError(Exception e) {
                        loadingProgressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, "Error checking active session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
                    }
                });
            }, (Exception e) -> {
                loadingProgressBar.setVisibility(View.GONE);
                String msg = e.getMessage();
                if (msg.equals("Username not found")) {
                    usernameEditText.setError("Username not found");
                } else if (msg.equals("Incorrect password")) {
                    passwordEditText.setError("Incorrect password");
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}