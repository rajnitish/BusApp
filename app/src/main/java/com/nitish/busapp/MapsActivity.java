package com.nitish.busapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        OnConnectionFailedListener {

    private static final int AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE_FINAL_LOCATION = 2;
    private static final int MULTIPLE_ROUTE_ACTIVITY = 3;

    // vars
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Location currentLocation;
    LatLng origin = new LatLng(28.564659, 77.232880);

    // Markers
    Marker startingLocationMarker = null;
    Marker finalLocationMarker = null;
    ArrayList<Marker> busMarkers = null;

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

        // Get widgets
        mStartingLocationTextbox = findViewById(R.id.starting_location_textbox);
        mFinalLocationTextbox = findViewById(R.id.final_location_textbox);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set action bar
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.bus_route:
                        startActivity(new Intent(MapsActivity.this, BusRouteActivity.class));
                        break;
                    case R.id.complaint_section:
                        startActivity(new Intent(MapsActivity.this, ComplaintActivity.class));
                        break;
                    case R.id.about_us:
                        startActivity(new Intent(MapsActivity.this, AboutUsActivity.class));
                        break;
                    case R.id.exit_section:
                        startActivity(new Intent(MapsActivity.this, ExitActivity.class));
                        finish();
                        break;
                }
                return true;
            }
        });

        // Get permissions and initialize map
        getLocationPermission();

        // Initialize Places
        Places.initialize(getApplicationContext(), "AIzaSyCSrpzMTna9S8hI-gmHVwlxvqC8QnPFPZY");

        // Set the fields to specify which types of place data to return
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
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_STARTING_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                // Set global variables
                ((BusApplication) this.getApplication()).setStartingLocationName(place.getName());
                ((BusApplication) this.getApplication()).setStartingLocation(place.getLatLng());

                // Set starting location name
                mStartingLocationTextbox.setText(place.getName());

                // Add starting marker
                if (startingLocationMarker == null) {
                    startingLocationMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
                } else {
                    startingLocationMarker.setPosition(place.getLatLng());
                }

                // Show route
                showRoutes();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // handle error
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
            }
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE_FINAL_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                // Set global variables
                ((BusApplication) this.getApplication()).setFinalLocationName(place.getName());
                ((BusApplication) this.getApplication()).setFinalLocation(place.getLatLng());

                // Set final location name
                mFinalLocationTextbox.setText(place.getName());

                // Add final marker
                if (finalLocationMarker == null) {
                    finalLocationMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
                } else {
                    finalLocationMarker.setPosition(place.getLatLng());
                }

                // Show route
                showRoutes();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // handle error
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
            }
        } else if (requestCode == MULTIPLE_ROUTE_ACTIVITY) {
            // Set text in textboxes
            mStartingLocationTextbox.setText(((BusApplication) this.getApplication()).getStartingLocationName());
            mFinalLocationTextbox.setText(((BusApplication) this.getApplication()).getFinalLocationName());

            // Set markers
            startingLocationMarker.setPosition(((BusApplication) this.getApplication()).getStartingLocation());
            finalLocationMarker.setPosition(((BusApplication) this.getApplication()).getFinalLocation());
        }
    }

    // Gets the required permissions
    private void getLocationPermission() {
        String[] permissions = {BusApplication.FINE_LOCATION, BusApplication.COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this, BusApplication.FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, BusApplication.COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, BusApplication.LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, BusApplication.LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // This is executed once the permissions are granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case BusApplication.LOCATION_PERMISSION_REQUEST_CODE: {
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

    // Initializes the map fragment
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
    }

    // This is executed once the map is initialized and is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            getDeviceLocation();
            setLocationUpdates();



            if(currentLocation != null)
            {
                origin = new LatLng( currentLocation.getLatitude(), currentLocation.getLongitude());

            }
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(origin)
                    .radius(5000)
                    .strokeColor(Color.RED)
                    .fillColor(getColorWithAlpha(Color.CYAN, 0.2f)));





            // Place the location button
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(MapsActivity.this, BusDetailsActivity.class);
                    intent.putExtra("bus_id", marker.getTitle());
                    startActivity(intent);
                }
            });

        }
    }

    // Set starting location marker as current location
    void setStartingLocation() {
        // Set marker
        if (startingLocationMarker == null) {
            startingLocationMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
        }

        // Set location name in textbox
        String currentLocationName = "";
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (listAddresses != null && listAddresses.size() > 0) {
                currentLocationName = listAddresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
        }
        mStartingLocationTextbox.setText(currentLocationName);

        // Set global variables
        ((BusApplication)this.getApplication()).setStartingLocation(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        ((BusApplication)this.getApplication()).setStartingLocationName(currentLocationName);
    }

    // Set nearby bus markers on map
    void setBusMarkers() {
        String query_url = "https://busappgp16.000webhostapp.com/retrieve.php?case=1&lat=" + currentLocation.getLatitude() + "&lng=" + currentLocation.getLongitude();
        new FetchSQLQuery(MapsActivity.this, new FetchSQLQuery.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                // Parse JSON string and show markers (do this is an async task preferably)
                try {
                    JSONArray jsonArray = (new JSONObject(output)).getJSONArray("busdetail");
                    if (busMarkers == null) {
                        busMarkers = new ArrayList<>();
                    }

                    // Get bus marker
                    BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.bus);
                    Bitmap b = bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        double lat = Double.parseDouble((String) ((JSONObject) jsonArray.get(i)).get("Lat"));
                        double lng = Double.parseDouble((String) ((JSONObject) jsonArray.get(i)).get("Lng"));
                        Marker tempMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .title((String) (((JSONObject) jsonArray.get(i)).get("ID")))
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                        busMarkers.add(tempMarker);
                    }
                } catch (JSONException e) {
                }
            }
        }).execute(query_url);
    }

    // Sets location updates to update current location periodically
    void setLocationUpdates() {
        // Set location updates
        final LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        locationRequest.setInterval(BusApplication.LOCATION_UPDATE_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }

        }, null);


    }

    private static int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    // This method marks the initial location and our current location
    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            currentLocation = (Location) task.getResult();

                            // Set current location as starting location
                            setStartingLocation();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 12.0f);



                            // Setup nearby bus markers
                            setBusMarkers();

                        } else {
                            // Task unsuccessful
                            Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
       CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
       mMap.animateCamera(cu);

        hideSoftKeyboard();
    }

    private void showRoutes() {
        if(startingLocationMarker != null && finalLocationMarker != null) {
            startActivityForResult(new Intent(this, MultipleRouteActivity.class), MULTIPLE_ROUTE_ACTIVITY);
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

//    // This method is executed once the places api is finished. It places the route on the map
//    @Override
//    public void onTaskDone(Object... values) {
//        if(currentPolylines != null && currentPolylines.size() > 0) {
//            for(int i = 0; i < currentPolylines.size(); i++) {
//                currentPolylines.get(i).remove();
//            }
//        }
//
//        currentPolylines = new ArrayList<>();
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//
//        ArrayList<PolylineOptions> arr = (ArrayList<PolylineOptions>) values[0];
//        for(int i = 0; i < arr.size(); i++) {
//            Polyline p = mMap.addPolyline(arr.get(i));
//            currentPolylines.add(p);
//            List<LatLng> l = arr.get(i).getPoints();
//            for(int j = 0; j < l.size(); j++) {
//                builder.include(l.get(j));
//            }
//        }
//
//        // zoom out map
//        int padding = 100;
//        LatLngBounds bounds = builder.build();
//        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//        mMap.animateCamera(cu);
//    }
}
