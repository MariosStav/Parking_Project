package com.example.parkhonolulu;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private CheckBox checkboxShowUnavailable;
    private Button parkHereButton;
    private Marker selectedMarker;
    private GoogleMap mMap;

    private static final LatLng HONOLULU = new LatLng(21.3069, -157.8583);
    private static final float DEFAULT_ZOOM = 14.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        parkHereButton = findViewById(R.id.parkHereButton);
        parkHereButton.setOnClickListener(v -> {
            if (selectedMarker != null) {
                String markerTitle = selectedMarker.getTitle();
                Intent intent = new Intent(MapsActivity.this, ParkActivity.class);
                startActivity(intent);
            }
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
            Object tag = marker.getTag();
            boolean isFree = tag instanceof Boolean && (Boolean) tag;
            parkHereButton.setEnabled(isFree);
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
                        marker.setTag(spot.isFree());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MapsActivity.this, "Αποτυχία φόρτωσης σημείων στάθμευσης", Toast.LENGTH_SHORT).show();
                    Log.e("MapsActivity", "Failed to load parking spots", e);
                });
            }
        });
    }
}
