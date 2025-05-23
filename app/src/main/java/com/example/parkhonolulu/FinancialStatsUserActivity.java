package com.example.parkhonolulu;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FinancialStatsUserActivity extends BaseDrawerActivity {

    private TextView tvTotalSpendAmount, tvAvgSpendAmount, tvTotalTimeAmount;
    private Spinner spinnerMonthFilter;
    private LineChart lineChartExpenses;

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
        loadFinancialData(null);  // null means no filter, show all by default
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
                loadFinancialData(selectedMonth.equals("All Months") ? null : selectedMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadFinancialData(null);
            }
        });
    }

    private void setupLineChart() {
        // Basic setup for the LineChart, customize as needed
        lineChartExpenses.getDescription().setEnabled(false);
        lineChartExpenses.setNoDataText("No data available");
        // Additional styling here if you want
    }

    /**
     * Load and display financial data filtered by month (or all if monthFilter is null)
     * For now, this is a stub with dummy data.
     */
    private void loadFinancialData(String monthFilter) {
        // TODO: Replace this stub with actual data loading & filtering logic

        // Dummy values example
        if (monthFilter == null) {
            tvTotalSpendAmount.setText("$1234.56");
            tvAvgSpendAmount.setText("$45.67");
            tvTotalTimeAmount.setText("12h 34m");
            // TODO: populate line chart with all-time data
        } else {
            tvTotalSpendAmount.setText("$200.00 (" + monthFilter + ")");
            tvAvgSpendAmount.setText("$20.00");
            tvTotalTimeAmount.setText("2h 15m");
            // TODO: populate line chart with filtered data for monthFilter
        }

        // You would update the lineChartExpenses here based on filtered data
    }
}
