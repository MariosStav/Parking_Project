package com.example.parkhonolulu.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.parkhonolulu.R;


import androidx.appcompat.app.AppCompatActivity;

import com.example.parkhonolulu.ui.login.LoginActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.auth.FirebaseAuth;
import com.example.parkhonolulu.model.User;


public class SignUpActivity extends AppCompatActivity {
    private Button signUpButton;
    private FirebaseAuth auth;

    private FirebaseFirestore db;
    private EditText nameEditText, emailEditText, surnameEditText, usernameEditText, passwordEditText;
    private Spinner carTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Find views by their ID
        signUpButton = findViewById(R.id.signup);
        nameEditText = findViewById(R.id.editTextTextPersonName);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        surnameEditText = findViewById(R.id.editTextTextPersonName2);
        usernameEditText = findViewById(R.id.editTextTextPersonName4);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        carTypeSpinner = findViewById(R.id.spinner);

        // Setup the spinner with car types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.car_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carTypeSpinner.setAdapter(adapter);

        signUpButton.setOnClickListener(v -> {
            // Get user input
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String carType = carTypeSpinner.getSelectedItem().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Email and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        User newUser = new User(name, email, surname, username, password);
                        addUserToFirestore(newUser);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SignUpActivity.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void addUserToFirestore(User user) {
        // Reference to the "users" collection in Firestore
        CollectionReference usersRef = db.collection("users");

        // Add the user document to the collection
        usersRef.add(user)
                .addOnSuccessListener(documentReference -> {
                    // Success: Go to LoginActivity
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    // Failure: Handle error
                    Toast.makeText(SignUpActivity.this, "Error adding user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
