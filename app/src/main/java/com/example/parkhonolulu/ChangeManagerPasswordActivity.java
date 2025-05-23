package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChangeManagerPasswordActivity extends BaseManagerDrawerActivity {

    private Button change;
    private EditText password1, password2;
    private String managerEmail; // store passed email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupManagerDrawer(R.layout.drawer_base_manager);
        getLayoutInflater().inflate(R.layout.activity_changemanagerpassword, findViewById(R.id.content_frame), true);

        managerEmail = getIntent().getStringExtra("managerEmail");

        change = findViewById(R.id.change);
        password1 = findViewById(R.id.editTextTextPassword1);
        password2 = findViewById(R.id.editTextTextPassword2);

        change.setOnClickListener(v -> {
            String pw1 = password1.getText().toString().trim();
            String pw2 = password2.getText().toString().trim();

            if (pw1.isEmpty() || pw2.isEmpty()) {
                Toast.makeText(this, "Please fill both password fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pw1.equals(pw2)) {
                Toast.makeText(this, "The passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pw1.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // All checks passed — continue to OTP verification
            Intent intent = new Intent(ChangeManagerPasswordActivity.this, AuthManagerPasswordActivity.class);
            intent.putExtra("newPassword", pw1);
            if (managerEmail != null) {
                intent.putExtra("managerEmail", managerEmail);
            }
            startActivity(intent);
        });
    }
}
