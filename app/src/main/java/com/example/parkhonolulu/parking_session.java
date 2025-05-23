package com.example.parkhonolulu;

import android.util.Log;
import android.widget.Toast;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class parking_session {

    private String id;
    private DocumentReference user_id;
    private DocumentReference vehicle_id;
    private DocumentReference parking_location_id;
    private Timestamp start_time;
    private Timestamp end_time;
    private double fee_charged;
    private double security_deposit;

    // Required empty constructor for Firestore
    public parking_session() {
    }

    public parking_session(String id, DocumentReference user_id, DocumentReference vehicle_id,
                           DocumentReference parking_location_id, Timestamp start_time,
                           Timestamp end_time, double fee_charged, double security_deposit) {
        this.id = id;
        this.user_id = user_id;
        this.vehicle_id = vehicle_id;
        this.parking_location_id = parking_location_id;
        this.start_time = start_time;
        this.end_time = end_time;
        this.fee_charged = fee_charged;
        this.security_deposit = security_deposit;
    }

    public parking_session(DocumentReference user_id, DocumentReference vehicle_id,
                           DocumentReference parking_location_id, Timestamp start_time) {
        this.user_id = user_id;
        this.vehicle_id = vehicle_id;
        this.parking_location_id = parking_location_id;
        this.start_time = start_time;
        this.end_time = null;
        this.fee_charged = 0.0;
        this.security_deposit = 0.0;
    }


    public void saveSession(OnSessionSavedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking_sessions")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    if (parking_location_id != null) {
                        parking_location_id.update("free", false)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("parking_session", "Parking spot marked as occupied.");
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w("parking_session", "Failed to update parking location", e);
                                    listener.onFailure(e);
                                });
                    } else {
                        listener.onFailure(new Exception("Parking location reference is null"));
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    public interface OnSessionSavedListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static void checkActiveSession(DocumentReference userRef,
                                          OnActiveSessionChecked callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking_sessions")
                .whereEqualTo("user_id", userRef)
                .whereEqualTo("end_time", null)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        parking_session existing = doc.toObject(parking_session.class);

                        if (existing != null) {
                            existing.setId(doc.getId());
                            callback.onActiveSessionFound(existing);
                        } else {
                            callback.onError(new Exception("Session document could not be parsed"));
                        }
                    } else {
                        callback.onNoActiveSession();
                    }
                })
                .addOnFailureListener(callback::onError);
    }


    public interface OnActiveSessionChecked {
        void onActiveSessionFound(parking_session session);
        void onNoActiveSession();
        void onError(Exception e);
    }

    public void endSession(OnSessionEndedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp now = Timestamp.now();
        this.end_time = now;

        long durationMillis = now.toDate().getTime() - start_time.toDate().getTime();
        long hours = Math.max(1, (long) Math.ceil(durationMillis / (1000.0 * 60 * 60)));

        parking_locations.loadTypeById(parking_location_id.getId(), type -> {
            final double fee;

            if ("electric".equalsIgnoreCase(type)) {
                if (hours == 1) fee = 7;
                else if (hours <= 5) fee = 14;
                else fee = 14 + (hours - 5) * 3;
            } else if ("gas".equalsIgnoreCase(type)) {
                if (hours == 1) fee = 5;
                else if (hours <= 5) fee = 10;
                else fee = 10 + (hours - 5) * 2;
            } else {
                listener.onFailure(new Exception("Unknown parking type"));
                return;
            }

            this.fee_charged = fee;

            final double deposit = this.security_deposit;
            final double difference = fee - deposit;

            if (difference <= 0) {
                // Refund the difference
                double refundAmount = -difference;
                Balance.addToCurrentUserBalance(refundAmount, () -> {
                    completeSessionUpdate(fee, listener);
                }, e -> {
                    listener.onFailure(e);
                });
            } else {
                // Fee is more than deposit, try deducting the extra
                Balance.deductFromCurrentUserBalance(difference, () -> {
                    completeSessionUpdate(fee, listener);
                }, e -> {
                    listener.onFailure(e);
                }, () -> {
                    // Not enough balance
                    listener.onFailure(new Exception("Not enough money in balance. Please add funds."));
                });
            }
        });
    }

    // Helper method to complete Firestore updates
    private void completeSessionUpdate(double fee, OnSessionEndedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference sessionRef = db.collection("parking_sessions").document(this.id);

        sessionRef.update("end_time", end_time, "fee_charged", fee)
                .addOnSuccessListener(aVoid -> {
                    parking_location_id.update("free", true)
                            .addOnSuccessListener(aVoid2 -> listener.onSuccess(fee))
                            .addOnFailureListener(listener::onFailure);
                })
                .addOnFailureListener(listener::onFailure);
    }

    public interface OnSessionEndedListener {
        void onSuccess(double fee);
        void onFailure(Exception e);
    }

    public static void fetchCurrentUserActiveSession(OnActiveSessionChecked callback) {
        DocumentReference userRef = User.getUserRef();
        if (userRef == null) {
            callback.onError(new Exception("User not logged in"));
            return;
        }

        checkActiveSession(userRef, callback);
    }

    public void fetchParkingLocationLatLng(OnLocationFetchedListener listener) {
        if (parking_location_id == null) {
            listener.onFailure(new Exception("Parking location reference is null"));
            return;
        }

        parking_location_id.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        GeoPoint geoPoint = doc.getGeoPoint("geopoint");
                        if (geoPoint != null) {
                            listener.onSuccess(geoPoint.getLatitude(), geoPoint.getLongitude());
                        } else {
                            listener.onFailure(new Exception("GeoPoint is missing in parking location"));
                        }
                    } else {
                        listener.onFailure(new Exception("Parking location document does not exist"));
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    public static void fetchActiveParkingLocation(OnParkingLocationFetched callback) {
        fetchCurrentUserActiveSession(new OnActiveSessionChecked() {
            @Override
            public void onActiveSessionFound(parking_session session) {
                // fetch parking location lat/lng from the session
                session.fetchParkingLocationLatLng(new OnLocationFetchedListener() {
                    @Override
                    public void onSuccess(double latitude, double longitude) {
                        LatLng location = new LatLng(latitude, longitude);
                        callback.onLocationFetched(location);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onError(e);
                    }
                });
            }

            @Override
            public void onNoActiveSession() {
                callback.onNoActiveSession();
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


    public interface OnLocationFetchedListener {
        void onSuccess(double latitude, double longitude);
        void onFailure(Exception e);
    }


    public interface OnParkingLocationFetched {
        void onLocationFetched(LatLng location);
        void onNoActiveSession();
        void onError(Exception e);
    }

    public void calculateCurrentFee(FeeCalculationCallback callback) {
        if (start_time == null || parking_location_id == null) {
            callback.onFailure(new Exception("Start time or parking location missing"));
            return;
        }

        // Calculate hours elapsed (at least 1)
        long durationMillis = System.currentTimeMillis() - start_time.toDate().getTime();
        long hours = Math.max(1, (long) Math.ceil(durationMillis / (1000.0 * 60 * 60)));

        // Fetch parking type asynchronously
        parking_locations.loadTypeById(parking_location_id.getId(), type -> {
            double fee;

            if ("electric".equalsIgnoreCase(type)) {
                if (hours == 1) fee = 7;
                else if (hours <= 5) fee = 14;
                else fee = 14 + (hours - 5) * 3;
            } else if ("gas".equalsIgnoreCase(type)) {
                if (hours == 1) fee = 5;
                else if (hours <= 5) fee = 10;
                else fee = 10 + (hours - 5) * 2;
            } else {
                callback.onFailure(new Exception("Unknown parking type"));
                return;
            }

            callback.onFeeCalculated(fee, hours);
        });
    }

    public interface FeeCalculationCallback {
        void onFeeCalculated(double fee, long hours);
        void onFailure(Exception e);
    }

    public interface OnTotalRevenueFetched {
        void onSuccess(double totalRevenue);
        void onFailure(Exception e);
    }

    public interface OnAverageDurationFetched {
        void onSuccess(String formattedDuration);
        void onFailure(Exception e);
    }

    public static void fetchAverageDurationForCurrentUser(OnAverageDurationFetched callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        String uid = firebaseUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("users").document(uid);

        db.collection("parking_sessions")
                .whereEqualTo("user_id", userRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    calculateAndCallbackDuration(queryDocumentSnapshots.getDocuments(), callback);
                })
                .addOnFailureListener(callback::onFailure);
    }



    // Average duration for all users (all parking sessions, filter after fetch)
    public static void fetchAverageDurationForAllUsers(OnAverageDurationFetched callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("parking_sessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // filter inside the helper method
                    calculateAndCallbackDuration(queryDocumentSnapshots.getDocuments(), callback);
                })
                .addOnFailureListener(callback::onFailure);
    }

    // Helper function remains the same, but filters out null end_time sessions
    private static void calculateAndCallbackDuration(List<DocumentSnapshot> docs, OnAverageDurationFetched callback) {
        long totalDurationMillis = 0;
        int count = 0;

        for (DocumentSnapshot doc : docs) {
            Timestamp start = doc.getTimestamp("start_time");
            Timestamp end = doc.getTimestamp("end_time");

            // Skip incomplete sessions
            if (start != null && end != null) {
                long duration = end.toDate().getTime() - start.toDate().getTime();
                if (duration > 0) {
                    totalDurationMillis += duration;
                    count++;
                }
            }
        }

        if (count == 0) {
            callback.onSuccess("0 min");
            return;
        }

        long avgMillis = totalDurationMillis / count;
        int avgMinutes = (int) Math.round(avgMillis / 60000.0);

        int hours = avgMinutes / 60;
        int minutes = avgMinutes % 60;

        String formatted;
        if (hours > 0) {
            formatted = String.format("%dh %02dmin", hours, minutes);
        } else {
            formatted = String.format("%d min", minutes);
        }

        callback.onSuccess(formatted);
    }

    public static void fetchTotalSessionsForCurrentUser(OnTotalSessionsFetched callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String uid = firebaseUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("users").document(uid);

        db.collection("parking_sessions")
                .whereEqualTo("user_id", userRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalSessions = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // Filter out sessions where end_time is null (ongoing sessions)
                        Timestamp endTime = doc.getTimestamp("end_time");
                        if (endTime != null) {
                            totalSessions++;
                        }
                    }

                    callback.onSuccess(totalSessions);
                })
                .addOnFailureListener(callback::onFailure);
    }


    public interface OnTotalSessionsFetched {
        void onSuccess(int totalSessions);
        void onFailure(Exception e);
    }

    public static void fetchTotalSpendForCurrentUser(OnTotalSpendFetched callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String uid = firebaseUser.getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking_sessions")
                .whereEqualTo("user_id", userRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalSpend = 0.0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp endTime = doc.getTimestamp("end_time");
                        if (endTime != null) { // exclude sessions where end_time is null
                            Double fee = doc.getDouble("fee_charged");
                            if (fee != null) {
                                totalSpend += fee;
                            }
                        }
                    }

                    callback.onSuccess(totalSpend);
                })
                .addOnFailureListener(callback::onFailure);
    }


    public interface OnTotalSpendFetched {
        void onSuccess(double totalSpend);
        void onFailure(Exception e);
    }

    public static void fetchLastSessionForCurrentUser(OnLastSessionFetched callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String uid = firebaseUser.getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking_sessions")
                .whereEqualTo("user_id", userRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Timestamp latestEndTime = null;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp endTime = doc.getTimestamp("end_time");
                        if (endTime != null) {
                            if (latestEndTime == null || endTime.compareTo(latestEndTime) > 0) {
                                latestEndTime = endTime;
                            }
                        }
                    }

                    if (latestEndTime != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Athens"));
                        String formattedDate = sdf.format(latestEndTime.toDate());
                        callback.onSuccess(formattedDate);
                    } else {
                        callback.onFailure(new Exception("No sessions with end_time found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface OnLastSessionFetched {
        void onSuccess(String lastSessionDateTime);
        void onFailure(Exception e);
    }

    public static void fetchFirstSessionForCurrentUser(OnFirstSessionFetched callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String uid = firebaseUser.getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking_sessions")
                .whereEqualTo("user_id", userRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Timestamp earliestEndTime = null;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp endTime = doc.getTimestamp("end_time");
                        if (endTime != null) {
                            if (earliestEndTime == null || endTime.compareTo(earliestEndTime) < 0) {
                                earliestEndTime = endTime;
                            }
                        }
                    }

                    if (earliestEndTime != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Europe/Athens"));
                        String formattedDate = sdf.format(earliestEndTime.toDate());

                        callback.onSuccess(formattedDate);
                    } else {
                        callback.onFailure(new Exception("No sessions with end_time found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface OnFirstSessionFetched {
        void onSuccess(String firstSessionDateTime);
        void onFailure(Exception e);
    }


    public static void fetchTotalRevenue(OnTotalRevenueFetched callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking_sessions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRevenue = 0.0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp endTime = doc.getTimestamp("end_time");
                        if (endTime != null) {
                            Double fee = doc.getDouble("fee_charged");
                            if (fee != null) {
                                totalRevenue += fee;
                            }
                        }
                    }

                    callback.onSuccess(totalRevenue);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void fetchSessionCountsPerDateInMonth(String userId, int year, int month,
                                                        OnSuccessListener<Map<String, Integer>> onSuccess,
                                                        OnFailureListener onFailure) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month, 1, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        long startMillis = startCal.getTimeInMillis();

        Calendar endCal = Calendar.getInstance();
        endCal.set(year, month, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        long endMillis = endCal.getTimeInMillis();

        db.collection("parking_sessions")
                .whereGreaterThanOrEqualTo("start_time", new Timestamp(new Date(startMillis)))
                .whereLessThanOrEqualTo("start_time", new Timestamp(new Date(endMillis)))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> dateCounts = new TreeMap<>();
                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                    dayFormat.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        DocumentReference userRef = doc.getDocumentReference("user_id");
                        if (userRef != null && userRef.getId().equals(userId)) {
                            // Check if end_time is NOT null (skip if null)
                            Timestamp endTime = doc.getTimestamp("end_time");
                            if (endTime != null) {
                                Timestamp startTime = doc.getTimestamp("start_time");
                                if (startTime != null) {
                                    String day = dayFormat.format(startTime.toDate());
                                    Integer count = dateCounts.get(day);
                                    if (count == null) count = 0;
                                    dateCounts.put(day, count + 1);
                                }
                            }
                        }
                    }
                    onSuccess.onSuccess(dateCounts);
                })
                .addOnFailureListener(onFailure);
    }

    public static void fetchAverageDurationsPerDateInMonth(String userId, int year, int month,
                                                           OnSuccessListener<Map<String, Float>> onSuccess, OnFailureListener onFailure) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month, 1, 0, 0, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        long startMillis = startCal.getTimeInMillis();

        Calendar endCal = Calendar.getInstance();
        endCal.set(year, month, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        long endMillis = endCal.getTimeInMillis();

        db.collection("parking_sessions")
                .whereGreaterThanOrEqualTo("start_time", new Timestamp(new Date(startMillis)))
                .whereLessThanOrEqualTo("start_time", new Timestamp(new Date(endMillis)))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, List<Long>> durationsByDay = new TreeMap<>();
                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                    dayFormat.setTimeZone(TimeZone.getTimeZone("Europe/Athens"));

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        DocumentReference userRef = doc.getDocumentReference("user_id");
                        if (userRef != null && userRef.getId().equals(userId)) {
                            Timestamp start = doc.getTimestamp("start_time");
                            Timestamp end = doc.getTimestamp("end_time");
                            if (start != null && end != null) {
                                long durationMillis = end.toDate().getTime() - start.toDate().getTime();
                                String day = dayFormat.format(start.toDate());
                                if (!durationsByDay.containsKey(day)) {
                                    durationsByDay.put(day, new ArrayList<>());
                                }
                                durationsByDay.get(day).add(durationMillis);
                            }
                        }
                    }

                    // Calculate averages
                    Map<String, Float> avgDurations = new TreeMap<>();
                    for (Map.Entry<String, List<Long>> entry : durationsByDay.entrySet()) {
                        List<Long> durations = entry.getValue();
                        long total = 0;
                        for (Long d : durations) total += d;
                        float avgMinutes = (total / durations.size()) / 60000f;
                        avgDurations.put(entry.getKey(), avgMinutes);
                    }

                    onSuccess.onSuccess(avgDurations);
                })
                .addOnFailureListener(onFailure);
    }

    public interface OnLocationNamesFetched {
        void onComplete(Map<String, String> locationIdToName);
    }


    public interface OnErrorListener {
        void onError(Exception e);
    }

    public static void fetchLocationUsageStats(String userId, OnLocationUsageFetched callback, OnErrorListener errorCallback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId);

        db.collection("parking_sessions")
                .whereEqualTo("user_id", userRef)   // compare with DocumentReference, NOT String
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> locationCount = new HashMap<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        if (doc.contains("parking_location_id") && doc.contains("end_time")) {
                            Object locRefObj = doc.get("parking_location_id");
                            Object endTimeObj = doc.get("end_time");

                            if (locRefObj != null && endTimeObj != null) {
                                // parking_location_id is a DocumentReference
                                com.google.firebase.firestore.DocumentReference locRef = (com.google.firebase.firestore.DocumentReference) locRefObj;
                                String locationId = locRef.getId();

                                Integer count = locationCount.get(locationId);
                                if (count == null) {
                                    locationCount.put(locationId, 1);
                                } else {
                                    locationCount.put(locationId, count + 1);
                                }
                            }
                        }
                    }

                    callback.onComplete(locationCount);
                })
                .addOnFailureListener(errorCallback::onError);
    }

    public interface OnLocationUsageFetched {
        void onComplete(Map<String, Integer> locationUsageMap);
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentReference getUser_id() {
        return user_id;
    }

    public void setUser_id(DocumentReference user_id) {
        this.user_id = user_id;
    }

    public DocumentReference getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(DocumentReference vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public DocumentReference getParking_location_id() {
        return parking_location_id;
    }

    public void setParking_location_id(DocumentReference parking_location_id) {
        this.parking_location_id = parking_location_id;
    }

    public Timestamp getStart_time() {
        return start_time;
    }

    public void setStart_time(Timestamp start_time) {
        this.start_time = start_time;
    }

    public Timestamp getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Timestamp end_time) {
        this.end_time = end_time;
    }

    public double getFee_charged() {
        return fee_charged;
    }

    public void setFee_charged(double fee_charged) {
        this.fee_charged = fee_charged;
    }

    public double getSecurity_deposit() { return security_deposit; }

    public void setSecurity_deposit(double security_deposit) { this.security_deposit = security_deposit; }
}
