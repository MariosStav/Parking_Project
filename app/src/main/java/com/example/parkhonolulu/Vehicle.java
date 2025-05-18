package com.example.parkhonolulu;

public class Vehicle {
    private String vehicleNum;
    private String carType;

    public Vehicle() {
        // Firestore needs a public no-arg constructor
    }

    public Vehicle(String vehicleNum, String carType) {
        this.vehicleNum = vehicleNum;
        this.carType = carType;
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
}

