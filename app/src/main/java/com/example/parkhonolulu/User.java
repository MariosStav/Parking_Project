package com.example.parkhonolulu;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    // Save the user using current Firebase UID
    public void saveToDatabase(OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("surname", surname);
        userData.put("username", username);
        userData.put("password", password);
        userData.put("vehicleid", vehicleid);

        db.collection(COLLECTION_NAME).document(uid)
                .set(userData)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public static void loginWithUsername(String username, String password,
                                         OnSuccessListener<FirebaseUser> onSuccess,
                                         OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String email = document.getString("email");

                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(authResult -> {
                                        FirebaseUser user = authResult.getUser();
                                        if (user != null) {
                                            onSuccess.onSuccess(user);
                                        } else {
                                            onFailure.onFailure(new Exception("Login failed"));
                                        }
                                    })
                                    .addOnFailureListener(onFailure);
                            break;
                        }
                    } else {
                        onFailure.onFailure(new Exception("Username not found"));
                    }
                })
                .addOnFailureListener(onFailure);
    }
}
