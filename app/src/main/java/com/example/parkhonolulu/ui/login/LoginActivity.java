package com.example.parkhonolulu.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkhonolulu.HomePage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import com.example.parkhonolulu.R;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login);
        Button signUpButton = findViewById(R.id.signup);
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
                                assert email != null;
                                auth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(loginTask -> {
                                            loadingProgressBar.setVisibility(View.GONE);
                                            if (loginTask.isSuccessful()) {
                                                FirebaseUser user = auth.getCurrentUser();
                                                Toast.makeText(LoginActivity.this, "Welcome, " + username, Toast.LENGTH_SHORT).show();

                                                // Log user info
                                                String userId = document.getId(); // Firestore document ID
                                                String name = document.getString("name");
                                                String email_log = document.getString("email");
                                                String surname = document.getString("surname");
                                                String role = document.getString("role");
                                                String uid = document.getString("uid");
                                                String vehicleId = document.getString("vehicle"); // this may be a vehicle document ID
                                                String username_log = document.getString("username");

                                                // Print all fields
                                                Log.d("LoginInfo", "User Info ->");
                                                Log.d("LoginInfo", "Name: " + name);
                                                Log.d("LoginInfo", "Surname: " + surname);
                                                Log.d("LoginInfo", "Username: " + username_log);
                                                Log.d("LoginInfo", "Email: " + email_log);
                                                Log.d("LoginInfo", "Role: " + role);
                                                Log.d("LoginInfo", "UID: " + uid);
                                                Log.d("LoginInfo", "Vehicle ID: " + vehicleId);

                                                if (vehicleId != null && !vehicleId.isEmpty()) {
                                                    db.collection("vehicles").document(vehicleId).get()
                                                            .addOnSuccessListener(vehicleDocument -> {
                                                                if (vehicleDocument.exists()) {
                                                                    String vehicleNum = vehicleDocument.getString("vehicleNum");
                                                                    String carType = vehicleDocument.getString("carType");
                                                                    Log.d("LoginInfo", "Vehicle Details ->");
                                                                    Log.d("LoginInfo", "Vehicle Number: " + vehicleNum);
                                                                    Log.d("LoginInfo", "Car Type: " + carType);
                                                                } else {
                                                                    Log.d("LoginInfo", "Vehicle document not found for ID: " + vehicleId);
                                                                }
                                                                // Navigate to HomePage after attempting to log vehicle details
                                                                startActivity(new Intent(LoginActivity.this, HomePage.class));
                                                                finish(); // Finish LoginActivity
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("LoginInfo", "Error fetching vehicle details: " + e.getMessage());
                                                                // Navigate to HomePage even if vehicle details fetch fails
                                                                startActivity(new Intent(LoginActivity.this, HomePage.class));
                                                                finish(); // Finish LoginActivity
                                                            });
                                                } else {
                                                    Log.d("LoginInfo", "No Vehicle ID associated with user.");
                                                    // Navigate to HomePage if there's no vehicle ID
                                                    startActivity(new Intent(LoginActivity.this, HomePage.class));
                                                    finish(); // Finish LoginActivity
                                                }
                                                // The startActivity and finish calls are moved into the listeners above
                                                // to ensure they happen after the async vehicle fetch attempt.

                                            } else {
                                                Toast.makeText(LoginActivity.this, "Authentication failed: " + Objects.requireNonNull(loginTask.getException()).getMessage(), Toast.LENGTH_SHORT).show();
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
