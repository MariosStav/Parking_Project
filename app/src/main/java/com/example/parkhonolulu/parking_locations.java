package com.example.parkhonolulu;

import com.google.firebase.firestore.GeoPoint;

public class parking_locations {

    private String name;
    private String type;
    private GeoPoint geopoint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GeoPoint getGeopoint() {
        return geopoint;
    }

    public void setGeopoint(GeoPoint geopoint) {
        this.geopoint = geopoint;
    }
}
