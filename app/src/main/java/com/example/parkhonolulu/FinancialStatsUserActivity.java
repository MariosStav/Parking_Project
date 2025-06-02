package com.example.parkhonolulu;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FinancialStatsUserActivity extends BaseDrawerActivity {

    private TextView tvTotalSpendAmount, tvAvgSpendAmount, tvTotalTimeAmount;
    private Spinner spinnerMonthFilter;
    private LineChart lineChartExpenses;

    private final List<Entry> chartEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_financial_stats_user, findViewById(R.id.content_frame), true);

        // Initialize views
        tvTotalSpendAmount = findViewById(R.id.tvTotalSpendAmount);
        tvAvgSpendAmount = findViewById(R.id.tvAvgSpendAmount);
        tvTotalTimeAmount = findViewById(R.id.tvTotalTimeAmount);

        spinnerMonthFilter = findViewById(R.id.spinnerMonthFilter);
        lineChartExpenses = findViewById(R.id.lineChartExpenses);

        setupMonthFilterSpinner();
        setupLineChart();
    }

    private void setupMonthFilterSpinner() {
        // Month names including an "All" option for no filtering
        List<String> months = new ArrayList<>(Arrays.asList(
                "All Months",
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, months
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonthFilter.setAdapter(adapter);

        spinnerMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMonth = (String) parent.getItemAtPosition(position);
                // Update chart and cards based on selected month filter
                loadFinancialData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadFinancialData(0);
            }
        });
    }

    private void setupLineChart() {
        lineChartExpenses.getDescription().setEnabled(false);
        lineChartExpenses.setNoDataText("No data available");
        XAxis xAxis = lineChartExpenses.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.US);

            @Override
            public String getFormattedValue(float value) {
                return sdf.format(new Date((long) value));
            }
        });
    }


    private void updateLineChart() {
        if (chartEntries.isEmpty()) {
            lineChartExpenses.clear();
            return;
        }

        LineDataSet dataSet = new LineDataSet(chartEntries, "Session Spend ($)");
        dataSet.setColor(getResources().getColor(R.color.teal_700));
        dataSet.setCircleColor(getResources().getColor(R.color.teal_700));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChartExpenses.setData(lineData);
        lineChartExpenses.invalidate();
    }

    private void loadFinancialData(int monthIndex) {

        chartEntries.clear();
        lineChartExpenses.clear();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();

        int chartIndex = 0;

        // Prepare date range if a month is selected
        Date startDate = null;
        Date endDate = null;

        if (monthIndex != 0) { // 0 = All Months
            TimeZone greeceTimeZone = TimeZone.getTimeZone("Europe/Athens");
            Calendar calendar = Calendar.getInstance(greeceTimeZone);
            calendar.setTime(new Date()); // current date
            calendar.set(Calendar.MONTH, monthIndex - 1); // January = 1 => index 0
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            startDate = calendar.getTime();

            calendar.add(Calendar.MONTH, 1);
            endDate = calendar.getTime();
        }

        final Date finalStartDate = startDate;
        final Date finalEndDate = endDate;
        final int finalMonthIndex = monthIndex;

        db.collection("parking_sessions")
                .whereEqualTo("user_id", db.document("users/" + currentUserId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalSpend = 0;
                    int sessionCount = 0;
                    long totalTimeMillis = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp startTimestamp = doc.getTimestamp("start_time");
                        Timestamp endTimestamp = doc.getTimestamp("end_time");
                        Number feeCharged = doc.getDouble("fee_charged");

                        if (startTimestamp != null && endTimestamp != null && feeCharged != null) {
                            Date start = startTimestamp.toDate();
                            Date end = endTimestamp.toDate();

                            boolean inMonth = finalMonthIndex == 0 || // All Months
                                    (finalStartDate != null && finalEndDate != null &&
                                            !end.before(finalStartDate) && end.before(finalEndDate));

                            if (inMonth) {
                                double fee = feeCharged.doubleValue();
                                totalSpend += fee;

                                long xValue = end.getTime();
                                chartEntries.add(new Entry(xValue, (float) fee));

                                long durationMillis = end.getTime() - start.getTime();
                                totalTimeMillis += durationMillis;

                                sessionCount++;
                            }
                        }
                    }

                    // Calculate average spend
                    double avgSpend = sessionCount > 0 ? totalSpend / sessionCount : 0;

                    // Convert total parking time to hours and minutes
                    long totalMinutes = totalTimeMillis / (1000 * 60);
                    long hours = totalMinutes / 60;
                    long minutes = totalMinutes % 60;

                    // Display results
                    Collections.sort(chartEntries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));
                    updateLineChart();
                    String[] monthNames = getResources().getStringArray(R.array.month_names);
                    String monthLabel = finalMonthIndex != 0 ? " (" + monthNames[finalMonthIndex - 1] + ")" : "";

                    tvTotalSpendAmount.setText(String.format(Locale.US, "$%.2f%s", totalSpend, monthLabel));
                    tvAvgSpendAmount.setText(String.format(Locale.US, "$%.2f avg", avgSpend));
                    tvTotalTimeAmount.setText(String.format(Locale.US, "%dh %dm total", hours, minutes));
                })
                .addOnFailureListener(e -> {
                    tvTotalSpendAmount.setText("Error");
                    tvAvgSpendAmount.setText("Error");
                    tvTotalTimeAmount.setText("Error");
                    Log.e("Firestore", "Failed to load financial stats", e);
                });
    }
}
