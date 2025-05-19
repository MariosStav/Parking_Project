package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ManagerHomePage extends AppCompatActivity {

    private Button AddManagerButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_home_page);

        AddManagerButton = findViewById(R.id.addmanager);
        AddManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomePage.this, AddManagerActivity.class);
                startActivity(intent);
            }
        });
    }

}
