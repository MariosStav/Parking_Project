package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// This class is responsible for the Manager's home screen with the navigation drawer.
public class ManagerHomeDrawerActivity extends BaseManagerDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupManagerDrawer(R.layout.drawer_base_manager);

        getLayoutInflater().inflate(R.layout.activity_manager_home_page, findViewById(R.id.content_frame), true);

        Button addManagerButton = findViewById(R.id.addmanager);
        addManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomeDrawerActivity.this, AddManagerActivity.class);
                startActivity(intent);
            }
        });
        Button editManagerButton = findViewById(R.id.profile);
        editManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomeDrawerActivity.this, EditManagerActivity.class);
                startActivity(intent);
            }
        });
        Button statisticsButton = findViewById(R.id.statistika);
        statisticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomeDrawerActivity.this, ManagerStatisticsActivity.class);
                startActivity(intent);
            }
        });
        Button addParkLocationButton = findViewById(R.id.addParkLocation);
        addParkLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManagerHomeDrawerActivity.this, HandlungParklocationActivity.class);
                startActivity(intent);
            }
        });
    }
}
