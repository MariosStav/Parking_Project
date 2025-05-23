package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MapsActivity extends BaseDrawerActivity implements OnMapReadyCallback {

    private CheckBox checkboxShowUnavailable;
    private Button parkHereButton;
    private Marker selectedMarker;
    private GoogleMap mMap;
    private parking_locations selectedSpot;
    private static final LatLng HONOLULU = new LatLng(21.3069, -157.8583);
    private static final float DEFAULT_ZOOM = 14.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_maps, findViewById(R.id.content_frame), true);

        parkHereButton = findViewById(R.id.parkHereButton);
        parkHereButton.setOnClickListener(v -> {
            DocumentReference userRef = User.getUserRef();
            parking_session.checkActiveSession(userRef, new parking_session.OnActiveSessionChecked() {
                @Override
                public void onActiveSessionFound(parking_session session) {
                    runOnUiThread(() -> {
                        Toast.makeText(MapsActivity.this, "You already have an active session!", Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onNoActiveSession() {
                    if (selectedSpot != null && selectedSpot.isFree()) {
                        runOnUiThread(() -> parkHereButton.setEnabled(true));
                        Intent intent = new Intent(MapsActivity.this, ParkActivity.class);
                        intent.putExtra("parkingSpot", selectedSpot);
                        startActivity(intent);
                    } else {
                        runOnUiThread(() -> parkHereButton.setEnabled(false));
                    }
                }


                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MapsActivity.this, "Error checking session", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });


        checkboxShowUnavailable = findViewById(R.id.unavailableCheckbox);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        checkboxShowUnavailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mMap != null) {
                loadParkingSpots();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HONOLULU, DEFAULT_ZOOM));
        mMap.addMarker(new MarkerOptions().position(HONOLULU).title("Honolulu, Hawaii"));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        loadParkingSpots();

        mMap.setOnMarkerClickListener(marker -> {
            selectedMarker = marker;
            parkHereButton.setEnabled(false); // Disable immediately

            Object tag = marker.getTag();
            if (tag instanceof parking_locations) {
                selectedSpot = (parking_locations) tag;

                DocumentReference userRef = User.getUserRef();

                parking_session.checkActiveSession(userRef, new parking_session.OnActiveSessionChecked() {
                    @Override
                    public void onActiveSessionFound(parking_session session) {
                        runOnUiThread(() -> {
                            if (session.getParking_location_id().getId().equals(selectedSpot.getId())) {
                                Toast.makeText(MapsActivity.this,
                                        "You have already parked here.",
                                        Toast.LENGTH_LONG).show();
                                parkHereButton.setEnabled(false);
                            } else {
                                Toast.makeText(MapsActivity.this,
                                        "You are already parked elsewhere.",
                                        Toast.LENGTH_LONG).show();
                                selectedSpot = null;
                                parkHereButton.setEnabled(false); // Prevent new parking
                            }
                        });
                    }


                    public void onNoActiveSession() {
                        parkHereButton.setEnabled(true);
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(MapsActivity.this, "Failed to check active session", Toast.LENGTH_SHORT).show();
                            parkHereButton.setEnabled(false); // Don't allow uncertain state
                        });
                    }
                });

            } else {
                selectedSpot = null;
                parkHereButton.setEnabled(false);
            }

            return false;
        });
    }

    private void loadParkingSpots() {
        boolean includeUnavailable = checkboxShowUnavailable.isChecked();

        parking_locations.loadAvailableSpotsBasedOnUser(includeUnavailable, new parking_locations.LoadSpotsCallback() {
            @Override
            public void onSuccess(List<parking_locations> spots) {
                runOnUiThread(() -> {
                    mMap.clear();
                    for (parking_locations spot : spots) {
                        LatLng position = new LatLng(
                                spot.getGeopoint().getLatitude(),
                                spot.getGeopoint().getLongitude()
                        );

                        float color;
                        if (!spot.isFree()) {
                            color = BitmapDescriptorFactory.HUE_RED;
                        } else {
                            switch (spot.getType().toLowerCase()) {
                                case "electric":
                                    color = BitmapDescriptorFactory.HUE_YELLOW;
                                    break;
                                case "gas":
                                    color = BitmapDescriptorFactory.HUE_GREEN;
                                    break;
                                default:
                                    color = BitmapDescriptorFactory.HUE_ORANGE;
                            }
                        }

                        String title = spot.getName();
                        if (!spot.isFree()) {
                            title += " (Unavailable)";
                        }

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(position)
                                .title(title)
                                .icon(BitmapDescriptorFactory.defaultMarker(color)));

                        marker.setTag(spot);
                    }

                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MapsActivity.this, "Failed to load parking spots", Toast.LENGTH_SHORT).show();
                    Log.e("MapsActivity", "Failed to load parking spots", e);
                });
            }
        });
    }

}
