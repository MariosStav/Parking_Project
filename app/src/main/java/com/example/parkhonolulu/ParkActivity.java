    package com.example.parkhonolulu;


    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.firebase.Timestamp;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.FirebaseFirestore;

    public class ParkActivity extends BaseDrawerActivity {
        private Button Park;

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setupDrawer(R.layout.drawer_base);
            getLayoutInflater().inflate(R.layout.activity_park, findViewById(R.id.content_frame), true);

            TextView selectedSpotInfo = findViewById(R.id.selectedSpotInfo);

            parking_locations spot = getIntent().getParcelableExtra("parkingSpot");

            if (spot != null) {
                String name = spot.getName();
                double lat = spot.getGeopoint().getLatitude();
                double lng = spot.getGeopoint().getLongitude();

                selectedSpotInfo.setText("Selected: " + name + " (" + lat + ", " + lng + ")");
            } else {
                selectedSpotInfo.setText("No parking location found");
            }

            ImageView electricCost = findViewById(R.id.electriccost);
            ImageView gasCost = findViewById(R.id.gascost);

            if (spot.getType().equalsIgnoreCase("electric")) {
                electricCost.setVisibility(View.VISIBLE);
                gasCost.setVisibility(View.GONE);
            } else if (spot.getType().equalsIgnoreCase("gas")) {
                gasCost.setVisibility(View.VISIBLE);
                electricCost.setVisibility(View.GONE);
            }

            Park = findViewById(R.id.park);
            Park.setOnClickListener(v -> {
                Park.setEnabled(false);
                final double parkingFee = 20.0;

                Balance.deductFromCurrentUserBalance(parkingFee,
                        () -> {
                            if (spot == null || spot.getId() == null || spot.getId().isEmpty()) {
                                Toast.makeText(ParkActivity.this, "Invalid parking spot", Toast.LENGTH_LONG).show();
                                return;
                            }

                            DocumentReference userRef = User.getUserRef();
                            DocumentReference locationRef = spot.getLocationRef();

                            User.fetchVehicleRef(vehicleRef -> {
                                parking_session session = new parking_session(userRef, vehicleRef, locationRef, Timestamp.now());
                                session.setSecurity_deposit(20.0);

                                session.saveSession(new parking_session.OnSessionSavedListener() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(ParkActivity.this,
                                                "Parking successful!\n$20 security deposit has been reserved on your account.",
                                                Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ParkActivity.this, MyparklocationActivity.class));
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        e.printStackTrace();
                                    }
                                });

                            }, e -> Toast.makeText(ParkActivity.this, "Failed to fetch vehicle info: " + e.getMessage(), Toast.LENGTH_LONG).show());

                        },
                        e -> {
                            Log.d("BalanceDebug", "FAILURE: some error: " + e.getMessage());
                            Toast.makeText(ParkActivity.this, "Error checking balance: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        },
                        () -> {
                            Log.d("BalanceDebug", "INSUFFICIENT FUNDS");
                            new androidx.appcompat.app.AlertDialog.Builder(ParkActivity.this)
                                    .setTitle("Insufficient Funds")
                                    .setMessage("Not enough money in balance. Please add funds.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                );
            });
        }
    }