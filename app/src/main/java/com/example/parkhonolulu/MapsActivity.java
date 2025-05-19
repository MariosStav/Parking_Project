package com.example.parkhonolulu;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MapsActivity extends BaseDrawerActivity implements OnMapReadyCallback {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    private String currentUserCarType;
    private GoogleMap mMap;

    // Coordinates for Honolulu, Hawaii
    private static final LatLng HONOLULU = new LatLng(21.3069, -157.8583);
    private static final float DEFAULT_ZOOM = 14.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.drawer_base);
        getLayoutInflater().inflate(R.layout.activity_maps, findViewById(R.id.content_frame), true);
        auth = FirebaseAuth.getInstance();

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
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

        // Enable UI controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // ✅ Φόρτωση car type και parking spots
        fetchUserCarTypeAndLoadSpots();
    }

    private void fetchUserCarTypeAndLoadSpots() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String vehicleId = userDoc.getString("vehicleid");

                if (vehicleId != null && !vehicleId.isEmpty()) {
                    db.collection("vehicles").document(vehicleId).get().addOnSuccessListener(vehicleDoc -> {
                        if (vehicleDoc.exists()) {
                            currentUserCarType = vehicleDoc.getString("carType");
                            Log.d("MapsActivity", "Current user's car type: " + currentUserCarType);
                            loadParkingSpots(); // ✅ ΤΩΡΑ φορτώνουμε τα spots
                        } else {
                            Log.w("MapsActivity", "Vehicle not found.");
                            Toast.makeText(this, "Το όχημα δεν βρέθηκε.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Log.e("MapsActivity", "Error fetching vehicle", e);
                        Toast.makeText(this, "Σφάλμα κατά τη φόρτωση οχήματος.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Log.w("MapsActivity", "Vehicle ID is null or empty.");
                    Toast.makeText(this, "Το όχημα δεν έχει οριστεί.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w("MapsActivity", "User document does not exist.");
                Toast.makeText(this, "Ο χρήστης δεν βρέθηκε στη βάση.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("MapsActivity", "Error fetching user document", e);
            Toast.makeText(this, "Σφάλμα κατά τη φόρτωση χρήστη.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadParkingSpots() {
        db.collection("parking_locations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String type = document.getString("type");
                        GeoPoint geoPoint = document.getGeoPoint("geopoint");

                        Log.d("MapsActivity", "Processing parking: " + name + ", Type: " + type);
                        if (geoPoint != null && name != null && type != null) {
                            boolean shouldAddMarker = false;
                            Log.d("MapsActivity", "Comparing with currentUserCarType: " + currentUserCarType);

                            if ("Gas".equalsIgnoreCase(currentUserCarType)) {
                                if ("Gas".equalsIgnoreCase(type)) {
                                    shouldAddMarker = true;
                                    Log.d("MapsActivity", "User has Gas car, parking is Gas. Adding marker.");
                                } else {
                                    Log.d("MapsActivity", "User has Gas car, parking is Electric. Skipping.");
                                }
                            } else if ("Electric".equalsIgnoreCase(currentUserCarType)) {
                                if ("Electric".equalsIgnoreCase(type)) {
                                    shouldAddMarker = true;
                                    Log.d("MapsActivity", "User has Electric car, parking is Electric. Adding marker.");
                                } else {
                                    Log.d("MapsActivity", "User has Electric car, parking is Gas. Skipping.");
                                }
                                if ("Gas".equalsIgnoreCase(type)) {
                                    shouldAddMarker = true;
                                    Log.d("MapsActivity", "User has Gas car, parking is Gas. Adding marker.");
                                } else {
                                    Log.d("MapsActivity", "User has Gas car, parking is Electric. Skipping.");
                                }
                            } else {
                                shouldAddMarker = true;
                                Log.d("MapsActivity", "User car type unknown, showing all markers.");
                            }

                            if (shouldAddMarker) {
                                LatLng position = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

                                float color;
                                if ("Electric".equalsIgnoreCase(type)) {
                                    color = BitmapDescriptorFactory.HUE_YELLOW;
                                } else if ("Gas".equalsIgnoreCase(type)) {
                                    color = BitmapDescriptorFactory.HUE_GREEN;
                                } else {
                                    color = BitmapDescriptorFactory.HUE_ORANGE;
                                }

                                mMap.addMarker(new MarkerOptions()
                                        .position(position)
                                        .title(name)
                                        .icon(BitmapDescriptorFactory.defaultMarker(color)));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MapsActivity.this, "Αποτυχία φόρτωσης σημείων στάθμευσης", Toast.LENGTH_SHORT).show();
                    Log.e("MapsActivity", "Failed to load parking locations", e);
                });
    }
}
