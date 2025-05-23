package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthManagerPasswordActivity extends AppCompatActivity {
    private Manager currentManager;
    private String managerEmail;
    private String otp;

    private EditText otpEditText;
    private Button verifyOtpButton;

    private String newPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managerauth);

        otpEditText = findViewById(R.id.codeEditText);
        verifyOtpButton = findViewById(R.id.verifyButton);

        newPassword = getIntent().getStringExtra("newPassword");
        managerEmail = getIntent().getStringExtra("managerEmail");

        if (managerEmail != null) {
            Log.d("ManagerAuthActivity", "Using passed email: " + managerEmail);
            sendOtp(managerEmail);
        } else {
            Toast.makeText(this, "Email missing", Toast.LENGTH_LONG).show();
            finish();
        }

        verifyOtpButton.setOnClickListener(v -> verifyOtp());
    }



    // Δημιουργία τυχαίου OTP
    private String generateOtp() {
        Random random = new Random();
        int otpNumber = 100000 + random.nextInt(900000); // 6-ψήφιο OTP
        return String.valueOf(otpNumber);
    }

    // Send OTP using ExecutorService
    private void sendOtp(String email) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            otp = generateOtp(); // Δημιουργούμε το OTP
            EmailUtils.sendOtpEmailforpassword(email, otp); // Στέλνουμε το OTP μέσω email
            Log.d("ManagerAuthActivity", "Code sent to " + managerEmail);
        });
        executor.shutdown();
    }

    // Επαλήθευση του OTP που έβαλε ο χρήστης
    private void verifyOtp() {
        String enteredOtp = otpEditText.getText().toString();

        if (enteredOtp.isEmpty()) {
            Toast.makeText(this, "Please enter the code", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredOtp.equals(otp)) {
            Log.d("ManagerAuthActivity", "Code verification successful");
            Toast.makeText(this, "Code verified successfully!", Toast.LENGTH_SHORT).show();

            if (newPassword == null || newPassword.isEmpty()) {
                Toast.makeText(this, "No new password provided", Toast.LENGTH_LONG).show();
                return;
            }

            Manager.changePassword(newPassword,
                    unused -> {
                        Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();

                        FirebaseAuth.getInstance().signOut(); // force re-authentication
                        Intent intent = new Intent(AuthManagerPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    },
                    e -> Toast.makeText(this, "Failed to change password: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );

        } else {
            Toast.makeText(this, "Code verification failed.", Toast.LENGTH_SHORT).show();
        }
    }

}
