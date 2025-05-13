package com.example.parkhonolulu.model; // or your actual package

public class User {
    private String name;
    private String email;
    private String surname;
    private String username;
    private String password;

    public User() {
        // Empty constructor required for Firebase
    }

    public User(String name, String email, String surname, String username, String password) {
        this.name = name;
        this.email = email;
        this.surname = surname;
        this.username = username;
        this.password = password;
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
