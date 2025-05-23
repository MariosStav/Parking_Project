package com.example.parkhonolulu;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class Balance {
    private double amount;
    private String userId;

    public Balance() {}

    public Balance(double amount, String userId) {
        this.amount = amount;
        this.userId = userId;
    }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    // Create initial balance (0) for new user
    public static void createInitialBalance(String userId,
                                            OnSuccessListener<Void> onSuccess,
                                            OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Balance balance = new Balance(0.0, userId);

        db.collection("Balance")
                .document(userId)
                .set(balance)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // Fetch balance for a userId
    public static void fetchBalance(String userId,
                                    OnSuccessListener<Double> onSuccess,
                                    OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Balance")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Balance balance = documentSnapshot.toObject(Balance.class);
                        if (balance != null) {
                            onSuccess.onSuccess(balance.getAmount());
                        } else {
                            onSuccess.onSuccess(0.0);
                        }
                    } else {
                        onSuccess.onSuccess(0.0);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    public static void fetchCurrentUserBalance(OnSuccessListener<Double> onSuccess, OnFailureListener onFailure) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            onFailure.onFailure(new Exception("User not logged in"));
            return;
        }

        fetchBalance(userId, onSuccess, onFailure);
    }

    public static void addToCurrentUserBalance(double amountToAdd, Runnable onSuccess, OnFailureListener onFailure) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            onFailure.onFailure(new Exception("User not logged in"));
            return;
        }

        // Assume you have a Firestore instance and balances are stored in "balances" collection, document userId
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference balanceRef = db.collection("Balance").document(userId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(balanceRef);
                    double currentBalance = 0.0;
                    if (snapshot.exists()) {
                        currentBalance = snapshot.getDouble("amount") != null ? snapshot.getDouble("amount") : 0.0;
                    }
                    double newBalance = currentBalance + amountToAdd;
                    transaction.update(balanceRef, "amount", newBalance);
                    return null;
                }).addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure);
    }

    public static void deductFromCurrentUserBalance(double amountToDeduct,
                                                    Runnable onSuccess,
                                                    OnFailureListener onFailure,
                                                    Runnable onInsufficientFunds) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            if (onFailure != null) {
                onFailure.onFailure(new Exception("User not logged in"));
            }
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference balanceRef = db.collection("Balance").document(userId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(balanceRef);
            double currentBalance = 0.0;
            if (snapshot.exists()) {
                Double storedAmount = snapshot.getDouble("amount");
                if (storedAmount != null) {
                    currentBalance = storedAmount;
                }
            }

            if (currentBalance < amountToDeduct) {
                throw new IllegalStateException("Insufficient funds");
            }

            double newBalance = currentBalance - amountToDeduct;
            transaction.update(balanceRef, "amount", newBalance);
            return null;

        }).addOnSuccessListener(aVoid -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        }).addOnFailureListener(e -> {
            if (e instanceof IllegalStateException && "Insufficient funds".equals(e.getMessage())) {
                if (onInsufficientFunds != null) {
                    onInsufficientFunds.run();
                }
            } else {
                if (onFailure != null) {
                    onFailure.onFailure(e);
                }
            }
        });
    }

}
