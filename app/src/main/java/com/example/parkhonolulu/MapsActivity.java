package com.example.parkhonolulu;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    // Coordinates for Honolulu, Hawaii
    private static final LatLng HONOLULU = new LatLng(21.3069, -157.8583);
    private static final float DEFAULT_ZOOM = 14.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up button click listeners
        Button btnLeft = findViewById(R.id.btnLeft);
        Button btnRight = findViewById(R.id.btnRight);
        Button btnBack = findViewById(R.id.btnBack);

        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Left button clicked", Toast.LENGTH_SHORT).show();
                // Add your parking-specific functionality here
            }
        });

        btnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Right button clicked", Toast.LENGTH_SHORT).show();
                // Add your parking-specific functionality here
            }
        });

        // Add back button functionality
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Going back", Toast.LENGTH_SHORT).show();
                // Method 1: Finish current activity to go back
                finish();

                // Method 2 (Alternative): Use Android's built-in back navigation
                // onBackPressed();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Move camera to Honolulu with appropriate zoom level
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HONOLULU, DEFAULT_ZOOM));

        // Add a marker in Honolulu
        mMap.addMarker(new MarkerOptions()
                .position(HONOLULU)
                .title("Honolulu, Hawaii"));

        // Enable necessary UI controls for a parking app
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking_locations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        GeoPoint geoPoint = document.getGeoPoint("geopoint");

                        if (geoPoint != null && name != null) {
                            LatLng position = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title(name));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MapsActivity.this, "Failed to load parking locations", Toast.LENGTH_SHORT).show();
                });

    }
}