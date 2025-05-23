package com.example.parkhonolulu;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.parkhonolulu.BaseDrawerActivity;
import com.example.parkhonolulu.User;
import com.example.parkhonolulu.Vehicle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class EditUserActivity extends BaseDrawerActivity {

    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText emailEditText;
    private EditText vehicleNumberEditText;
    private Spinner carTypeSpinner;
    private EditText usernameEditText;
    private Button editButton;
    private Button changeButton;
    private TextView carTypeLabel;
    private User currentUser;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_edit_user, findViewById(R.id.content_frame), true);

        // Initialize views
        nameEditText = findViewById(R.id.editTextTextPersonName);
        surnameEditText = findViewById(R.id.editTextTextPersonName2);
        emailEditText = findViewById(R.id.editTextTextEmailAddress);
        vehicleNumberEditText = findViewById(R.id.vehicleNum);
        carTypeSpinner = findViewById(R.id.spinner);
        usernameEditText = findViewById(R.id.editTextTextPersonName4);
        editButton = findViewById(R.id.button_edit_manager3);
        carTypeLabel = findViewById(R.id.textView5);
        saveButton = findViewById(R.id.button_save_manager3);
        changeButton = findViewById(R.id.changepassword3);

        // Setup Spinner Adapter
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.car_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carTypeSpinner.setAdapter(adapter);

        // Disable editing initially
        setEditable(false);
        editButton.setEnabled(true);
        saveButton.setEnabled(false);

        loadUserData();

        changeButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(EditUserActivity.this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> Toast.makeText(EditUserActivity.this, "Password reset email sent", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(EditUserActivity.this, "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });

        editButton.setOnClickListener(v -> {
            setEditable(true);
            editButton.setEnabled(false);
            saveButton.setEnabled(true);
        });

        saveButton.setOnClickListener(v -> {
            saveButton.setEnabled(false);

            updateUserFromFields();

            if (currentUser == null) {
                Toast.makeText(EditUserActivity.this, "User not loaded", Toast.LENGTH_SHORT).show();
                saveButton.setEnabled(true);
                return;
            }

            // The existing vehicle document ID
            String vehicleId = currentUser.getVehicleid();
            String newVehicleNum = vehicleNumberEditText.getText().toString().trim();
            String newCarType = carTypeSpinner.getSelectedItem().toString();

            if (vehicleId != null && !vehicleId.isEmpty()) {
                // Vehicle document exists, fetch and update
                Vehicle.fetchVehicle(vehicleId,
                        vehicle -> {
                            if (vehicle != null) {
                                boolean needUpdate = false;

                                // Update vehicleNum if changed
                                if (!newVehicleNum.equals(vehicle.getVehicleNum())) {
                                    vehicle.updateVehicleNum(newVehicleNum,
                                            aVoid -> {
                                                // Successfully updated vehicleNum
                                            },
                                            e -> {
                                                runOnUiThread(() -> Toast.makeText(EditUserActivity.this, "Failed to update vehicle number: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                            });
                                    needUpdate = true;
                                }

                                // Update carType if changed
                                if (!newCarType.equalsIgnoreCase(vehicle.getCarType())) {
                                    vehicle.updateCarType(newCarType,
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    // Handle success, e.g. log or update UI
                                                    System.out.println("CarType updated successfully");
                                                }
                                            },
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Handle failure
                                                    System.err.println("Failed to update CarType: " + e.getMessage());
                                                }
                                            }
                                    );

                                }

                                // After updating vehicle info, save user
                                saveUserAndFinish();
                            } else {
                                // Vehicle doc not found, create new vehicle and update user.vehicleid
                                Vehicle newVehicle = new Vehicle(newVehicleNum, newCarType);
                                newVehicle.saveToDatabase(aVoid -> {
                                    // Update user's vehicleid to the new vehicle doc ID
                                    currentUser.setVehicleid(newVehicle.getId());

                                    saveUserAndFinish();
                                }, e -> {
                                    runOnUiThread(() -> {
                                        Toast.makeText(EditUserActivity.this, "Failed to create new vehicle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        saveButton.setEnabled(true);
                                    });
                                });
                            }
                        },
                        e -> {
                            // Fetch vehicle failed, create new vehicle
                            Vehicle newVehicle = new Vehicle(newVehicleNum, newCarType);
                            newVehicle.saveToDatabase(aVoid -> {
                                currentUser.setVehicleid(newVehicle.getId());
                                saveUserAndFinish();
                            }, err -> {
                                runOnUiThread(() -> {
                                    Toast.makeText(EditUserActivity.this, "Failed to create vehicle: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                                    saveButton.setEnabled(true);
                                });
                            });
                        });
            } else {
                // No vehicleId associated yet, create new vehicle and save user
                Vehicle newVehicle = new Vehicle(newVehicleNum, newCarType);
                newVehicle.saveToDatabase(aVoid -> {
                    currentUser.setVehicleid(newVehicle.getId());
                    saveUserAndFinish();
                }, e -> {
                    runOnUiThread(() -> {
                        Toast.makeText(EditUserActivity.this, "Failed to create vehicle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        saveButton.setEnabled(true);
                    });
                });
            }
        });
    }

    private void saveUserAndFinish() {
        currentUser.saveToDatabase(aVoid -> {
            runOnUiThread(() -> {
                Toast.makeText(EditUserActivity.this, "User saved successfully", Toast.LENGTH_SHORT).show();
                setEditable(false);
                editButton.setEnabled(true);
                saveButton.setEnabled(false);
            });
        }, e -> {
            runOnUiThread(() -> {
                Toast.makeText(EditUserActivity.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                saveButton.setEnabled(true);
            });
        });
    }

    private void setEditable(boolean editable) {
        nameEditText.setEnabled(editable);
        surnameEditText.setEnabled(editable);
        emailEditText.setEnabled(editable);
        vehicleNumberEditText.setEnabled(editable);
        carTypeSpinner.setEnabled(editable);
        usernameEditText.setEnabled(editable);
        changeButton.setEnabled(editable);
    }

    private void loadUserData() {
        User.fetchCurrentUser(user -> {
            currentUser = user;
            fillUserFields(user);

            String vehicleId = user.getVehicleid();
            if (vehicleId != null && !vehicleId.isEmpty()) {
                Vehicle.fetchVehicle(vehicleId,
                        vehicle -> {
                            if (vehicle != null) {
                                vehicleNumberEditText.setText(vehicle.getVehicleNum());
                                setCarTypeSpinnerSelection(vehicle.getCarType());
                            } else {
                                vehicleNumberEditText.setText("");
                                setCarTypeSpinnerSelection(null);
                            }
                        },
                        e -> {
                            vehicleNumberEditText.setText("");
                            setCarTypeSpinnerSelection(null);
                            Toast.makeText(this, "Failed to load vehicle: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                vehicleNumberEditText.setText("");
                setCarTypeSpinnerSelection(null);
            }
        }, e -> Toast.makeText(this, "Failed to load user: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void fillUserFields(User user) {
        nameEditText.setText(user.getName());
        surnameEditText.setText(user.getSurname());
        emailEditText.setText(user.getEmail());
        usernameEditText.setText(user.getUsername());
    }

    private void setCarTypeSpinnerSelection(String carType) {
        if (carType == null) {
            carTypeSpinner.setSelection(0);
            return;
        }

        for (int i = 0; i < carTypeSpinner.getCount(); i++) {
            Object item = carTypeSpinner.getItemAtPosition(i);
            if (item != null && item.toString().equalsIgnoreCase(carType)) {
                carTypeSpinner.setSelection(i);
                return;
            }
        }
        carTypeSpinner.setSelection(0);
    }

    private void updateUserFromFields() {
        currentUser.setName(nameEditText.getText().toString().trim());
        currentUser.setSurname(surnameEditText.getText().toString().trim());
        currentUser.setEmail(emailEditText.getText().toString().trim());
        currentUser.setUsername(usernameEditText.getText().toString().trim());
        // DO NOT update vehicleid here — it’s managed separately when vehicle created/updated
    }
}
