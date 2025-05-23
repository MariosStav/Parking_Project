package com.example.parkhonolulu;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MyparklocationActivity extends BaseDrawerActivity implements OnMapReadyCallback {

    private static final LatLng HONOLULU = new LatLng(21.3069, -157.8583);
    private static final float DEFAULT_ZOOM = 14.0f;
    private GoogleMap mMap;
    private TextView currentFeeText;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_myparklocation, findViewById(R.id.content_frame), true);

        currentFeeText = findViewById(R.id.currentFeeText);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HONOLULU, DEFAULT_ZOOM));

        // UI controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // 🔍 Check for active session and place marker
        parking_session.fetchCurrentUserActiveSession(new parking_session.OnActiveSessionChecked() {
            @Override
            public void onActiveSessionFound(parking_session session) {
                // Now you have the session object

                // Fetch the parking location coordinates
                session.fetchParkingLocationLatLng(new parking_session.OnLocationFetchedListener() {
                    @Override
                    public void onSuccess(double latitude, double longitude) {
                        LatLng location = new LatLng(latitude, longitude);

                        // Show marker and move camera
                        mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                                .position(location)
                                .title("Your Active Parking Spot"));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));

                        // Calculate current fee
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
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("ParkingSession", "Failed to fetch parking location", e);
                        currentFeeText.setText("Error loading parking location");
                    }
                });
            }

            @Override
            public void onNoActiveSession() {
                Log.d("ParkingSession", "No active parking session found.");
                currentFeeText.setText("No active parking session");
            }

            @Override
            public void onError(Exception e) {
                Log.e("ParkingSession", "Failed to fetch active parking session", e);
                currentFeeText.setText("Error loading session");
            }
        });
    }
}
