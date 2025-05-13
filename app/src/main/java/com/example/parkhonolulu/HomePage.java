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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        LookUpButton = findViewById(R.id.button3);

        LookUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                Intent intent = new Intent(HomePage.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}