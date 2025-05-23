package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    private Button signUpButton;
    private FirebaseAuth auth;

    private EditText nameEditText, emailEditText, surnameEditText, usernameEditText, passwordEditText, vehicleNumEditText;
    private Spinner carTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        // Find views by their ID
        signUpButton = findViewById(R.id.signup);
        nameEditText = findViewById(R.id.editTextTextPersonName);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        surnameEditText = findViewById(R.id.editTextTextPersonName2);
        usernameEditText = findViewById(R.id.editTextTextPersonName4);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        carTypeSpinner = findViewById(R.id.spinner);
        vehicleNumEditText = findViewById(R.id.vehicleNum);

        // Setup the spinner with car types
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.car_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carTypeSpinner.setAdapter(adapter);

        signUpButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String surname = surnameEditText.getText().toString().trim();
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String carType = carTypeSpinner.getSelectedItem().toString();
            String vehicleNum = vehicleNumEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Email and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            User.register(name, email, surname, username, password, carType, vehicleNum,
                    aVoid -> {
                        // On success: go to LoginActivity
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    },
                    e -> {
                        Toast.makeText(SignUpActivity.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
            );
        });
    }
}