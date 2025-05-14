package com.example.parkhonolulu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HONOLULU, DEFAULT_ZOOM));

        mMap.addMarker(new MarkerOptions()
                .position(HONOLULU)
                .title("Honolulu, Hawaii"));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking_locations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String type = document.getString("type"); // Get the type field
                        GeoPoint geoPoint = document.getGeoPoint("geopoint");

                        if (geoPoint != null && name != null && type != null) {
                            LatLng position = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

                            // Choose marker color based on type
                            float color;
                            if (type.equalsIgnoreCase("Electric")) {
                                color = BitmapDescriptorFactory.HUE_YELLOW; // Pastel Yellow representation
                            } else if (type.equalsIgnoreCase("Gas")) {
                                color = BitmapDescriptorFactory.HUE_GREEN; // Pastel Green representation
                            } else {
                                color = BitmapDescriptorFactory.HUE_ORANGE; // default/fallback
                            }

                            mMap.addMarker(new MarkerOptions()
                                    .position(position)
                                    .title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(color)));
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(MapsActivity.this, "Failed to load parking locations", Toast.LENGTH_SHORT).show());
    }
}
