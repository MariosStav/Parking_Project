package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class UnparkActvity extends BaseDrawerActivity {

    private Button Unpark;
    private TextView currentFeeText;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_unpark, findViewById(R.id.content_frame), true);

        currentFeeText = findViewById(R.id.currentFeeText);
        ImageView electricCost = findViewById(R.id.electriccost);
        ImageView gasCost = findViewById(R.id.gascost);

        parking_session.fetchCurrentUserActiveSession(new parking_session.OnActiveSessionChecked() {
            @Override
            public void onActiveSessionFound(parking_session session) {

                session.calculateCurrentFee(new parking_session.FeeCalculationCallback() {
                    @Override
                    public void onFeeCalculated(double fee, long hours) {
                        currentFeeText.setText(String.format("Fee: $%.2f (%.0f hour%s)", fee, (double) hours, hours > 1 ? "s" : ""));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        currentFeeText.setText("Failed to calculate fee");
                        Log.e("ParkingSession", "Fee calculation error", e);
                    }
                });

                String parkingLocationId = session.getParking_location_id().getId();

                parking_locations.loadTypeById(parkingLocationId, type -> {
                    if ("electric".equalsIgnoreCase(type)) {
                        electricCost.setVisibility(View.VISIBLE);
                        gasCost.setVisibility(View.GONE);
                    } else if ("gas".equalsIgnoreCase(type)) {
                        gasCost.setVisibility(View.VISIBLE);
                        electricCost.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onNoActiveSession() {
                runOnUiThread(() -> {
                    Toast.makeText(UnparkActvity.this, "You have not park somewhere", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(UnparkActvity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });

        Unpark = findViewById(R.id.unpark_btn);
        Unpark.setOnClickListener(v -> {
            parking_session.fetchCurrentUserActiveSession(new parking_session.OnActiveSessionChecked() {
                @Override
                public void onActiveSessionFound(parking_session session) {
                    session.setId(session.getId()); // Required to update document reference

                    session.endSession(new parking_session.OnSessionEndedListener() {
                        @Override
                        public void onSuccess(double fee) {
                            runOnUiThread(() -> {
                                new androidx.appcompat.app.AlertDialog.Builder(UnparkActvity.this)
                                        .setTitle("Unparked Successfully")
                                        .setMessage("You have been unparked.\nFee charged: $" + fee)
                                        .setPositiveButton("OK", (dialog, which) -> {
                                            startActivity(new Intent(UnparkActvity.this, MapsActivity.class));
                                            finish();
                                        })
                                        .show();
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> Toast.makeText(UnparkActvity.this, "Error ending session: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    });
                }

                @Override
                public void onNoActiveSession() {
                    runOnUiThread(() -> Toast.makeText(UnparkActvity.this, "No active parking session found", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> Toast.makeText(UnparkActvity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });
    }
}
