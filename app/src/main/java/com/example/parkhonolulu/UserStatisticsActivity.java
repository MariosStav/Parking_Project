package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class UserStatisticsActivity extends BaseDrawerActivity {

    // TextView references for dashboard stats
    private TextView totalSessionsValue;
    private TextView avgDurationValue;
    private TextView totalSpendValue;
    private TextView lastSessionValue;

    // Buttons for navigation
    private Button btnUsageStats;
    private Button btnLocationStats;
    private Button btnFinancialStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_user_statistics, findViewById(R.id.content_frame), true);

        // Initialize TextViews
        totalSessionsValue = findViewById(R.id.totalSessionsValue);
        avgDurationValue = findViewById(R.id.avgDurationValue);
        totalSpendValue = findViewById(R.id.totalSpendValue);
        lastSessionValue = findViewById(R.id.lastSessionValue);

        // Initialize Buttons
        btnUsageStats = findViewById(R.id.btnUsageStats);
        btnLocationStats = findViewById(R.id.btnLocationStats);
        btnFinancialStats = findViewById(R.id.btnFinancialStats);

        parking_session.fetchTotalSessionsForCurrentUser(new parking_session.OnTotalSessionsFetched() {
            @Override
            public void onSuccess(int totalSessions) {
                totalSessionsValue.setText(String.valueOf(totalSessions));
            }

            @Override
            public void onFailure(Exception e) {
                totalSessionsValue.setText("Error");
                Log.e("TotalSessions", "Failed to fetch sessions", e);
            }
        });

        parking_session.fetchAverageDurationForCurrentUser(new parking_session.OnAverageDurationFetched() {
            @Override
            public void onSuccess(String formattedDuration) {
                avgDurationValue.setText(formattedDuration);
            }

            @Override
            public void onFailure(Exception e) {
                avgDurationValue.setText("Loading error");
            }
        });

        parking_session.fetchTotalSpendForCurrentUser(new parking_session.OnTotalSpendFetched() {
            @Override
            public void onSuccess(double totalSpend) {
                totalSpendValue.setText(String.format("$%.2f", totalSpend));
            }

            @Override
            public void onFailure(Exception e) {
                totalSpendValue.setText("Error");
                Log.e("TotalSpend", "Failed to fetch total spend", e);
            }
        });

        parking_session.fetchLastSessionForCurrentUser(new parking_session.OnLastSessionFetched() {
            @Override
            public void onSuccess(String lastSessionDateTime) {
                lastSessionValue.setText(lastSessionDateTime);
            }

            @Override
            public void onFailure(Exception e) {
                lastSessionValue.setText("Error");
                Log.e("LastSession", "Failed to fetch last session date", e);
            }
        });

        btnUsageStats.setOnClickListener(v -> {
            Intent intent = new Intent(UserStatisticsActivity.this, TimeStatsUserActivity.class);
            startActivity(intent);
        });

        btnLocationStats.setOnClickListener(v -> {
            Intent intent = new Intent(UserStatisticsActivity.this, LocationStatsUserActivity.class);
            startActivity(intent);
        });

        btnFinancialStats.setOnClickListener(v -> {
            Intent intent = new Intent(UserStatisticsActivity.this, FinancialStatsUserActivity.class);
            startActivity(intent);
        });
    }
}
