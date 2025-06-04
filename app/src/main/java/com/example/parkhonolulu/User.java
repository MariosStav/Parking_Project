package com.example.parkhonolulu;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String name;
    private String email;
    private String surname;
    private String username;
    private String password;
    private String vehicleid;

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_NAME = "users";

    public User() {
        // Empty constructor required for Firebase
    }

    public User(String name, String email, String surname, String username, String password, String vehicleid) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.surname = surname;
        this.username = username;
        this.vehicleid = vehicleid;
    }

    // Helper to get current UID safely
    private static String getCurrentUid() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    // Generic method to get a specific field from Firestore for current user
    private static void getField(String fieldName, OnSuccessListener<Object> onSuccess, OnFailureListener onFailure) {
        String uid = getCurrentUid();
        if (uid == null) {
            onFailure.onFailure(new Exception("User not logged in"));
            return;
        }

        db.collection(COLLECTION_NAME).document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Object value = documentSnapshot.get(fieldName);
                        onSuccess.onSuccess(value);
                    } else {
                        onFailure.onFailure(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // Async getter for name
    public static void fetchName(OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        getField("name",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    // Async getter for email
    public static void fetchEmail(OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        getField("email",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    // Async getter for surname
    public static void fetchSurname(OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        getField("surname",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    // Async getter for username
    public static void fetchUsername(OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        getField("username",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    // Async getter for password (usually not recommended to retrieve plain password)
    public static void fetchPassword(OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        getField("password",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    // Async getter for vehicleid
    public static void fetchVehicleid(OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        getField("vehicleid",
                value -> onSuccess.onSuccess(value != null ? (String) value : null),
                onFailure);
    }

    public static void loginWithUsername(String username, String password,
                                         OnSuccessListener<FirebaseUser> onSuccess,
                                         OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Find the document with the matching username
        db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String email = document.getString("email");

                        if (email == null || email.isEmpty()) {
                            onFailure.onFailure(new Exception("Email not found for user"));
                            return;
                        }

                        // Authenticate with FirebaseAuth using email and password
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener(authResult -> {
                                    FirebaseUser user = authResult.getUser();
                                    if (user != null) {
                                        onSuccess.onSuccess(user);
                                    } else {
                                        onFailure.onFailure(new Exception("Login failed"));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Relay the actual FirebaseAuth exception message
                                    onFailure.onFailure(new Exception(e.getMessage()));
                                });

                    } else if (task.isSuccessful()) {
                        onFailure.onFailure(new Exception("Username not found"));
                    } else {
                        onFailure.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(onFailure);
    }

    // Fetch entire User object from Firestore
    public static void fetchCurrentUser(OnSuccessListener<User> onSuccess, OnFailureListener onFailure) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null) {
            onFailure.onFailure(new Exception("User not logged in"));
            return;
        }

        db.collection(COLLECTION_NAME).document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        onSuccess.onSuccess(user);
                    } else {
                        onFailure.onFailure(new Exception("User document not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public static void register(String name, String email, String surname, String username, String password,
                                String vehicleNum, String carType,
                                OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Step 1: Create Vehicle with auto-generated ID
                    Vehicle newVehicle = new Vehicle(vehicleNum, carType);
                    newVehicle.saveToDatabaseWithAutoId(vehicleId -> {
                        // Step 2: Create User with vehicleId from newly created vehicle doc
                        User newUser = new User(name, email, surname, username, password, vehicleId);

                        // Step 3: Save User with FirebaseAuth UID
                        newUser.saveToDatabase(aVoid -> {
                            // Step 4: Create initial balance record for user
                            String uid = authResult.getUser().getUid();
                            Balance.createInitialBalance(uid, onSuccess, onFailure);
                        }, onFailure);
                    }, onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    // Save user data to Firestore
    public void saveToDatabase(OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null) {
            onFailure.onFailure(new Exception("User not logged in"));
            return;
        }

        db.collection(COLLECTION_NAME).document(uid)
                .set(this) // save whole user object as map
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static DocumentReference getUserRef() {
        return FirebaseFirestore.getInstance().collection("users").document(getCurrentUserId());
    }

    public static void fetchVehicleRef(OnVehicleRefFetched listener, OnVehicleRefFetchError errorListener) {
        getUserRef().get().addOnSuccessListener(documentSnapshot -> {
            String vehicleId = documentSnapshot.getString("vehicleid");
            if (vehicleId != null) {
                DocumentReference vehicleRef = FirebaseFirestore.getInstance()
                        .collection("vehicles").document(vehicleId);
                listener.onVehicleRefFetched(vehicleRef);
            } else {
                errorListener.onError(new Exception("vehicleid is null"));
            }
        }).addOnFailureListener(errorListener::onError);
    }

    public interface OnVehicleRefFetched {
        void onVehicleRefFetched(DocumentReference vehicleRef);
    }

    public interface OnVehicleRefFetchError {
        void onError(Exception e);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getVehicleid() { return vehicleid; }
    public void setVehicleid(String vehicleid) { this.vehicleid = vehicleid; }
}
