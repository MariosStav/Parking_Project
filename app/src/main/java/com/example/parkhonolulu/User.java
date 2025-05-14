package com.example.parkhonolulu;
public class User {
    private String name;
    private String surname;
    private String username;
    private String email;
    private String password;
    private String role;
    private String uid;
    private String vehicle;

    public User() {
        // Required for Firebase
    }

    public User(String name, String surname, String username, String email, String role, String uid, String vehicle) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.role = role;
        this.uid = uid;
        this.vehicle = vehicle;
    }

    // Getters required by Firebase
    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getUid() {
        return uid;
    }

    public String getVehicle() {
        return vehicle;
    }

    // Getter for password is intentionally omitted as it's not set by the constructor used
    // and storing passwords directly in Firestore documents is not recommended.
    // If you have a specific reason to store it and set it, you can add:
    // public String getPassword() { return password; }
}
