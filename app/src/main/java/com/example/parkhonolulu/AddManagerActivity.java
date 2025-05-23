package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddManagerActivity extends BaseManagerDrawerActivity {
    private EditText emailEditText, passwordEditText, phoneEditText, nameEditText, surnameEditText;
    private Button addManagerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupManagerDrawer(R.layout.drawer_base_manager);
        getLayoutInflater().inflate(R.layout.activity_addmanager, findViewById(R.id.content_frame), true);

        emailEditText = findViewById(R.id.manageremail);
        passwordEditText = findViewById(R.id.managerpassword);
        phoneEditText = findViewById(R.id.vehicleNum);
        nameEditText = findViewById(R.id.managername);
        surnameEditText = findViewById(R.id.managersurname);


        addManagerButton = findViewById(R.id.button_add_manager);

        addManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String name = nameEditText.getText().toString().trim();
                String surname = surnameEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(AddManagerActivity.this, "Email and password are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                Manager newManager = new Manager(name, surname, email, password, phone);
                newManager.registerNewManager(
                        unused -> {
                            Toast.makeText(AddManagerActivity.this, "The manager was added successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddManagerActivity.this, ManagerHomeDrawerActivity.class);
                            startActivity(intent);
                        },
                        e -> Toast.makeText(AddManagerActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}