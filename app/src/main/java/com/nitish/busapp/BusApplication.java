package com.nitish.busapp;

import android.Manifest;
import android.app.Application;

import com.google.android.gms.maps.model.LatLng;

public class BusApplication extends Application {
    public static int ROUTE_MINOR_DETAILS = 1;
    public static int ROUTE_PATH_DETAILS = 2;
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    public static final float DEFAULT_ZOOM = 15f;
    public static final int LOCATION_UPDATE_INTERVAL = 5000;     // in milliseconds

    private LatLng startingLocation = null;
    private LatLng finalLocation = null;
    private String startingLocationName = null;
    private String finalLocationName = null;

    public LatLng getStartingLocation() {
        return startingLocation;
    }

    public LatLng getFinalLocation() {
        return finalLocation;
    }

    public String getStartingLocationName() {
        return startingLocationName;
    }

    public String getFinalLocationName() {
        return finalLocationName;
    }

    public void setStartingLocation(LatLng startingLocation) {
        this.startingLocation = startingLocation;
    }

    public void setFinalLocation(LatLng finalLocation) {
        this.finalLocation = finalLocation;
    }

    public void setStartingLocationName(String startingLocationName) {
        this.startingLocationName = startingLocationName;
    }

    public void setFinalLocationName(String finalLocationName) {
        this.finalLocationName = finalLocationName;
    }
}