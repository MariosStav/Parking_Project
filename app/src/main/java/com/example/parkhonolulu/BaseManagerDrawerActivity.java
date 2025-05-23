package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseManagerDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupManagerDrawer(int layoutResID) {
        setContentView(layoutResID);
        drawerLayout = findViewById(R.id.drawer_layout); // Ensure your manager layout has a DrawerLayout with this ID
        navigationView = findViewById(R.id.nav_view); // Ensure your manager layout has a NavigationView with this ID
        navigationView.setNavigationItemSelectedListener(this);
        // navigationView.inflateMenu(R.menu.manager_nav_menu); // REMOVED: Menu is inflated via XML in drawer_base_manager.xml

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle != null && toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_manager_home) {
            Intent intent = new Intent(this, ManagerHomeDrawerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else if (id == R.id.nav_add_manager) {
            startActivity(new Intent(this, AddManagerActivity.class));
        } else if (id == R.id.nav_edit_manager_profile) {
            startActivity(new Intent(this, EditManagerActivity.class));
        } else if (id == R.id.nav_manager_statistics) {
            startActivity(new Intent(this, ManagerStatisticsActivity.class));
        } else if (id == R.id.nav_add_park_location) {
            startActivity(new Intent(this, HandlungParklocationActivity.class));
        } else if (id == R.id.nav_manager_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class); // Assuming LoginActivity is the main login
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAffinity();
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
