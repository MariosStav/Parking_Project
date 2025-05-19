package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginManagerActivity extends AppCompatActivity {

    private EditText EmailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LoginManagerActivity", "Activity started");
        setContentView(R.layout.activity_loginmanager);

        EmailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        loadingProgressBar = findViewById(R.id.loading);

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateLoginForm();
            }
        };

        EmailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void validateLoginForm() {
        String email = EmailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        loginButton.setEnabled(!email.isEmpty() && !password.isEmpty());
    }

    private void attemptLogin() {
        String email = EmailEditText.getText().toString().trim().toLowerCase();
        String password = passwordEditText.getText().toString().trim();

        Log.d("LOGIN_DEBUG", "Email entered: '" + email + "'");

        loadingProgressBar.setVisibility(View.VISIBLE);

        Manager.loginAsManager(email, password,
                unused -> {
                    // Success: launch Manager activity
                    Intent intent = new Intent(this, ManagerAuthActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}
