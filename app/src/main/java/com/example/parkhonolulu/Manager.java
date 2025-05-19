package com.example.parkhonolulu;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Manager {

    private String name;
    private String surname;
    private String email;
    private String password;
    private String phone;
    private String role;

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "Manager";

    // Empty constructor for Firestore
    public Manager() {
    }

    // Constructor
    public Manager(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Full constructor
    public Manager(String name, String surname, String email, String password, String phone) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = "Manager";
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public static void login(String email, String password,
                             OnSuccessListener<Void> onSuccess,
                             OnFailureListener onFailure) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public static void loginAsManager(String email, String password,
                                      OnSuccessListener<Void> onSuccess,
                                      OnFailureListener onFailure) {
        login(email, password,
                unused -> {
                    fetchRole(email,
                            role -> {
                                if ("Manager".equalsIgnoreCase(role)) {
                                    onSuccess.onSuccess(null);
                                } else {
                                    onFailure.onFailure(new Exception("User is not a Manager"));
                                }
                            },
                            onFailure);
                },
                onFailure);
    }

    // Create Firebase Auth user and save Manager data to Firestore
    public void registerNewManager(OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String id = email.trim().toLowerCase();

        // Create user in Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Prepare manager data to save to Firestore
                    Map<String, Object> managerData = new HashMap<>();
                    managerData.put("Email", email);
                    managerData.put("Phone", phone);
                    managerData.put("Name", name);
                    managerData.put("Surname", surname);
                    managerData.put("role", "Manager");

                    // Save to Firestore with lowercase email as document ID
                    db.collection(COLLECTION_NAME).document(id)
                            .set(managerData)
                            .addOnSuccessListener(unused -> {
                                Log.d("Manager", "Manager data saved to Firestore.");
                                onSuccess.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Manager", "Error saving manager data: " + e.getMessage());
                                onFailure.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Manager", "Error creating auth user: " + e.getMessage());
                    onFailure.onFailure(e);
                });
    }

    // Save Manager to Firestore with email as document ID
    public void saveToDatabase(OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> managerData = new HashMap<>();
        managerData.put("Name", name);
        managerData.put("Surname", surname);
        managerData.put("Email", email);
        managerData.put("Password", password);
        managerData.put("Phone", phone);
        managerData.put("role", role);

        String id = email.trim().toLowerCase();
        db.collection(COLLECTION_NAME).document(id)
                .set(managerData)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Generic private fetch helper
    private static void fetchField(String email, String fieldName, OnSuccessListener<Object> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("Email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0); // πάρε το πρώτο που ταιριάζει
                        Object value = document.get(fieldName);
                        onSuccess.onSuccess(value);
                    } else {
                        onFailure.onFailure(new Exception("Document not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // Async fetchers for each field by email
    public static void fetchName(String email, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        fetchField(email, "Name",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    public static void fetchSurname(String email, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        fetchField(email, "Surname",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    public static void fetchPhone(String email, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        fetchField(email, "Phone",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    public static void fetchRole(String email, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        fetchField(email, "role",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    public static void fetchPassword(String email, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        fetchField(email, "Password",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    public static void fetchEmail(String email, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        fetchField(email, "Email",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

}
