package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// This class is responsible for the Manager's home screen with the navigation drawer.
public class ManagerHomeDrawerActivity extends BaseManagerDrawerActivity {

    private Button AddManagerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the drawer using the base layout for manager drawers
        setupManagerDrawer(R.layout.drawer_base_manager);

        // Inflate the specific layout for the Manager's home page content
        // into the content_frame of drawer_base_manager.xml
        getLayoutInflater().inflate(R.layout.activity_manager_home_page, findViewById(R.id.content_frame), true);

        // Initialize and set up UI elements from activity_manager_home_page.xml
        AddManagerButton = findViewById(R.id.addmanager);
        if (AddManagerButton != null) {
            AddManagerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ManagerHomeDrawerActivity.this, AddManagerActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}
