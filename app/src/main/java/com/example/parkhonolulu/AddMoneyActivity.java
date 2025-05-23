package com.example.parkhonolulu;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddMoneyActivity extends BaseDrawerActivity {
    private EditText cardNumberEditText, cardNameEditText, cvvEditText;
    private Spinner monthSpinner, yearSpinner;
    private Button payButton;

    private double amountToAdd = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_addmoney, findViewById(R.id.content_frame), true);

        // Link views
        cardNumberEditText = findViewById(R.id.cardNumberEditText);
        cardNameEditText = findViewById(R.id.cardNameEditText);
        cvvEditText = findViewById(R.id.cvvEditText);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        payButton = findViewById(R.id.payButton);

        String amountStr = getIntent().getStringExtra("amount_to_add");
        if (amountStr != null) {
            try {
                amountToAdd = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                amountToAdd = 0.0;
            }
        }

        // Populate month spinner with "MM", "01", ..., "12"
        ArrayList<String> months = new ArrayList<>();
        months.add("MM");
        for (int i = 1; i <= 12; i++) {
            months.add(String.format(Locale.getDefault(), "%02d", i));
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // Populate year spinner with "YY", "25", ..., "34"
        ArrayList<String> years = new ArrayList<>();
        years.add("YY");
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i <= 9; i++) {
            String yearSuffix = String.valueOf(currentYear + i).substring(2); // e.g., "25"
            years.add(yearSuffix);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Pay button logic
        payButton.setOnClickListener(v -> {
            String cardNumber = cardNumberEditText.getText().toString().trim();
            String name = cardNameEditText.getText().toString().trim();
            String cvv = cvvEditText.getText().toString().trim();
            String month = monthSpinner.getSelectedItem().toString();
            String year = yearSpinner.getSelectedItem().toString();

            if (cardNumber.length() != 16 || !cardNumber.matches("\\d{16}")) {
                showToast("Card number must be 16 digits");
            } else if (name.isEmpty()) {
                showToast("Cardholder name is required");
            } else if (!cvv.matches("\\d{3}")) {
                showToast("CVV must be 3 digits");
            } else if (month.equals("MM") || year.equals("YY")) {
                showToast("Please select valid expiry month and year");
            } else {
                Balance.addToCurrentUserBalance(amountToAdd,
                        () -> {
                            showToast("Balance updated successfully!");
                            // Return to WalletActivity, maybe finish AddMoneyActivity
                            finish();
                        },
                        e -> showToast("Failed to update balance: " + e.getMessage())
                );
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
