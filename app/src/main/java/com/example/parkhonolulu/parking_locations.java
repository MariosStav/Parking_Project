package com.example.parkhonolulu;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.Exclude;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class parking_locations implements Parcelable {

    private String name;
    private String type;
    private GeoPoint geopoint;
    private boolean free, deleted;
    @Exclude
    private String documentId;


    // Firestore instance & collection name
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "parking_locations";

    // Constructors
    public parking_locations() {}

    public parking_locations(String name, String type, GeoPoint geopoint, boolean free) {
        this.name = name;
        this.type = type;
        this.geopoint = geopoint;
        this.free = free;
        this.deleted = false;
    }

    // Parcelable constructor
    protected parking_locations(Parcel in) {
        name = in.readString();
        type = in.readString();
        free = in.readByte() != 0;

        double lat = in.readDouble();
        double lng = in.readDouble();
        geopoint = new GeoPoint(lat, lng);
        documentId = in.readString();
    }

    public static final Creator<parking_locations> CREATOR = new Creator<parking_locations>() {
        @Override
        public parking_locations createFromParcel(Parcel in) {
            return new parking_locations(in);
        }

        @Override
        public parking_locations[] newArray(int size) {
            return new parking_locations[size];
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeByte((byte) (free ? 1 : 0));
        parcel.writeDouble(geopoint != null ? geopoint.getLatitude() : 0.0);
        parcel.writeDouble(geopoint != null ? geopoint.getLongitude() : 0.0);
        parcel.writeString(documentId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Callback interface
    public interface LoadSpotsCallback {
        void onSuccess(List<parking_locations> spots);
        void onFailure(Exception e);
    }

    // Static method to load spots based on user and car type
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

                                db.collection(COLLECTION_NAME)
                                        .whereIn("type", typesToQuery)
                                        .get()
                                        .addOnSuccessListener(querySnapshot -> {
                                            List<parking_locations> spots = new ArrayList<>();
                                            for (QueryDocumentSnapshot doc : querySnapshot) {
                                                Boolean freeValue = doc.getBoolean("free");
                                                boolean isFree = freeValue != null && freeValue;

                                                if (!includeUnavailable && !isFree) continue;

                                                parking_locations spot = doc.toObject(parking_locations.class);
                                                spot.setId(doc.getId());
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

    public static void loadAllSpots(boolean includeUnavailable, LoadSpotsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query = db.collection("parking_locations");

        if (!includeUnavailable) {
            query = query.whereEqualTo("free", true);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<parking_locations> spots = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                parking_locations spot = doc.toObject(parking_locations.class);
                if (spot != null) {
                    spot.setId(doc.getId());
                    spots.add(spot);
                }
            }
            callback.onSuccess(spots);
        }).addOnFailureListener(callback::onFailure);
    }

    public void saveToDatabase(OnSaveCallback callback) {
        db.collection(COLLECTION_NAME)
                .add(this)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    public interface OnSaveCallback {
        void onSuccess(String documentId);
        void onFailure(Exception e);
    }

    public void deleteFromDatabase(OnDeleteCallback callback) {
        if (documentId == null || documentId.isEmpty()) {
            callback.onFailure(new Exception("Document ID is null or empty"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(documentId)
                .update("deleted", true)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public interface OnDeleteCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static void loadTypeById(String documentId, TypeCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking_locations").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String type = documentSnapshot.getString("type");
                        callback.onTypeLoaded(type);
                    } else {
                        callback.onTypeLoaded(null);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onTypeLoaded(null);
                });
    }

    public interface TypeCallback {
        void onTypeLoaded(String type);
    }

    @Exclude
    public DocumentReference getLocationRef() {
        String id = getId();
        if (id == null || id.isEmpty()) return null;

        return FirebaseFirestore.getInstance()
                .collection("parking_locations")
                .document(id);
    }

    // Getters and setters
    public String getId() { return documentId; }
    public void setId(String documentId) { this.documentId = documentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public GeoPoint getGeopoint() { return geopoint; }
    public void setGeopoint(GeoPoint geopoint) { this.geopoint = geopoint; }

    public boolean isFree() { return free; }
    public void setFree(boolean free) { this.free = free; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
