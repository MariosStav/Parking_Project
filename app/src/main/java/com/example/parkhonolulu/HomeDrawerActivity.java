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

    private Button LookUpButton;
    private Button CompletePark;
    private Button Wallet;
    private Button EditProfile;
    private Button Statistics;
    private Button Mypark;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_home_page, findViewById(R.id.content_frame), true);

        LookUpButton = findViewById(R.id.anazitisi);
        CompletePark = findViewById(R.id.completepark);
        Wallet = findViewById(R.id.wallet);
        EditProfile = findViewById(R.id.profile2);
        Statistics = findViewById(R.id.statistika);
        Mypark = findViewById(R.id.activepark);

        Mypark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDrawerActivity.this, MyparklocationActivity.class);
                startActivity(intent);
            }
        });

        LookUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDrawerActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        CompletePark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDrawerActivity.this, UnparkActvity.class);
                startActivity(intent);
            }
        });

        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDrawerActivity.this, EditUserActivity.class);
                startActivity(intent);
            }
        });

        Statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDrawerActivity.this, UserStatisticsActivity.class);
                startActivity(intent);
            }
        });

        Wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeDrawerActivity.this, WalletActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) { 
            Intent intent = new Intent(this, HomeDrawerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else if (id == R.id.nav_lookup) {
            startActivity(new Intent(this, MapsActivity.class));
        } else if (id == R.id.nav_complete_park) {
            startActivity(new Intent(this, UnparkActvity.class));
        } else if (id == R.id.nav_wallet) {
            startActivity(new Intent(this, WalletActivity.class));
        } else if (id == R.id.nav_my_park) {
            startActivity(new Intent(this, MyparklocationActivity.class));
        } else if (id == R.id.nav_edit_profile) {
            startActivity(new Intent(this, EditUserActivity.class));
        } else if (id == R.id.nav_statistics) {
            startActivity(new Intent(this, UserStatisticsActivity.class));
        } else if (id == R.id.nav_logout) {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            Intent logoutIntent = new Intent(this, LoginActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finishAffinity(); 
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
