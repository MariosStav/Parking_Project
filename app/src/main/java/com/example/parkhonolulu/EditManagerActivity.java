package com.example.parkhonolulu;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class EditManagerActivity extends BaseManagerDrawerActivity {

    private EditText editTextName, editTextSurname, editTextEmail, editTextPhone;
    private Button buttonSave, buttonEdit, buttonChangePassword;

    private Manager currentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupManagerDrawer(R.layout.drawer_base_manager);
        getLayoutInflater().inflate(R.layout.activity_edit_manager_profile, findViewById(R.id.content_frame), true);

        editTextName = findViewById(R.id.editTextTextPersonName);
        editTextSurname = findViewById(R.id.managername);
        editTextEmail = findViewById(R.id.editTextTextPersonName4);
        editTextPhone = findViewById(R.id.vehicleNum);
        buttonChangePassword = findViewById(R.id.changepassword);
        buttonSave = findViewById(R.id.button_save_manager);
        buttonEdit = findViewById(R.id.button_edit_manager);

        setEditable(false);
        buttonSave.setEnabled(false);

        loadManagerData();

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(EditManagerActivity.this, "Password reset email sent", Toast.LENGTH_LONG).show();
                        });
            }
        });

        buttonEdit.setOnClickListener(v -> {
            setEditable(true);
            buttonSave.setEnabled(true);
            buttonEdit.setEnabled(false);
        });

        buttonSave.setOnClickListener(v -> {
            updateManagerFromFields();
            buttonEdit.setEnabled(true);
            currentManager.saveToDatabase(
                    unused -> {
                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                        setEditable(false);
                        buttonSave.setEnabled(false);
                    },
                    e -> Toast.makeText(this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });
    }

    private void loadManagerData() {
        Manager.fetchCurrentManager(
                manager -> {
                    currentManager = manager;
                    fillFields(manager);
                },
                e -> Toast.makeText(this, "Failed to load manager: " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void fillFields(Manager manager) {
        editTextName.setText(manager.getName());
        editTextSurname.setText(manager.getSurname());
        editTextEmail.setText(manager.getEmail());
        editTextPhone.setText(manager.getPhone());
    }

    private void updateManagerFromFields() {
        currentManager.setName(editTextName.getText().toString());
        currentManager.setSurname(editTextSurname.getText().toString());
        currentManager.setPhone(editTextPhone.getText().toString());
    }

    private void setEditable(boolean editable) {
        editTextName.setEnabled(editable);
        editTextSurname.setEnabled(editable);
        editTextPhone.setEnabled(editable);
        buttonChangePassword.setEnabled(editable);
        // Email shouldn't be editable
        editTextEmail.setEnabled(false);
    }
}
