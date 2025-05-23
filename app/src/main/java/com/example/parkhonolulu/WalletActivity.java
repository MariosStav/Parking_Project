package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class WalletActivity extends BaseDrawerActivity {

    private EditText balanceEditText;
    private EditText AmountText;
    private Button addMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_wallet, findViewById(R.id.content_frame), true);

        balanceEditText = findViewById(R.id.balanceEditText);
        AmountText = findViewById(R.id.amountEditText);
        addMoney = findViewById(R.id.payParkingButton);

        loadUserBalance();

        // Disable button initially
        addMoney.setEnabled(false);

        // Add TextWatcher to listen for changes in the amount EditText
        AmountText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable button if text is not empty
                addMoney.setEnabled(!s.toString().trim().isEmpty());
                addMoney.setAlpha(s.toString().trim().isEmpty() ? 0.5f : 1.0f);

            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        addMoney.setOnClickListener(v -> {
            String amountStr = AmountText.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                Intent intent = new Intent(WalletActivity.this, AddMoneyActivity.class);
                intent.putExtra("amount_to_add", amountStr);
                startActivity(intent);
            }
        });
    }

    private void loadUserBalance() {
        Balance.fetchCurrentUserBalance(
                amount -> balanceEditText.setText(String.format(Locale.getDefault(), "%.2f $", amount)),
                e -> Toast.makeText(WalletActivity.this, "Failed to load balance: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserBalance();
        AmountText.setText("");
        addMoney.setEnabled(false);
        addMoney.setAlpha(0.5f);
    }
}
