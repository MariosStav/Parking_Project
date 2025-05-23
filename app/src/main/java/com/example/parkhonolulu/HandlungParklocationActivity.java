    package com.example.parkhonolulu;

    import android.content.Intent;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.BitmapDescriptorFactory;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.Marker;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.android.material.bottomsheet.BottomSheetDialog;

    import java.util.List;

    public class HandlungParklocationActivity extends BaseManagerDrawerActivity implements OnMapReadyCallback {

        private LatLng selectedLatLng;
        private Marker selectedMarker;
        private Button AddButton, DeleteButton;
        private GoogleMap mMap;
        private static final LatLng HONOLULU = new LatLng(21.3069, -157.8583);
        private static final float DEFAULT_ZOOM = 14.0f;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setupManagerDrawer(R.layout.drawer_base_manager);
            getLayoutInflater().inflate(R.layout.activity_handlung_parklocation, findViewById(R.id.content_frame), true);

            AddButton = findViewById(R.id.addParklocation);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            AddButton.setOnClickListener(v -> {
                if (selectedLatLng != null) {
                    Intent intent = new Intent(HandlungParklocationActivity.this, NewParkLocationActivity.class);
                    intent.putExtra("latitude", selectedLatLng.latitude);
                    intent.putExtra("longitude", selectedLatLng.longitude);
                    startActivity(intent);
                }
            });

            AddButton.setEnabled(false);

            DeleteButton = findViewById(R.id.delete);
            DeleteButton.setOnClickListener(v -> {
                if (selectedLatLng != null) {
                    showDeleteConfirmationDialog(selectedLatLng);
                }
            });

            DeleteButton.setEnabled(false);
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

            mMap.setOnMapClickListener(latLng -> {
                // Remove previous marker if exists
                if (selectedMarker != null) {
                    selectedMarker.remove();
                }

                // Add new marker where user tapped
                selectedMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("New Park Stop"));

                selectedLatLng = latLng;
                AddButton.setEnabled(true);
                DeleteButton.setEnabled(false);
            });

            mMap.setOnMarkerClickListener(marker -> {
                // Only allow deletion if this marker represents a parking spot (has a tag)
                Object tag = marker.getTag();
                if (tag instanceof parking_locations) {
                    selectedMarker = marker;
                    selectedLatLng = marker.getPosition();
                    DeleteButton.setEnabled(true);    // Enable delete
                    AddButton.setEnabled(false);      // Disable Add, since this is an existing spot
                    return false; // Return false to allow default behavior (show info window)
                } else {
                    // If it's not a parking spot, ignore or disable delete button
                    DeleteButton.setEnabled(false);
                    return true;  // consume event
                }
            });
        }

        @Override
        protected void onResume() {
            super.onResume();
            if (selectedMarker != null) {
                selectedMarker.remove();
                selectedMarker = null;
                selectedLatLng = null;
                AddButton.setEnabled(false);
            }
        }

        private void loadParkingSpots() {
            boolean includeUnavailable = false;

            parking_locations.loadAllSpots(includeUnavailable, new parking_locations.LoadSpotsCallback() {
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
                    runOnUiThread(() ->
                            Toast.makeText(HandlungParklocationActivity.this, "Αποτυχία φόρτωσης σημείων στάθμευσης", Toast.LENGTH_SHORT).show()
                    );
                    Log.e("AddParklocationActivity", "Failed to load all parking spots", e);
                }
            });
        }
        private void showDeleteConfirmationDialog(LatLng location) {
            View view = getLayoutInflater().inflate(R.layout.bottom_sheet_delete_parking, null);
            EditText latEdit = view.findViewById(R.id.lag);
            EditText lngEdit = view.findViewById(R.id.lgn);
            TextView info = view.findViewById(R.id.selectedSpotInfo);
            Button confirmBtn = view.findViewById(R.id.confirmDelete);

            latEdit.setText(String.valueOf(location.latitude));
            lngEdit.setText(String.valueOf(location.longitude));
            info.setText("Selected location: " + location.latitude + ", " + location.longitude);

            BottomSheetDialog dialog = new BottomSheetDialog(this);
            dialog.setContentView(view);
            dialog.show();

            confirmBtn.setOnClickListener(v -> {
                if (selectedMarker != null) {
                    Object tag = selectedMarker.getTag();
                    if (tag instanceof parking_locations) {
                        parking_locations spot = (parking_locations) tag;
                        spot.deleteFromDatabase(new parking_locations.OnDeleteCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> {
                                    dialog.dismiss();
                                    Toast.makeText(HandlungParklocationActivity.this, "Parking location deleted successfully.", Toast.LENGTH_SHORT).show();

                                    // Remove marker from map
                                    selectedMarker.remove();
                                    selectedMarker = null;
                                    selectedLatLng = null;

                                    DeleteButton.setEnabled(false);
                                    AddButton.setEnabled(false);

                                    // Reload spots
                                    loadParkingSpots();
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                runOnUiThread(() ->
                                        Toast.makeText(HandlungParklocationActivity.this, "Failed to delete parking location: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    } else {
                        Toast.makeText(this, "Selected marker is not a parking location.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(this, "No parking location selected.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }

    }
