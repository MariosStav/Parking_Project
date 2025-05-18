package com.example.parkhonolulu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomePage extends AppCompatActivity {

    private Button LookUpButton;
    private Button CompletePark;

    private Button Wallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        LookUpButton = findViewById(R.id.anazitisi);

        LookUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        CompletePark = findViewById(R.id.completepark);

        CompletePark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, UnparkActvity.class);
                startActivity(intent);
            }
        });

        Wallet = findViewById(R.id.wallet);

        Wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, WalletActivity.class);
                startActivity(intent);
            }
        });
    }
}