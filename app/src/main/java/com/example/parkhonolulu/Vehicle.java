package com.example.parkhonolulu;

public class Vehicle {
    private String vehicleNum;
    private String carType;

    public Vehicle() {
        // Required for Firebase
    }

    public Vehicle(String vehicleNum, String carType) {
        this.vehicleNum = vehicleNum;
        this.carType = carType;
    }

    public String getVehicleNum() {
        return vehicleNum;
    }

    public String getCarType() {
        return carType;
    }
}
