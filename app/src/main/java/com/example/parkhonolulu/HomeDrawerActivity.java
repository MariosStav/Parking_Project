package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import com.google.android.material.navigation.NavigationView;

public class HomeDrawerActivity extends BaseDrawerActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_home_page, findViewById(R.id.content_frame), true);
        // Set up home page button listeners
        Button lookUpButton = findViewById(R.id.anazitisi);
        Button completePark = findViewById(R.id.completepark);
        Button wallet = findViewById(R.id.wallet);
        lookUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeDrawerActivity.this, MapsActivity.class));
            }
        });
        completePark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeDrawerActivity.this, UnparkActvity.class));
            }
        });
        wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeDrawerActivity.this, WalletActivity.class));
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_lookup) {
            startActivity(new Intent(this, MapsActivity.class));
        } else if (id == R.id.nav_complete_park) {
            startActivity(new Intent(this, UnparkActvity.class));
        } else if (id == R.id.nav_wallet) {
            startActivity(new Intent(this, WalletActivity.class));
        } else if (id == R.id.nav_logout) {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAffinity(); // Changed from finish() to ensure all activities in the task are cleared
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
