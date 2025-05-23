package com.example.parkhonolulu;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParkingStatusUsersManagerActivity extends BaseManagerDrawerActivity {

    private PieChart pieChartAvailability;
    private LinearLayout layoutHighDemandSpots, layoutLowDemandSpots;
    private TextView tvUniqueUsers, tvReturningUsersPercentage, tvAvgParkingPerUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupManagerDrawer(R.layout.drawer_base_manager);
        getLayoutInflater().inflate(R.layout.activity_parking_status_users, findViewById(R.id.content_frame), true);

        // 1. Initialize UI Components
        pieChartAvailability = findViewById(R.id.pieChartAvailability);
        layoutHighDemandSpots = findViewById(R.id.layoutHighDemandSpots);
        layoutLowDemandSpots = findViewById(R.id.layoutLowDemandSpots);
        tvUniqueUsers = findViewById(R.id.tvUniqueUsers);
        tvReturningUsersPercentage = findViewById(R.id.tvReturningUsersPercentage);
        tvAvgParkingPerUser = findViewById(R.id.tvAvgParkingPerUser);

        // 2. Load data
        loadParkingAvailabilityData();
        loadDemandSpotsData();
        loadUserStatistics();
    }

    private void loadParkingAvailabilityData() {
        PieChart pieChartAvailability = findViewById(R.id.pieChartAvailability);

        parking_locations.loadAllSpots(true, new parking_locations.LoadSpotsCallback() {
            @Override
            public void onSuccess(List<parking_locations> spots) {
                int freeSpots = 0;
                int occupiedSpots = 0;

                for (parking_locations spot : spots) {
                    if (spot.isFree()) {
                        freeSpots++;
                    } else {
                        occupiedSpots++;
                    }
                }

                // Prepare pie chart entries
                List<PieEntry> entries = new ArrayList<>();
                entries.add(new PieEntry(freeSpots, "Free"));
                entries.add(new PieEntry(occupiedSpots, "Occupied"));

                PieDataSet dataSet = new PieDataSet(entries, "");
                dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336")); // Green, Red
                dataSet.setValueTextColor(Color.WHITE);
                dataSet.setValueTextSize(14f);

                PieData pieData = new PieData(dataSet);
                pieChartAvailability.setData(pieData);
                pieChartAvailability.getDescription().setEnabled(false);
                pieChartAvailability.setCenterText("Availability");
                pieChartAvailability.setCenterTextSize(16f);
                pieChartAvailability.setHoleRadius(40f);
                pieChartAvailability.setTransparentCircleRadius(45f);
                pieChartAvailability.setEntryLabelColor(Color.DKGRAY);
                pieChartAvailability.setEntryLabelTextSize(14f);
                pieChartAvailability.animateY(1000);
                pieChartAvailability.invalidate(); // refresh
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ParkingData", "Failed to load parking data", e);
            }
        });
    }


    private void loadDemandSpotsData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking_locations")
                .get()
                .addOnSuccessListener(locationSnapshots -> {
                    Map<String, Integer> demandMap = new HashMap<>();
                    Map<String, String> locationNames = new HashMap<>();

                    for (DocumentSnapshot doc : locationSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        demandMap.put(id, 0); // initialize count to 0
                        locationNames.put(id, name != null ? name : "Spot " + id);
                    }

                    db.collection("parking_sessions")
                            .whereNotEqualTo("end_time", null)
                            .get()
                            .addOnSuccessListener(sessionSnapshots -> {
                                for (DocumentSnapshot doc : sessionSnapshots) {
                                    DocumentReference locRef = doc.getDocumentReference("parking_location_id");
                                    if (locRef != null) {
                                        String id = locRef.getId();
                                        if (demandMap.containsKey(id)) {
                                            demandMap.put(id, demandMap.get(id) + 1);
                                        }
                                    }
                                }

                                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(demandMap.entrySet());
                                Collections.sort(sorted, new Comparator<Map.Entry<String, Integer>>() {
                                    @Override
                                    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                                        return b.getValue().compareTo(a.getValue()); // Descending
                                    }
                                });

                                List<Map.Entry<String, Integer>> top5High = sorted.subList(0, Math.min(5, sorted.size()));
                                List<Map.Entry<String, Integer>> top5Low = sorted.subList(Math.max(0, sorted.size() - 5), sorted.size());

                                layoutHighDemandSpots.removeAllViews();
                                layoutLowDemandSpots.removeAllViews();

                                for (Map.Entry<String, Integer> entry : top5High) {
                                    addSpotToLayout(entry.getKey(), entry.getValue(), locationNames.get(entry.getKey()), layoutHighDemandSpots);
                                }

                                for (Map.Entry<String, Integer> entry : top5Low) {
                                    addSpotToLayout(entry.getKey(), entry.getValue(), locationNames.get(entry.getKey()), layoutLowDemandSpots);
                                }
                            })
                            .addOnFailureListener(e -> Log.e("loadDemandSpotsData", "Error fetching sessions", e));
                })
                .addOnFailureListener(e -> Log.e("loadDemandSpotsData", "Error fetching locations", e));
    }

    private void addSpotToLayout(String spotId, int count, String name, LinearLayout layout) {
        TextView tv = new TextView(this);
        tv.setText(name + " - " + count + " sessions");
        tv.setTextSize(16f);
        tv.setPadding(8, 8, 8, 8);
        layout.addView(tv);
    }

    private void loadUserStatistics() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking_sessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> userSessionCounts = new HashMap<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        DocumentReference userRef = doc.getDocumentReference("user_id");
                        if (userRef != null) {
                            String userId = userRef.getId();
                            Integer currentCount = userSessionCounts.get(userId);
                            if (currentCount == null) currentCount = 0;
                            userSessionCounts.put(userId, currentCount + 1);
                        }
                    }

                    int totalSessions = queryDocumentSnapshots.size();
                    int uniqueUsers = userSessionCounts.size();

                    int returningUsers = 0;
                    for (Integer count : userSessionCounts.values()) {
                        if (count > 1) returningUsers++;
                    }

                    double returningPercentage = uniqueUsers > 0
                            ? (returningUsers * 100.0) / uniqueUsers
                            : 0.0;

                    double avgParkingPerUser = uniqueUsers > 0
                            ? (totalSessions * 1.0) / uniqueUsers
                            : 0.0;

                    tvUniqueUsers.setText("Unique Users: " + uniqueUsers);
                    tvReturningUsersPercentage.setText("Returning Users: " + String.format(Locale.US, "%.1f%%", returningPercentage));
                    tvAvgParkingPerUser.setText("Avg. Parking per User: " + String.format(Locale.US, "%.2f", avgParkingPerUser));

                })
                .addOnFailureListener(e -> {
                    Log.e("loadUserStatistics", "Failed to fetch sessions", e);
                });
    }
}
