package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.auth.FirebaseAuth;



public class SignUpActivity extends AppCompatActivity {
    private Button signUpButton;
    private FirebaseAuth auth;

    private FirebaseFirestore db;
    private EditText nameEditText, emailEditText, surnameEditText, usernameEditText, passwordEditText, vehicleidEditText;
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
        vehicleidEditText = findViewById(R.id.editTextTextPersonName3);

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

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Email and password are required", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        // 1️⃣ Φτιάξε πρώτα το όχημα
                        Vehicle newVehicle = new Vehicle("", carType); // προσωρινό κενό vehicleNum
                        db.collection("vehicles").add(newVehicle)
                                .addOnSuccessListener(vehicleRef -> {
                                    String vehicleDocId = vehicleRef.getId();

                                    // 2️⃣ Φτιάξε τώρα τον user με σωστό vehicleid
                                    User newUser = new User(name, email, surname, username, password, vehicleDocId);
                                    addUserToFirestore(newUser);

                                    // 3️⃣ (προαιρετικό) ενημέρωσε το όχημα με vehicleNum = vehicleDocId
                                    vehicleRef.update("vehicleNum", vehicleDocId);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(SignUpActivity.this, "Error adding vehicle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SignUpActivity.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void addUserToFirestore(User user) {
        String uid = auth.getCurrentUser().getUid(); // get UID of newly created user
        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(aVoid -> {
                    // Success: Go to LoginActivity
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "Error adding user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void addVehicleToFirestore(Vehicle vehicle) {
        CollectionReference vehiclesRef = db.collection("vehicles");

        vehiclesRef.add(vehicle)
                .addOnSuccessListener(documentReference -> {
                    //Toast.makeText(SignUpActivity.this, "Vehicle added successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "Error adding vehicle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}