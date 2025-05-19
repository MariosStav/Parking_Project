package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManagerAuthActivity extends AppCompatActivity {

    private String managerEmail;
    private String otp;

    private EditText otpEditText;
    private Button verifyOtpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managerauth);

        otpEditText = findViewById(R.id.codeEditText);
        verifyOtpButton = findViewById(R.id.verifyButton);

        // Παίρνουμε το email από το Intent
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            Log.d("ManagerAuthActivity", "Received email: " + email);
            managerEmail = email;
            sendOtp(managerEmail); // Call the method to send OTP
        } else {
            Log.d("ManagerAuthActivity", "No email received");
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
            EmailUtils.sendOtpEmail(email, otp); // Στέλνουμε το OTP μέσω email
            Log.d("ManagerAuthActivity", "OTP sent to " + managerEmail);
        });
        executor.shutdown();
    }

    // Επαλήθευση του OTP που έβαλε ο χρήστης
    private void verifyOtp() {
        String enteredOtp = otpEditText.getText().toString();

        if (enteredOtp.isEmpty()) {
            Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredOtp.equals(otp)) {
            Toast.makeText(this, "OTP verified successfully!", Toast.LENGTH_SHORT).show();
            Log.d("ManagerAuthActivity", "OTP verification successful, starting ManagerHomePage.");
            startActivity(new Intent(ManagerAuthActivity.this, ManagerHomePage.class));
            finish();
        } else {
            Toast.makeText(this, "OTP verification failed.", Toast.LENGTH_SHORT).show();
        }
    }
}
