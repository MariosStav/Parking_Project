package com.example.parkhonolulu;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class parking_locations {

    private String name;
    private String type;
    private GeoPoint geopoint;
    private boolean free;

    // Firestore instance & collection name
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "parking_locations";

    // Constructors, getters, setters omitted for brevity...

    public parking_locations() {}

    public parking_locations(String name, String type, GeoPoint geopoint, boolean free) {
        this.name = name;
        this.type = type;
        this.geopoint = geopoint;
        this.free = free;
    }

    public interface LoadSpotsCallback {
        void onSuccess(List<parking_locations> spots);
        void onFailure(Exception e);
    }

    public static void loadAvailableSpotsBasedOnUser(boolean includeUnavailable, LoadSpotsCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure(new Exception("No user logged in"));
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        callback.onFailure(new Exception("User document not found"));
                        return;
                    }

                    String vehicleId = userDoc.getString("vehicleid");
                    if (vehicleId == null) {
                        callback.onFailure(new Exception("Vehicle ID not set for user"));
                        return;
                    }

                    db.collection("vehicles").document(vehicleId).get()
                            .addOnSuccessListener(vehicleDoc -> {
                                if (!vehicleDoc.exists()) {
                                    callback.onFailure(new Exception("Vehicle document not found"));
                                    return;
                                }

                                String carType = vehicleDoc.getString("carType");
                                Log.d("parking_locations", "User carType from vehicle: " + carType);

                                List<String> typesToQuery = new ArrayList<>();
                                if (carType != null && carType.equalsIgnoreCase("Electric")) {
                                    typesToQuery.add("electric");
                                    typesToQuery.add("gas");
                                } else {
                                    typesToQuery.add("gas");
                                }

                                Log.d("parking_locations", "Querying parking types: " + typesToQuery);

                                db.collection("parking_locations")
                                        .whereIn("type", typesToQuery)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            List<parking_locations> spots = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                                Boolean freeValue = doc.getBoolean("free");
                                                boolean isFree = freeValue != null && freeValue;

                                                if (!includeUnavailable && !isFree) continue;

                                                parking_locations spot = doc.toObject(parking_locations.class);
                                                spots.add(spot);
                                            }
                                            callback.onSuccess(spots);
                                        })
                                        .addOnFailureListener(callback::onFailure);

                            })
                            .addOnFailureListener(callback::onFailure);

                })
                .addOnFailureListener(callback::onFailure);
    }



    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public GeoPoint getGeopoint() { return geopoint; }
    public void setGeopoint(GeoPoint geopoint) { this.geopoint = geopoint; }

    public boolean isFree() { return free; }
    public void setFree(boolean free) { this.free = free; }
}
