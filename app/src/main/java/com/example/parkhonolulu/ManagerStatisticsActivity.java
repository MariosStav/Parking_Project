package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class ManagerStatisticsActivity extends BaseManagerDrawerActivity {

    private Button btnUsageDuration, btnFinancials, btnSpotsUsers;
    private TextView occupiedSpotsValue, totalRevenueValue, avgDurationValue, availableSpotsValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupManagerDrawer(R.layout.drawer_base_manager);
        getLayoutInflater().inflate(R.layout.activity_manager_statistics, findViewById(R.id.content_frame), true);

        // Buttons
        btnUsageDuration = findViewById(R.id.btnUsageDuration);
        btnFinancials = findViewById(R.id.btnFinancials);
        btnSpotsUsers = findViewById(R.id.btnSpotsUsers);

        // TextViews
        occupiedSpotsValue = findViewById(R.id.occupiedSpotsValue);
        totalRevenueValue = findViewById(R.id.totalRevenueValue);
        avgDurationValue = findViewById(R.id.avgDurationValue);
        availableSpotsValue = findViewById(R.id.availableSpotsValue);

        loadAndDisplayOccupiedSpotsPercentage();
        loadAndDisplayTotalRevenue();
        loadAndDisplayAverageDuration();
        loadAndDisplayAvailableSpots();

        btnUsageDuration.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerStatisticsActivity.this, UsageDurationManagerActivity.class);
            startActivity(intent);
        });

        btnFinancials.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerStatisticsActivity.this, FinancialStatsManagerActivity.class);
            startActivity(intent);
        });

        btnSpotsUsers.setOnClickListener(v -> {
            Intent intent = new Intent(ManagerStatisticsActivity.this, ParkingStatusUsersManagerActivity.class);
            startActivity(intent);
        });
    }

    private void loadAndDisplayOccupiedSpotsPercentage() {
        parking_locations.loadAllSpots(true, new parking_locations.LoadSpotsCallback() {
            @Override
            public void onSuccess(List<parking_locations> spots) {
                if (spots == null || spots.isEmpty()) {
                    runOnUiThread(() -> occupiedSpotsValue.setText("No data found"));
                    return;
                }

                int totalSpots = spots.size();
                int occupiedCount = 0;

                for (parking_locations spot : spots) {
                    if (!spot.isFree()) { // Δηλαδή κατειλημμένο
                        occupiedCount++;
                    }
                }

                final double occupiedPercentage = (occupiedCount * 100.0) / totalSpots;

                runOnUiThread(() -> {
                    String formatted = String.format("%.1f%%", occupiedPercentage);
                    occupiedSpotsValue.setText(formatted);
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> occupiedSpotsValue.setText("Loading error"));
            }
        });
    }

    private void loadAndDisplayTotalRevenue() {
        parking_session.fetchTotalRevenue(new parking_session.OnTotalRevenueFetched() {
            @Override
            public void onSuccess(double totalRevenue) {
                runOnUiThread(() -> {
                    String formatted = String.format("%.2f $", totalRevenue);
                    totalRevenueValue.setText(formatted);
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> totalRevenueValue.setText("Loading error"));
            }
        });
    }

    private void loadAndDisplayAverageDuration() {
        parking_session.fetchAverageDurationForAllUsers(new parking_session.OnAverageDurationFetched() {
            @Override
            public void onSuccess(String formattedDuration) {
                runOnUiThread(() -> avgDurationValue.setText(formattedDuration));
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> avgDurationValue.setText("Loading error"));
            }
        });
    }


    private void loadAndDisplayAvailableSpots() {
        parking_locations.loadAllSpots(true, new parking_locations.LoadSpotsCallback() {
            @Override
            public void onSuccess(List<parking_locations> spots) {
                if (spots == null || spots.isEmpty()) {
                    runOnUiThread(() -> availableSpotsValue.setText("No data found"));
                    return;
                }

                int availableCount = 0;
                for (parking_locations spot : spots) {
                    if (spot.isFree()) {
                        availableCount++;
                    }
                }

                final int available = availableCount;

                runOnUiThread(() -> availableSpotsValue.setText(String.valueOf(available)));
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> availableSpotsValue.setText("Loading error"));
            }
        });
    }
}
