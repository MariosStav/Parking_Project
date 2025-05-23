package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class GetEmailActivity extends AppCompatActivity {

    private Button cont;
    private EditText emailEditText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getemail);

        emailEditText = findViewById(R.id.editTextTextEmailAddress2);
        cont = findViewById(R.id.cont);

        cont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(GetEmailActivity.this, "Password reset email sent", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(GetEmailActivity.this, LoginActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(GetEmailActivity.this, "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });

            }
        });

    }
}
