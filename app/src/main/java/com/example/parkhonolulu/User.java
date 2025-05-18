package com.example.parkhonolulu;

public class User {
    private String name;
    private String email;
    private String surname;
    private String username;
    private String password;

    private String vehicleid;

    public String getVehicleid() {
        return vehicleid;
    }

    public void setVehicleid(String vehicleid) {
        this.vehicleid = vehicleid;
    }

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

    // Add getters and setters (important for Firebase)
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
}
