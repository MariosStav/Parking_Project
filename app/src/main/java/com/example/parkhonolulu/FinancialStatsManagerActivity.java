package com.example.parkhonolulu;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class FinancialStatsManagerActivity extends BaseManagerDrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupManagerDrawer(R.layout.drawer_base_manager);
        getLayoutInflater().inflate(R.layout.activity_financialstatsmanager, findViewById(R.id.content_frame), true);

        Spinner spinnerDateRange = findViewById(R.id.spinnerDateRange);
        Spinner spinnerChartType = findViewById(R.id.spinnerChartType);
        PieChart chartRevenueBySpot = findViewById(R.id.chartRevenueBySpot);
        LineChart chartRevenueHistory = findViewById(R.id.chartRevenueHistory);
        TextView tvAvgRevenue = findViewById(R.id.tvAvgRevenue);

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Last 7 Days", "Last 30 Days", "This Month", "This Year"});
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDateRange.setAdapter(dateAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All", "Electric", "Gas"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChartType.setAdapter(typeAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadFinancialData(spinnerDateRange.getSelectedItem().toString(),
                        spinnerChartType.getSelectedItem().toString(),
                        chartRevenueBySpot, chartRevenueHistory, tvAvgRevenue);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerDateRange.setOnItemSelectedListener(filterListener);
        spinnerChartType.setOnItemSelectedListener(filterListener);
    }

    private void loadFinancialData(String dateRange, String vehicleType, PieChart pieChart,
                                   LineChart lineChart, TextView avgRevenueText) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Setup date range
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        switch (dateRange) {
            case "Last 7 Days": calendar.add(Calendar.DAY_OF_YEAR, -7); break;
            case "Last 30 Days": calendar.add(Calendar.DAY_OF_YEAR, -30); break;
            case "This Month": calendar.set(Calendar.DAY_OF_MONTH, 1); break;
            case "This Year": calendar.set(Calendar.DAY_OF_YEAR, 1); break;
        }

        Date startDate = calendar.getTime();

        // Base query to fetch parking sessions
        db.collection("parking_sessions")
                .whereGreaterThanOrEqualTo("start_time", startDate)
                .get()
                .addOnSuccessListener(snapshot -> {
                    final double[] totalRevenue = {0.0};
                    final int[] sessionCount = {0};

                    Map<String, Double> revenuePerSpot = new HashMap<>();
                    Map<String, Double> revenuePerDay = new TreeMap<>();

                    final Set<String> locationIds = new HashSet<>();
                    final List<QueryDocumentSnapshot> filteredSessions = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Timestamp startTime = doc.getTimestamp("start_time");
                        Timestamp endTime = doc.getTimestamp("end_time");

                        if (startTime == null) continue;

                        Double revenue = doc.getDouble("fee_charged");
                        if (revenue == null) continue;

                        Date startDateVal = startTime.toDate();
                        Date endDateVal = endTime != null ? endTime.toDate() : new Date();

                        if (startDateVal.before(startDate) || startDateVal.after(endDate)) continue;

                        DocumentReference locRef = doc.getDocumentReference("parking_location_id");
                        if (locRef != null) {
                            locationIds.add(locRef.getId());
                            filteredSessions.add(doc);
                        }
                    }

                    if (vehicleType.equals("All")) {
                        // Aggregate revenue without filtering by vehicle type
                        for (QueryDocumentSnapshot doc : filteredSessions) {
                            Double revenue = doc.getDouble("fee_charged");
                            DocumentReference locRef = doc.getDocumentReference("parking_location_id");
                            Timestamp startTimestamp = doc.getTimestamp("start_time");
                            Date startDateVal = startTimestamp != null ? startTimestamp.toDate() : null;

                            if (revenue == null || locRef == null || startDateVal == null) continue;

                            String locId = locRef.getId();
                            if (revenuePerSpot.containsKey(locId)) {
                                revenuePerSpot.put(locId, revenuePerSpot.get(locId) + revenue);
                            } else {
                                revenuePerSpot.put(locId, revenue);
                            }

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
                            String day = sdf.format(startDateVal);

                            if (revenuePerDay.containsKey(day)) {
                                revenuePerDay.put(day, revenuePerDay.get(day) + revenue);
                            } else {
                                revenuePerDay.put(day, revenue);
                            }

                            totalRevenue[0] += revenue;
                            sessionCount[0]++;
                        }

                        updateCharts(db, revenuePerSpot, revenuePerDay, pieChart, lineChart, avgRevenueText, totalRevenue[0], sessionCount[0], startDate, endDate);
                        return;
                    }

                    // Filter by vehicle type: fetch parking locations and match their type
                    db.collection("parking_locations")
                            .whereIn(FieldPath.documentId(), new ArrayList<>(locationIds))
                            .get()
                            .addOnSuccessListener(locSnapshot -> {
                                Map<String, String> locationTypeMap = new HashMap<>();
                                for (DocumentSnapshot locDoc : locSnapshot) {
                                    locationTypeMap.put(locDoc.getId(), locDoc.getString("type"));
                                }

                                for (QueryDocumentSnapshot doc : filteredSessions) {
                                    Double revenue = doc.getDouble("fee_charged");
                                    DocumentReference locRef = doc.getDocumentReference("parking_location_id");
                                    Timestamp startTimestamp = doc.getTimestamp("start_time");

                                    if (revenue == null || locRef == null || startTimestamp == null) continue;

                                    Date startDateVal = startTimestamp.toDate();
                                    if (startDateVal == null) continue;

                                    String locId = locRef.getId();
                                    String locType = locationTypeMap.get(locId);

                                    if (!vehicleType.toLowerCase(Locale.ROOT).equals(locType)) continue;

                                    if (revenuePerSpot.containsKey(locId)) {
                                        revenuePerSpot.put(locId, revenuePerSpot.get(locId) + revenue);
                                    } else {
                                        revenuePerSpot.put(locId, revenue);
                                    }

                                    String day = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDateVal);
                                    if (revenuePerDay.containsKey(day)) {
                                        revenuePerDay.put(day, revenuePerDay.get(day) + revenue);
                                    } else {
                                        revenuePerDay.put(day, revenue);
                                    }

                                    totalRevenue[0] += revenue;
                                    sessionCount[0]++;
                                }

                                updateCharts(db, revenuePerSpot, revenuePerDay, pieChart, lineChart, avgRevenueText, totalRevenue[0], sessionCount[0], startDate, endDate);
                            });

                }).addOnFailureListener(e -> Log.e("FinanceStats", "Failed to load data", e));
    }

    private void updateCharts(FirebaseFirestore db, Map<String, Double> revenuePerSpot,
                              Map<String, Double> revenuePerDay, PieChart pieChart,
                              LineChart lineChart, TextView avgRevenueText,
                              double totalRevenue, int sessionCount,
                              Date startDate, Date endDate) {

        double avgRevenue = sessionCount > 0 ? totalRevenue / sessionCount : 0;
        avgRevenueText.setText(String.format(Locale.getDefault(), "Average fee charged per use: $%.2f", avgRevenue));

        if (revenuePerSpot.isEmpty()) {
            pieChart.clear();
            lineChart.clear();
            pieChart.invalidate();
            lineChart.invalidate();
            return;
        }

        // Generate all dates from startDate to endDate inclusive
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));
        List<String> allDates = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        while (!cal.after(endCal)) {
            allDates.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        for (String date : allDates) {
            if (!revenuePerDay.containsKey(date)) {
                revenuePerDay.put(date, 0.0);
            }
        }

        Collections.sort(allDates);

        db.collection("parking_locations")
                .whereIn(FieldPath.documentId(), new ArrayList<>(revenuePerSpot.keySet()))
                .get().addOnSuccessListener(spotsSnapshot -> {
                    Map<String, String> spotNames = new HashMap<>();
                    for (DocumentSnapshot spotDoc : spotsSnapshot) {
                        spotNames.put(spotDoc.getId(), spotDoc.getString("name"));
                    }

                    List<PieEntry> pieEntries = new ArrayList<>();
                    for (String spotId : revenuePerSpot.keySet()) {
                        String name = spotNames.containsKey(spotId) ? spotNames.get(spotId) : "Spot " + spotId;
                        pieEntries.add(new PieEntry(revenuePerSpot.get(spotId).floatValue(), name));
                    }

                    PieDataSet pieDataSet = new PieDataSet(pieEntries, "Fee charged by Spot");
                    pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                    pieDataSet.setValueTextColor(Color.BLACK);
                    pieDataSet.setValueTextSize(12f);
                    pieChart.setData(new PieData(pieDataSet));
                    pieChart.getDescription().setEnabled(false);
                    pieChart.invalidate();

                    List<String> sortedDays = new ArrayList<>(allDates);

                    List<Entry> lineEntries = new ArrayList<>();
                    for (int i = 0; i < sortedDays.size(); i++) {
                        lineEntries.add(new Entry(i, revenuePerDay.get(sortedDays.get(i)).floatValue()));
                    }

                    LineDataSet lineDataSet = new LineDataSet(lineEntries, "Fee charged Over Time");
                    lineDataSet.setColor(Color.BLUE);
                    lineDataSet.setValueTextSize(10f);
                    lineDataSet.setCircleRadius(3f);

                    LineData lineData = new LineData(lineDataSet);
                    lineChart.setData(lineData);

                    lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value;
                            if (index >= 0 && index < sortedDays.size()) {
                                return sortedDays.get(index);
                            }
                            return "";
                        }
                    });
                    lineChart.getXAxis().setLabelRotationAngle(-45f);
                    lineChart.getXAxis().setGranularity(1f);
                    lineChart.getXAxis().setDrawLabels(true);

                    lineChart.getDescription().setEnabled(false);
                    lineChart.invalidate();

                });
    }
}
