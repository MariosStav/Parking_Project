package com.example.parkhonolulu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.parkhonolulu.ui.login.LoginActivity;
import com.example.parkhonolulu.ui.login.SignUpActivity;

public class HomePage extends AppCompatActivity {

    private Button LookUpButton;
    private String currentUserCarType; // Added to store car type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Retrieve the car type from the intent
        currentUserCarType = getIntent().getStringExtra("USER_CAR_TYPE");
        // Optional: Log the retrieved car type for debugging
        // android.util.Log.d("HomePage", "Received car type: " + currentUserCarType);

        LookUpButton = findViewById(R.id.button3);

        LookUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                Intent intent = new Intent(HomePage.this, MapsActivity.class);
                // Pass the car type to MapsActivity
                intent.putExtra("USER_CAR_TYPE", currentUserCarType);
                startActivity(intent);
            }
        });
    }
}