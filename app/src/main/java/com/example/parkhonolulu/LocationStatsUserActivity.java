package com.example.parkhonolulu;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationStatsUserActivity extends BaseDrawerActivity {

    private TextView distinctParkingCountText;
    private HorizontalBarChart topLocationsBarChart;
    private PieChart locationPieChart;
    private static final String TAG = "LocationStatsUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_location_stats_user, findViewById(R.id.content_frame), true);

        // Init views
        distinctParkingCountText = findViewById(R.id.distinctParkingCountText);
        topLocationsBarChart = findViewById(R.id.topLocationsBarChart);
        locationPieChart = findViewById(R.id.locationPieChart);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadAndShowStats(userId);
    }

    private void updateDistinctCount(Map<String, Integer> locationMap) {
        int count = locationMap.size();
        distinctParkingCountText.setText("You used " + count + " different parking locations");
    }

    private void populateBarChart(Map<String, Integer> locationMap) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(locationMap.entrySet());
        Collections.sort(sorted, (a, b) -> b.getValue() - a.getValue());

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int max = Math.min(5, sorted.size());
        for (int i = 0; i < max; i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Top Locations");
        BarData barData = new BarData(dataSet);
        dataSet.setColors(new int[]{
                R.color.sunset,
                R.color.iris,
                R.color.gold,
                R.color.navy,
                R.color.baby_blue
        }, this);
        dataSet.setValueTextSize(12f);

        topLocationsBarChart.setData(barData);
        topLocationsBarChart.getDescription().setEnabled(false);
        topLocationsBarChart.getAxisRight().setEnabled(false);
        topLocationsBarChart.getXAxis().setDrawGridLines(false);
        topLocationsBarChart.getXAxis().setGranularity(1f);
        topLocationsBarChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return index >= 0 && index < labels.size() ? labels.get(index) : "";
            }
        });

        topLocationsBarChart.animateY(1000);
        topLocationsBarChart.invalidate();
    }

    private void populatePieChart(Map<String, Integer> locationMap) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(locationMap.entrySet());
        Collections.sort(sorted, (a, b) -> b.getValue() - a.getValue());

        List<PieEntry> entries = new ArrayList<>();
        int total = 0;
        for (int value : locationMap.values()) {
            total += value;
        }

        int max = Math.min(5, sorted.size());
        for (int i = 0; i < max; i++) {
            Map.Entry<String, Integer> entry = sorted.get(i);
            float percent = (entry.getValue() * 100f) / total;
            entries.add(new PieEntry(percent, entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Location Share");
        dataSet.setColors(new int[]{
                R.color.iris,
                R.color.sunset,
                R.color.baby_blue,
                R.color.gold,
                R.color.navy
        }, this);



        PieData pieData = new PieData(dataSet);
        pieData.setValueTextSize(12f);

        locationPieChart.setData(pieData);
        locationPieChart.setUsePercentValues(true);
        locationPieChart.getDescription().setEnabled(false);
        locationPieChart.setEntryLabelColor(android.graphics.Color.BLACK);
        locationPieChart.animateY(1000);
        locationPieChart.invalidate();
    }

    private void loadAndShowStats(String userId) {
        Log.d(TAG, "Loading stats for userId: " + userId);

        parking_session.fetchLocationUsageStats(userId, locationUsageMap -> {
            Log.d(TAG, "Fetched location usage stats: " + locationUsageMap);

            if (locationUsageMap.isEmpty()) {
                Log.w(TAG, "Location usage map is empty - no parking sessions found");
                runOnUiThread(() -> {
                    Toast.makeText(this, "No parking usage data found for this user.", Toast.LENGTH_LONG).show();
                });
                return;
            }

            List<String> locationIds = new ArrayList<>(locationUsageMap.keySet());
            Log.d(TAG, "Location IDs to fetch names for: " + locationIds);

            fetchLocationNames(locationIds, locationIdToNameMap -> {
                Log.d(TAG, "Fetched location names: " + locationIdToNameMap);

                if (locationIdToNameMap.isEmpty()) {
                    Log.w(TAG, "No location names found for given IDs");
                }

                Map<String, Integer> namedUsageMap = new HashMap<>();
                for (String locId : locationUsageMap.keySet()) {
                    String name = locationIdToNameMap.containsKey(locId) ? locationIdToNameMap.get(locId) : locId;
                    namedUsageMap.put(name, locationUsageMap.get(locId));
                }

                Log.d(TAG, "Mapped usage with location names: " + namedUsageMap);

                runOnUiThread(() -> {
                    updateDistinctCount(namedUsageMap);
                    populateBarChart(namedUsageMap);
                    populatePieChart(namedUsageMap);
                });
            }, e -> {
                Log.e(TAG, "Failed to fetch location names", e);
                Toast.makeText(this, "Failed to load location names", Toast.LENGTH_SHORT).show();
            });
        }, e -> {
            Log.e(TAG, "Failed to fetch location usage stats", e);
            Toast.makeText(this, "Failed to load location stats", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchLocationNames(List<String> locationIds,
                                    OnLocationNamesFetched callback,
                                    OnErrorListener errorCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, String> idToName = new HashMap<>();

        List<com.google.android.gms.tasks.Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String locId : locationIds) {
            tasks.add(db.collection("parking_locations").document(locId).get());
        }

        com.google.android.gms.tasks.Tasks.whenAllSuccess(tasks)
                .addOnSuccessListener(results -> {
                    for (Object obj : results) {
                        DocumentSnapshot doc = (DocumentSnapshot) obj;
                        if (doc.exists()) {
                            idToName.put(doc.getId(), doc.getString("name"));
                        }
                    }
                    callback.onComplete(idToName);
                })
                .addOnFailureListener(errorCallback::onError);
    }

    // Callback interfaces you also need to declare in your class:
    public interface OnLocationNamesFetched {
        void onComplete(Map<String, String> locationIdToName);
    }

    public interface OnErrorListener {
        void onError(Exception e);
    }
}
