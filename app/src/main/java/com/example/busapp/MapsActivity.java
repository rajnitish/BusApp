package com.example.busapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.common.api.GoogleApiClient.*;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        OnConnectionFailedListener,
        TaskLoadedCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE_FINAL_LOCATION = 2;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    // vars
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;

    // Markers
    Marker startingLocationMarker = null;
    Marker finalLocationMarker = null;
    private ArrayList<Polyline> currentPolylines;

    // widgets
    DrawerLayout drawer;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    TextView mStartingLocationTextbox;
    TextView mFinalLocationTextbox;
    View mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mStartingLocationTextbox = findViewById(R.id.starting_location_textbox);
        mFinalLocationTextbox = findViewById(R.id.final_location_textbox);
        drawer = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.bus_route :
                        break;
                    case R.id.complaint_section :
                        startActivity(new Intent(MapsActivity.this, ComplaintActivity.class));
                        break;
                }
                return true;
            }
        });

        getLocationPermission();

        // Initialize Places
        Places.initialize(getApplicationContext(), "AIzaSyBAoJACcqTF7AOngp7lc2FcxTKsj75acuo");

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        final List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        mStartingLocationTextbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setLocationRestriction(RectangularBounds.newInstance(
                                new LatLng(28.442, 76.72),
                                new LatLng(28.858, 77.414)
                        ))
                        .build(MapsActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION);
            }
        });

        mFinalLocationTextbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setLocationRestriction(RectangularBounds.newInstance(
                                new LatLng(28.442, 76.72),
                                new LatLng(28.858, 77.414)
                        ))
                        .build(MapsActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_FINAL_LOCATION);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "Activity ended", Toast.LENGTH_SHORT);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                if(startingLocationMarker == null) {
                    startingLocationMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
                } else {
                    startingLocationMarker.setPosition(place.getLatLng());
                }
                mStartingLocationTextbox.setText(place.getName());

                // Calculate route
                if(startingLocationMarker != null && finalLocationMarker != null) {
                    showRoute();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
            }
        } else if(requestCode == AUTOCOMPLETE_REQUEST_CODE_FINAL_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                if(finalLocationMarker == null) {
                    finalLocationMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
                } else {
                    finalLocationMarker.setPosition(place.getLatLng());
                }
                mFinalLocationTextbox.setText(place.getName());

                // Calculate route
                if(startingLocationMarker != null && finalLocationMarker != null) {
                    showRoute();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        String transit_mode = "transit_mode=bus";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + transit_mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    // Gets the required permissions
    private void getLocationPermission() {
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if(mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            if(startingLocationMarker == null) {
                                startingLocationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
                            }
                            // Get location name
                            String currentLocationName = "";
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                List<Address> listAddresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                                if(listAddresses != null && listAddresses.size() > 0) {
                                    currentLocationName = listAddresses.get(0).getAddressLine(0);
                                }
                            } catch(IOException e) {
                            }
                            mStartingLocationTextbox.setText(currentLocationName);
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch(SecurityException e) {
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cu);
        hideSoftKeyboard();
    }

    private void showRoute() {
        startMyTask(new FetchURL(MapsActivity.this));
    }

    private void startMyTask(AsyncTask<String, Void, String> asyncTask) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getUrl(startingLocationMarker.getPosition(), finalLocationMarker.getPosition(), "transit"));
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onTaskDone(Object... values) {
        if(currentPolylines != null && currentPolylines.size() > 0) {
            for(int i = 0; i < currentPolylines.size(); i++) {
                currentPolylines.get(i).remove();
            }
        }

        currentPolylines = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        ArrayList<PolylineOptions> arr = (ArrayList<PolylineOptions>) values[0];
        for(int i = 0; i < arr.size(); i++) {
            Polyline p = mMap.addPolyline(arr.get(i));
            currentPolylines.add(p);
            List<LatLng> l = arr.get(i).getPoints();
            for(int j = 0; j < l.size(); j++) {
                builder.include(l.get(j));
            }
        }

        // zoom out map
        int padding = 100;
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }
}
