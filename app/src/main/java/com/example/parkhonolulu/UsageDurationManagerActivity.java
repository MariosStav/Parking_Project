package com.example.parkhonolulu;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UsageDurationManagerActivity extends BaseManagerDrawerActivity {

    private BarChart barChartUsage;
    private LineChart lineChartAvgDuration;
    private TextView tvBarChartLabel;
    private TextView tvLineChartLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupManagerDrawer(R.layout.drawer_base_manager);
        getLayoutInflater().inflate(R.layout.activity_usagedurationmanager, findViewById(R.id.content_frame), true);

        // Find views
        barChartUsage = findViewById(R.id.barChartUsage);
        lineChartAvgDuration = findViewById(R.id.lineChartAvgDuration);
        tvBarChartLabel = findViewById(R.id.tvBarChartLabel);
        tvLineChartLabel = findViewById(R.id.tvLineChartLabel);

        tvBarChartLabel.setText("Usage distribution per spot / time period");
        tvLineChartLabel.setText("Average parking duration (hours)");

        setupBarChart();
        setupLineChart();
    }

    private void setupBarChart() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking_sessions")
                .whereNotEqualTo("end_time", null)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Integer> usageCountBySpot = new HashMap<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        DocumentReference spotRef = doc.getDocumentReference("parking_location_id");
                        if (spotRef != null) {
                            String spotId = spotRef.getId();
                            int count = usageCountBySpot.containsKey(spotId) ? usageCountBySpot.get(spotId) : 0;
                            usageCountBySpot.put(spotId, count + 1);
                        }
                    }

                    if (usageCountBySpot.isEmpty()) {
                        barChartUsage.clear();
                        barChartUsage.invalidate();
                        return;
                    }

                    List<String> spotIds = new ArrayList<>(usageCountBySpot.keySet());

                    db.collection("parking_locations")
                            .whereIn(FieldPath.documentId(), spotIds)
                            .get()
                            .addOnSuccessListener(locationsSnapshot -> {
                                Map<String, String> spotIdToName = new HashMap<>();
                                for (QueryDocumentSnapshot locDoc : locationsSnapshot) {
                                    String id = locDoc.getId();
                                    String name = locDoc.getString("name");
                                    spotIdToName.put(id, name != null ? name : "Unknown");
                                }

                                List<BarEntry> entries = new ArrayList<>();
                                List<String> labels = new ArrayList<>();
                                int index = 0;
                                for (String spotId : spotIds) {
                                    int count = usageCountBySpot.get(spotId);
                                    entries.add(new BarEntry(index, count));
                                    String label = spotIdToName.containsKey(spotId) ? spotIdToName.get(spotId) : "Spot " + spotId;
                                    labels.add(label);
                                    index++;
                                }

                                BarDataSet dataSet = new BarDataSet(entries, "Parking Spot Usage");
                                dataSet.setColor(Color.BLUE);
                                dataSet.setDrawValues(true);
                                dataSet.setValueTextSize(12f);
                                dataSet.setValueTextColor(Color.BLACK);

                                BarData barData = new BarData(dataSet);

                                int barCount = entries.size();
                                float barWidth = Math.min(0.9f, 8f / barCount); // Dynamic bar width
                                barData.setBarWidth(barWidth);

                                float groupSpace = 1f - barWidth;
                                float barSpace = 0f;
                                float startX = 0f;
                                float endX = startX + (barCount * (barWidth + barSpace + groupSpace));

                                barChartUsage.setData(barData);

                                XAxis xAxis = barChartUsage.getXAxis();
                                xAxis.setDrawLabels(false);
                                xAxis.setDrawGridLines(false);
                                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                xAxis.setGranularity(1f);
                                xAxis.setGranularityEnabled(true);
                                xAxis.setAxisMinimum(startX);
                                xAxis.setAxisMaximum(endX);

                                barChartUsage.setVisibleXRangeMaximum(barCount);
                                barChartUsage.setScaleEnabled(false);
                                barChartUsage.getAxisRight().setEnabled(false);
                                barChartUsage.getDescription().setEnabled(false);

                                barChartUsage.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
                                    @Override
                                    public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                                        int index = (int) e.getX();
                                        if (index >= 0 && index < labels.size()) {
                                            String spotName = labels.get(index);
                                            android.widget.Toast.makeText(UsageDurationManagerActivity.this,
                                                    "Spot: " + spotName, android.widget.Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected() {
                                        // Do nothing
                                    }
                                });

                                barChartUsage.invalidate();

                            }).addOnFailureListener(e -> Log.e("UsageDuration", "Failed to load parking locations", e));

                }).addOnFailureListener(e -> Log.e("UsageDuration", "Failed to load parking sessions", e));
    }



    private void setupLineChart() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking_sessions")
                .whereNotEqualTo("end_time", null)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, Long> totalDurationByDay = new HashMap<>();
                    Map<String, Integer> countByDay = new HashMap<>();

                    // Use a date format with year to avoid duplicate dates across years
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Athens"));

                    // Collect durations and counts per unique date
                    for (DocumentSnapshot doc : querySnapshot) {
                        Timestamp start = doc.getTimestamp("start_time");
                        Timestamp end = doc.getTimestamp("end_time");

                        if (start != null && end != null) {
                            String dayKey = dateFormat.format(start.toDate());
                            long duration = end.toDate().getTime() - start.toDate().getTime();

                            if (totalDurationByDay.containsKey(dayKey)) {
                                long currentTotal = totalDurationByDay.get(dayKey);
                                totalDurationByDay.put(dayKey, currentTotal + duration);
                            } else {
                                totalDurationByDay.put(dayKey, duration);
                            }

                            if (countByDay.containsKey(dayKey)) {
                                int currentCount = countByDay.get(dayKey);
                                countByDay.put(dayKey, currentCount + 1);
                            } else {
                                countByDay.put(dayKey, 1);
                            }

                        }
                    }

                    // Sort the unique days chronologically
                    List<String> sortedDays = new ArrayList<>(totalDurationByDay.keySet());
                    Collections.sort(sortedDays);

                    SimpleDateFormat displayFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
                    displayFormat.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Athens"));

                    List<String> displayLabels = new ArrayList<>();
                    for (String day : sortedDays) {
                        try {
                            Date date = dateFormat.parse(day);
                            displayLabels.add(displayFormat.format(date));
                        } catch (ParseException e) {
                            displayLabels.add(day); // fallback
                        }
                    }

                    // Build chart entries: one per unique day with average duration (in hours)
                    List<Entry> entries = new ArrayList<>();
                    for (int i = 0; i < sortedDays.size(); i++) {
                        String day = sortedDays.get(i);
                        long totalDuration = totalDurationByDay.get(day);
                        int count = countByDay.get(day);

                        float avgHours = (float) totalDuration / (count * 1000 * 60 * 60);
                        entries.add(new Entry(i, avgHours));
                    }

                    // Configure dataset and chart
                    LineDataSet dataSet = new LineDataSet(entries, "Average Duration per Day (hours)");
                    dataSet.setColor(Color.BLUE);
                    dataSet.setValueTextColor(Color.BLACK);
                    dataSet.setCircleColor(Color.RED);
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(4f);

                    LineData lineData = new LineData(dataSet);
                    lineChartAvgDuration.setData(lineData);

                    // Setup X-axis to show date labels
                    XAxis xAxis = lineChartAvgDuration.getXAxis();
                    xAxis.setDrawLabels(true);
                    xAxis.setTextColor(Color.BLACK);
                    xAxis.setTextSize(12f);
                    xAxis.setGranularity(1f);
                    xAxis.setLabelCount(sortedDays.size(), true);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value;
                            if (index >= 0 && index < displayLabels.size()) {
                                return displayLabels.get(index);
                            }
                            return "";
                        }
                    });


                    lineChartAvgDuration.getAxisRight().setEnabled(false);
                    lineChartAvgDuration.getDescription().setText("Average Parking Duration per Day");

                    // Set visible range and axis limits to fit data
                    lineChartAvgDuration.setVisibleXRangeMaximum(sortedDays.size());
                    xAxis.setAxisMinimum(0f);
                    xAxis.setAxisMaximum(sortedDays.size() - 1f);

                    lineChartAvgDuration.invalidate();
                })
                .addOnFailureListener(e -> Log.e("Chart", "Error loading sessions", e));
    }
}
