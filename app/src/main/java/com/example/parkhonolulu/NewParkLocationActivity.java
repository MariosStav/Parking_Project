package com.example.parkhonolulu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

public class NewParkLocationActivity extends BaseManagerDrawerActivity {

    private Button Save;
    private EditText editTextLat;
    private EditText editTextLng;
    private EditText editTextName;
    private Spinner typeSpinner;
    private TextView selectedSpotInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupManagerDrawer(R.layout.drawer_base_manager);
        getLayoutInflater().inflate(R.layout.activity_newparklocation, findViewById(R.id.content_frame), true);

        // Get latitude and longitude from the intent
        double lat = getIntent().getDoubleExtra("latitude", 0.0);
        double lng = getIntent().getDoubleExtra("longitude", 0.0);
        LatLng newLocation = new LatLng(lat, lng);

        // Bind views
        editTextLat = findViewById(R.id.lag);
        editTextLng = findViewById(R.id.lgn);
        editTextName = findViewById(R.id.parkingName);
        typeSpinner = findViewById(R.id.spinner);
        selectedSpotInfo = findViewById(R.id.selectedSpotInfo);

        // Set values to EditTexts
        editTextLat.setText(String.valueOf(lat));
        editTextLng.setText(String.valueOf(lng));

        // Update the info TextView
        selectedSpotInfo.setText("Selected spot: " + lat + ", " + lng);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.car_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        Save = findViewById(R.id.save);
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String type = typeSpinner.getSelectedItem().toString();
                double latitude = Double.parseDouble(editTextLat.getText().toString());
                double longitude = Double.parseDouble(editTextLng.getText().toString());

                if (name.isEmpty()) {
                    editTextName.setError("Name required");
                    return;
                }

                // Create parking location object
                GeoPoint geopoint = new GeoPoint(latitude, longitude);
                parking_locations newLocation = new parking_locations(name, type.toLowerCase(), geopoint, true); // free = true

                // Save to Firestore
                newLocation.saveToDatabase(new parking_locations.OnSaveCallback() {
                    @Override
                    public void onSuccess(String documentId) {
                        // You could show a toast or navigate back
                        Intent intent = new Intent(NewParkLocationActivity.this, HandlungParklocationActivity.class);
                        startActivity(intent);
                        finish(); // optional
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace(); // or show a Toast
                    }
                });
            }
        });
    }
}