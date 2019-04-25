package com.nitish.busapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BusRouteOnMapActivity extends AppCompatActivity implements
        TaskLoadedCallback,
        OnMapReadyCallback {

    private static final String TAG = "extra_msg";
    // vars
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Location currentLocation;

    ArrayList<Marker> busMarkers = null;
    View mapView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_route_on_map_view);
        String json = getIntent().getExtras().getString("bus_route");

        // Get permissions and initialize map
        getLocationPermission();


        new FetchURL(BusRouteOnMapActivity.this, BusApplication.ROUTE_PATH_DETAILS).execute(json);
        populatecontent(json);
    }

    private void populatecontent(String json) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(json);
            String fare = obj.getJSONObject("fare").getString("text");
            JSONObject main_obj = obj.getJSONArray("legs").getJSONObject(0);

            String arrival_time= main_obj.getJSONObject("arrival_time").getString("text");
            String depart_time= main_obj.getJSONObject("departure_time").getString("text");
            String travel_dist= main_obj.getJSONObject("distance").getString("text");
            String travel_duration = main_obj.getJSONObject("duration").getString("text");

            JSONArray jArr = main_obj.getJSONArray("steps");

            TextView eta_textview = findViewById(R.id.eta_text_detail);
            TextView duration_textview = findViewById(R.id.duration_text_detail);
            TextView fare_textview = findViewById(R.id.fare_text_detail);
            TextView distance_textview = findViewById(R.id.distance_text_detail);

            eta_textview.setText(depart_time + "-" + arrival_time);
            duration_textview.setText(travel_duration);
            fare_textview.setText(fare);
            distance_textview.setText(travel_dist);

            String[] directionsarr = new String[jArr.length()];
            String[] extra_arr = new String[jArr.length()];
            for(int i = 0; i < jArr.length(); i++)
            {
                JSONObject jsonobject = jArr.getJSONObject(i);
                directionsarr[i] = jsonobject.getString("html_instructions");
                extra_arr[i] = jsonobject.getString("travel_mode");
                if(extra_arr[i].equals("TRANSIT"))
                {
                    JSONObject currobj = jsonobject.getJSONObject("transit_details");
                    String busnum = "";
                    try
                    {
                    busnum=currobj.getJSONObject("line").getString("short_name");
                    }
                    catch (Exception e) {
                        Toast.makeText(BusRouteOnMapActivity.this,e.toString(),Toast.LENGTH_LONG);
                        Log.e(TAG, "Foo did not work", e);
                        e.printStackTrace();                        
                    }

                    extra_arr[i] = "Travel "+currobj.getString("num_stops")+" stops by Bus "+
                            busnum;
                    directionsarr[i] = extra_arr[i]+"\n"+directionsarr[i];
                }
            }
            final ListView listView = findViewById(R.id.direction_view_detail);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,directionsarr );
            listView.setAdapter(adapter);


        } catch (Exception e) {
            Toast.makeText(BusRouteOnMapActivity.this,e.toString(),Toast.LENGTH_LONG);
            Log.e(TAG, "Foo did not work", e);
            e.printStackTrace();
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.bus_route_map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);

    }

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

            // Place the location button
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(BusRouteOnMapActivity.this, BusDetailsActivity.class);
                    intent.putExtra("bus_id", marker.getTitle());
                    startActivity(intent);
                }
            });

        }
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
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), BusApplication.DEFAULT_ZOOM);

                            // Setup nearby bus markers
                            setBusMarkers();

                        } else {
                            // Task unsuccessful
                            Toast.makeText(BusRouteOnMapActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
        }
    }

    // Set nearby bus markers on map
    void setBusMarkers() {
        String query_url = "https://busappcol740.000webhostapp.com/get_all_buses.php?case=1&lat=" + currentLocation.getLatitude() + "&lng=" + currentLocation.getLongitude();
        new FetchSQLQuery(BusRouteOnMapActivity.this, new FetchSQLQuery.AsyncResponse() {
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

    private void moveCamera(LatLng latLng, float zoom) {
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cu);
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // This method is executed once the places api is finished. It places the route on the map
    @Override
    public void onTaskDone(Object... values) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // Add starting and final location markers
        LatLng startingLocation = ((BusApplication) this.getApplication()).getStartingLocation();
        LatLng finalLocation = ((BusApplication) this.getApplication()).getFinalLocation();
        mMap.addMarker(new MarkerOptions().position(startingLocation));
        mMap.addMarker(new MarkerOptions().position(finalLocation));

        ArrayList<PolylineOptions> arr = (ArrayList<PolylineOptions>) values[0];
        for(int i = 0; i < arr.size(); i++) {
            Polyline p = mMap.addPolyline(arr.get(i));
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
