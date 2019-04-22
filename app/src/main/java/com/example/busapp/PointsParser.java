package com.example.busapp;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class PointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    // Pattern data
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    TaskLoadedCallback taskCallback;

    public PointsParser(Context mContext) {
        this.taskCallback = (TaskLoadedCallback) mContext;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            DataParser parser = new DataParser();

            // Starts parsing data
            routes = parser.parse(jObject);
        } catch (Exception e) {
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        ArrayList<PolylineOptions> allPolylines = new ArrayList<PolylineOptions>();
        PolylineOptions lineOptions = null;
        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            String travelMode = path.get(0).get("travel_mode");
            for(int j = 0; j < path.size(); j++) {
                String currTravelMode = path.get(j).get("travel_mode");
                if(travelMode.equalsIgnoreCase(currTravelMode)) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                } else {
                    // Add the last point
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);

                    lineOptions.addAll(points);
                    if(travelMode.equalsIgnoreCase("walking")) {
                        lineOptions.width(20);
                        lineOptions.color(Color.argb(128, 0, 0, 255));
                        lineOptions.pattern(PATTERN_POLYLINE_DOTTED);
                    } else {
                        lineOptions.width(20);
                        lineOptions.color(Color.argb(128, 0, 0, 255));
                    }
                    allPolylines.add(lineOptions);
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();
                    travelMode = currTravelMode;
                    j--;
                }
            }

            // Add the last polyline
            lineOptions.addAll(points);
            if(travelMode.equalsIgnoreCase("walking")) {
                lineOptions.width(20);
                lineOptions.color(Color.argb(128, 0, 0, 255));
                lineOptions.pattern(PATTERN_POLYLINE_DOTTED);
            } else {
                lineOptions.width(20);
                lineOptions.color(Color.argb(128, 0, 0, 255));
            }
            allPolylines.add(lineOptions);

        }

        // Drawing polyline in the Google Map for the i-th route
        if (allPolylines != null) {
            //mMap.addPolyline(lineOptions);
            taskCallback.onTaskDone(allPolylines);

        } else {
        }
    }
}
