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

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "vehicles";

    public Vehicle() {
        // Needed for Firestore deserialization
    }

    public Vehicle(String vehicleNum, String carType) {
        this.vehicleNum = vehicleNum;
        this.carType = carType;
    }

    // Getters and Setters
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

    // Save vehicle to Firestore
    public void saveToDatabase() {
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("vehicleNum", vehicleNum);
        vehicleData.put("carType", carType);

        db.collection(COLLECTION_NAME).document(vehicleNum)
                .set(vehicleData)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Vehicle saved successfully.");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error saving vehicle: " + e.getMessage());
                });
    }

    // Update existing vehicle in Firestore
    public void updateCarType(String newCarType) {
        db.collection(COLLECTION_NAME).document(vehicleNum)
                .update("carType", newCarType)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Vehicle updated successfully.");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error updating vehicle: " + e.getMessage());
                });
    }

    // Delete vehicle from Firestore
    public void deleteFromDatabase() {
        db.collection(COLLECTION_NAME).document(vehicleNum)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Vehicle deleted successfully.");
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error deleting vehicle: " + e.getMessage());
                });
    }

    // Static method to fetch vehicle by vehicle number
    public static void getVehicleByNumber(String vehicleNum, OnSuccessListener<DocumentSnapshot> successListener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME).document(vehicleNum)
                .get()
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    public void saveToDatabaseWithAutoId(OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("vehicleNum", ""); // Initially blank, to be updated
        vehicleData.put("carType", carType);

        db.collection(COLLECTION_NAME).add(vehicleData)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();

                    // Update vehicleNum to match document ID
                    documentReference.update("vehicleNum", id)
                            .addOnSuccessListener(unused -> onSuccess.onSuccess(id))
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    // --- Async fetchers for individual fields by vehicleNum ---

    private static void fetchField(String vehicleNum, String fieldName, OnSuccessListener<Object> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME).document(vehicleNum)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object value = documentSnapshot.get(fieldName);
                        onSuccess.onSuccess(value);
                    } else {
                        onFailure.onFailure(new Exception("Vehicle not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public static void fetchVehicleNum(String vehicleNum, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        fetchField(vehicleNum, "vehicleNum",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    public static void fetchCarType(String vehicleNum, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        fetchField(vehicleNum, "carType",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }
}
