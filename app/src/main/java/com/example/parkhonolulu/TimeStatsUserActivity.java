package com.example.parkhonolulu;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TimeStatsUserActivity extends BaseDrawerActivity {

    private Spinner monthSpinner;
    private TextView totalSessionsValue, avgDurationValue, lastSessionValue, firstSessionValue;
    private BarChart barChartSessions;
    private LineChart lineChartAvgDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_time_stats_user, findViewById(R.id.content_frame), true);

        // Initialize Views
        totalSessionsValue = findViewById(R.id.totalSessionsValue);
        avgDurationValue = findViewById(R.id.avgDurationValue);
        lastSessionValue = findViewById(R.id.lastSessionValue);
        firstSessionValue = findViewById(R.id.firstSessionValue);
        barChartSessions = findViewById(R.id.barChartSessions);
        lineChartAvgDuration = findViewById(R.id.lineChartAvgDuration);

        monthSpinner = findViewById(R.id.monthSpinner);

        // Populate Spinner with month options (e.g. last 12 months or fixed list)
        setupMonthSpinner();

        // Load dummy data (replace with Firebase later)
        loadStatistics();
        setupBarChart();
        setupLineChart();
    }

    private void setupBarChart() {
        barChartSessions.getDescription().setEnabled(false);
        barChartSessions.getAxisRight().setEnabled(false);

        XAxis xAxis = barChartSessions.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = barChartSessions.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);

        barChartSessions.setFitBars(true);
    }

    private void loadStatistics() {
        // Example static data - replace with Firebase fetch later
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

        parking_session.fetchFirstSessionForCurrentUser(new parking_session.OnFirstSessionFetched() {
            @Override
            public void onSuccess(String firstSessionDateTime) {
                firstSessionValue.setText(firstSessionDateTime);
            }

            @Override
            public void onFailure(Exception e) {
                firstSessionValue.setText("Error");
                Log.e("LastSession", "Failed to fetch last session date", e);
            }
        });
    }

    private void updateBarChartForMonth(int year, int month) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        parking_session.fetchSessionCountsPerDateInMonth(userId, year, month, dateCounts -> {
            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            int index = 0;

            for (Map.Entry<String, Integer> entry : dateCounts.entrySet()) {
                entries.add(new BarEntry(index, entry.getValue()));
                labels.add(entry.getKey()); // day of month
                index++;
            }

            BarDataSet dataSet = new BarDataSet(entries, "Sessions per Day");
            dataSet.setColor(Color.BLUE);

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.9f);
            barChartSessions.setData(data);

            // Setup axis labels
            XAxis xAxis = barChartSessions.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(labels.size());
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int idx = (int) value;
                    if (idx >= 0 && idx < labels.size()) {
                        return labels.get(idx);
                    }
                    return "";
                }
            });

            barChartSessions.getAxisRight().setEnabled(false);
            barChartSessions.getDescription().setEnabled(false);
            barChartSessions.setFitBars(true);
            barChartSessions.animateY(1000);
            barChartSessions.invalidate();

        }, e -> {
            Log.e("BarChart", "Failed to load session data", e);
            Toast.makeText(this, "Error loading session stats", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupLineChart() {
        // Start with empty entries
        List<Entry> entries = new ArrayList<>();
        LineDataSet dataSet = new LineDataSet(entries, "Avg Duration (min)");

        dataSet.setColor(getResources().getColor(R.color.teal_700));
        dataSet.setCircleColor(getResources().getColor(R.color.teal_700));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChartAvgDuration.setData(lineData);

        // Chart style
        lineChartAvgDuration.getDescription().setEnabled(false);
        lineChartAvgDuration.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartAvgDuration.getAxisRight().setEnabled(false);
        lineChartAvgDuration.getAxisLeft().setGranularity(1f);
        lineChartAvgDuration.animateX(1000);
        lineChartAvgDuration.invalidate();
    }


    private void updateLineChartForMonth(int year, int month) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        parking_session.fetchAverageDurationsPerDateInMonth(userId, year, month, dateAverages -> {
            List<Entry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            int index = 0;

            for (Map.Entry<String, Float> entry : dateAverages.entrySet()) {
                entries.add(new Entry(index, entry.getValue()));
                labels.add(entry.getKey()); // day of month
                index++;
            }

            LineDataSet dataSet = new LineDataSet(entries, "Avg Duration (min)");
            dataSet.setColor(getResources().getColor(R.color.teal_700));
            dataSet.setCircleColor(getResources().getColor(R.color.teal_700));
            dataSet.setLineWidth(2f);
            dataSet.setValueTextSize(12f);

            LineData lineData = new LineData(dataSet);
            lineChartAvgDuration.setData(lineData);

            // Format X axis
            XAxis xAxis = lineChartAvgDuration.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(labels.size());
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int idx = (int) value;
                    if (idx >= 0 && idx < labels.size()) {
                        return labels.get(idx);
                    }
                    return "";
                }
            });

            lineChartAvgDuration.getAxisRight().setEnabled(false);
            lineChartAvgDuration.getDescription().setEnabled(false);
            lineChartAvgDuration.animateX(1000);
            lineChartAvgDuration.invalidate();

        }, e -> {
            Log.e("LineChart", "Failed to load avg durations", e);
            Toast.makeText(this, "Error loading duration stats", Toast.LENGTH_SHORT).show();
        });
    }


    private void setupMonthSpinner() {
        // For simplicity, use last 12 months formatted as "YYYY-MM" or "MMM yyyy"
        List<String> monthLabels = new ArrayList<>();
        final List<int[]> yearMonthPairs = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH); // 0-based

            // Format label e.g. "May 2025"
            String label = new java.text.SimpleDateFormat("MMM yyyy").format(cal.getTime());
            monthLabels.add(label);
            yearMonthPairs.add(new int[]{year, month});

            // Go back one month
            cal.add(Calendar.MONTH, -1);
        }

        // Reverse lists so earliest month is first
        java.util.Collections.reverse(monthLabels);
        java.util.Collections.reverse(yearMonthPairs);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                monthLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter);

        // Select current month by default (last in reversed list)
        monthSpinner.setSelection(monthLabels.size() - 1);

        // Listener to update chart on month selection
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                int[] ym = yearMonthPairs.get(position);
                int year = ym[0];
                int month = ym[1];
                updateBarChartForMonth(year, month);
                updateLineChartForMonth(year, month);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }


}
