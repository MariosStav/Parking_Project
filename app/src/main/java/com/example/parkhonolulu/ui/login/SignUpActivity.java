package com.example.parkhonolulu.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkhonolulu.R;
import com.example.parkhonolulu.User;
import com.example.parkhonolulu.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private Button signUpButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText nameEditText, emailEditText, surnameEditText, usernameEditText, passwordEditText, vehicleNumEditText;
    private Spinner carTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signUpButton = findViewById(R.id.signup);
        nameEditText = findViewById(R.id.editTextTextPersonName);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        surnameEditText = findViewById(R.id.editTextTextPersonName2);
        usernameEditText = findViewById(R.id.editTextTextPersonName4);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        vehicleNumEditText = findViewById(R.id.editTextTextPersonName3); // ← you must set correct ID in XML
        carTypeSpinner = findViewById(R.id.spinner);

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
            String vehicleNum = vehicleNumEditText.getText().toString().trim();
            String carType = carTypeSpinner.getSelectedItem().toString();

            if (email.isEmpty() || password.isEmpty() || vehicleNum.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            String role = "user";

                            Vehicle vehicle = new Vehicle(vehicleNum, carType);

                            // Save vehicle first
                            db.collection("vehicles").add(vehicle)
                                    .addOnSuccessListener(vehicleRef -> {
                                        String vehicleId = vehicleRef.getId();

                                        User newUser = new User(name, surname, username, email, role, uid, vehicleId);

                                        // Save user with vehicle reference
                                        db.collection("users").document(uid).set(newUser)
                                                .addOnSuccessListener(aVoid -> {
                                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(SignUpActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(SignUpActivity.this, "Error saving vehicle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SignUpActivity.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
