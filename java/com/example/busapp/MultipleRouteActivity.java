package com.nitish.busapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MultipleRouteActivity extends AppCompatActivity implements
        TaskLoadedCallback {

    private static final int AUTOCOMPLETE_REQUEST_CODE_FROM_LOCATION = 1;
    private static final int AUTOCOMPLETE_REQUEST_CODE_TO_LOCATION = 2;

    // widgets
    private EditText fromLocationTextbox;
    private EditText toLocationTextbox;
    private ListView multipleRouteListview;

    // Arrays
    ArrayList<String> ROUTE_DETAIL = null;
    ArrayList<String> FARE = null;
    ArrayList<String> DISTANCE = null;
    ArrayList<String> DURATION = null;
    ArrayList<String> ARRIVAL_TIME = null;
    ArrayList<String> DEPARTURE_TIME = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiple_route_activity_view);

        // Set widgets
        fromLocationTextbox = findViewById(R.id.from_location_textbox);
        toLocationTextbox = findViewById(R.id.to_location_textbox);
        multipleRouteListview = findViewById(R.id.multiple_route_listview);

        // Fill initial locations
        fromLocationTextbox.setText(((BusApplication)this.getApplication()).getStartingLocationName());
        toLocationTextbox.setText(((BusApplication)this.getApplication()).getFinalLocationName());

        // Set the fields to specify which types of place data to return
        final List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

        fromLocationTextbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setLocationRestriction(RectangularBounds.newInstance(
                                new LatLng(28.442, 76.72),
                                new LatLng(28.858, 77.414)
                        ))
                        .build(MultipleRouteActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_FROM_LOCATION);
            }
        });

        toLocationTextbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setLocationRestriction(RectangularBounds.newInstance(
                                new LatLng(28.442, 76.72),
                                new LatLng(28.858, 77.414)
                        ))
                        .build(MultipleRouteActivity.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_TO_LOCATION);
            }
        });

        multipleRouteListview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MultipleRouteActivity.this, BusRouteOnMapActivity.class);
                intent.putExtra("bus_route",ROUTE_DETAIL.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_FROM_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                // Set global variables
                ((BusApplication) this.getApplication()).setStartingLocationName(place.getName());
                ((BusApplication) this.getApplication()).setStartingLocation(place.getLatLng());

                // Set starting location name
                fromLocationTextbox.setText(place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // handle error
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
            }
        } else if (requestCode == AUTOCOMPLETE_REQUEST_CODE_TO_LOCATION) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);

                // Set global variables
                ((BusApplication) this.getApplication()).setFinalLocationName(place.getName());
                ((BusApplication) this.getApplication()).setFinalLocation(place.getLatLng());

                // Set final location name
                toLocationTextbox.setText(place.getName());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // handle error
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        LatLng startingLocation = ((BusApplication)this.getApplication()).getStartingLocation();
        LatLng finalLocation = ((BusApplication)this.getApplication()).getFinalLocation();
        new FetchURL(MultipleRouteActivity.this, BusApplication.ROUTE_MINOR_DETAILS).execute(getUrl(startingLocation, finalLocation, "transit"));
    }

    // Makes the URL required to be sent to google places api
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String transit_mode = "transit_mode=bus";
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + transit_mode + "&alternatives=true";
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        List<HashMap<String, String>> routes = (List<HashMap<String, String>>) values[0];

        // Populate arrays
        if(ROUTE_DETAIL == null) {
            ROUTE_DETAIL = new ArrayList<>();
        } else {
            ROUTE_DETAIL.clear();
        }

        if(FARE == null) {
            FARE = new ArrayList<>();
        } else {
            FARE.clear();
        }

        if(DISTANCE == null) {
            DISTANCE = new ArrayList<>();
        } else {
            DISTANCE.clear();
        }

        if(DURATION == null) {
            DURATION = new ArrayList<>();
        } else {
            DURATION.clear();
        }

        if(ARRIVAL_TIME == null) {
            ARRIVAL_TIME = new ArrayList<>();
        } else {
            ARRIVAL_TIME.clear();
        }

        if(DEPARTURE_TIME == null) {
            DEPARTURE_TIME = new ArrayList<>();
        } else {
            DEPARTURE_TIME.clear();
        }

        for(int i=0; i < routes.size(); i++) {
            ROUTE_DETAIL.add(routes.get(i).get("route_detail"));
            FARE.add(routes.get(i).get("fare"));
            DISTANCE.add(routes.get(i).get("distance"));
            DURATION.add(routes.get(i).get("duration"));
            ARRIVAL_TIME.add(routes.get(i).get("arrival_time"));
            DEPARTURE_TIME.add(routes.get(i).get("departure_time"));
        }

        // Populate list view
        CustomAdapter customAdapter = new CustomAdapter();
        multipleRouteListview.setAdapter(customAdapter);
    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ROUTE_DETAIL.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.custom_layout, null);

            TextView eta_textview = convertView.findViewById(R.id.eta_textview);
            TextView duration_textview = convertView.findViewById(R.id.duration_textview);
            TextView fare_textview = convertView.findViewById(R.id.fare_textview);
            TextView distance_textview = convertView.findViewById(R.id.distance_textview);
//            TextView route_details_textview = convertView.findViewById(R.id.route_detail_textview);

            eta_textview.setText(DEPARTURE_TIME.get(position) + "-" + ARRIVAL_TIME.get(position));
            duration_textview.setText(DURATION.get(position));
            fare_textview.setText(FARE.get(position));
            distance_textview.setText(DISTANCE.get(position));

//            SpannableString ss = new SpannableString("abc");
//            Drawable d = getResources().getDrawable(R.drawable.bus2);
//            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
//            ss.setSpan(span, 0, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//            route_details_textview.setText(ss);

            return convertView;
        }
    }

}


