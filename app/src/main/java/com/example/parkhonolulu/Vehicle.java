package com.example.parkhonolulu;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Vehicle {

    private String vehicleNum;
    private String carType;
    private String id;  // Firestore document ID

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "vehicles";

    public Vehicle() {
        // Needed for Firestore deserialization
    }

    public Vehicle(String vehicleNum, String carType) {
        this.vehicleNum = vehicleNum;
        this.carType = carType;
    }

    public Vehicle(String id, String vehicleNum, String carType) {
        this.id = id;
        this.vehicleNum = vehicleNum;
        this.carType = carType;
    }

    // Add getter and setter for 'id'
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVehicleNum() {
        return vehicleNum;
    }

    public void setVehicleNum(String vehicleNum) {
        this.vehicleNum = vehicleNum;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    // Save vehicle to Firestore with custom ID (if id is set)
    public void saveToDatabase(OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("vehicleNum", vehicleNum);
        vehicleData.put("carType", carType);

        if (id != null && !id.isEmpty()) {
            // Save using existing document ID
            db.collection(COLLECTION_NAME).document(id)
                    .set(vehicleData)
                    .addOnSuccessListener(onSuccess)
                    .addOnFailureListener(onFailure);
        } else {
            // No id, generate a new document with auto ID
            db.collection(COLLECTION_NAME).add(vehicleData)
                    .addOnSuccessListener(documentReference -> {
                        this.id = documentReference.getId();
                        // Optionally update the vehicleNum inside the document if needed
                        documentReference.update("vehicleNum", vehicleNum)
                                .addOnSuccessListener(aVoid -> onSuccess.onSuccess(aVoid))
                                .addOnFailureListener(onFailure);
                    })
                    .addOnFailureListener(onFailure);
        }
    }

    // Update vehicleNum field by document id
    public void updateVehicleNum(String newVehicleNum, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (id == null || id.isEmpty()) {
            if (onFailure != null) {
                onFailure.onFailure(new Exception("Vehicle ID is null or empty"));
            }
            return;
        }
        db.collection(COLLECTION_NAME)
                .document(id)
                .update("vehicleNum", newVehicleNum)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Update carType field by document id
    public void updateCarType(String newCarType, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (id == null || id.isEmpty()) {
            if (onFailure != null) {
                onFailure.onFailure(new Exception("Vehicle ID is null or empty"));
            }
            return;
        }
        db.collection(COLLECTION_NAME)
                .document(id)
                .update("carType", newCarType)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Delete vehicle from Firestore by document id
    public void deleteFromDatabase(OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (id == null || id.isEmpty()) {
            if (onFailure != null) {
                onFailure.onFailure(new Exception("Vehicle ID is null or empty"));
            }
            return;
        }
        db.collection(COLLECTION_NAME)
                .document(id)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Fetch vehicle by document id (id)
    public static void fetchVehicle(String id, OnSuccessListener<Vehicle> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Vehicle vehicle = documentSnapshot.toObject(Vehicle.class);
                        if (vehicle != null) {
                            vehicle.setId(documentSnapshot.getId()); // make sure id is set
                            onSuccess.onSuccess(vehicle);
                        } else {
                            onFailure.onFailure(new Exception("Vehicle data invalid"));
                        }
                    } else {
                        onFailure.onFailure(new Exception("Vehicle not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public void saveToDatabaseWithAutoId(OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("vehicleNum", vehicleNum);  // user input license plate
        vehicleData.put("carType", carType);

        db.collection(COLLECTION_NAME).add(vehicleData)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();  // Firestore generated doc ID
                    this.id = id; // optional: keep track of this ID in your instance
                    onSuccess.onSuccess(id);
                })
                .addOnFailureListener(onFailure);
    }

}
