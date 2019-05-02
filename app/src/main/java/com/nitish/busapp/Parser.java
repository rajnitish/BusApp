package com.nitish.busapp;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class PointsParser extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
    // Pattern data
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    TaskLoadedCallback taskCallback;
    int detailCase;

    public PointsParser(Context mContext, int detailCase) {
        this.taskCallback = (TaskLoadedCallback) mContext;
        this.detailCase = detailCase;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<HashMap<String, String>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<HashMap<String, String>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            DataParser parser = new DataParser();

            // Starts parsing data
            routes = parser.parse(jObject, detailCase);
        } catch (Exception e) {
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<HashMap<String, String>> result) {
        if(detailCase == MainApplication.ROUTE_MINOR_DETAILS) {
            taskCallback.onTaskDone(result);
        } else if(detailCase == MainApplication.ROUTE_PATH_DETAILS) {

            // Create polyline options and pass it to ui thread
            ArrayList<LatLng> points;
            ArrayList<PolylineOptions> allPolylines = new ArrayList<>();
            PolylineOptions lineOptions = null;

            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            List<HashMap<String, String>> path = result;

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

            // Drawing polyline in the Google Map for the i-th route
            if (allPolylines != null) {
                taskCallback.onTaskDone(allPolylines);
            } else {
            }
        }
    }
}

class DataParser {
    public List<HashMap<String, String>> parse(JSONObject jObject, int detailCase) {
        List<HashMap<String, String>> routes = new ArrayList<>();

        if(detailCase == MainApplication.ROUTE_MINOR_DETAILS) {
            JSONArray jRoutes;
            try {
                jRoutes = jObject.getJSONArray("routes");
                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("route_detail", jRoutes.get(i).toString());
                    String fare = (String) ((JSONObject) ((JSONObject) jRoutes.get(i)).get("fare")).get("text");
                    hashMap.put("fare", fare);
                    JSONObject jLegs = (JSONObject) ((JSONObject) jRoutes.get(i)).getJSONArray("legs").get(0);
                    hashMap.put("distance", (String) ((JSONObject) (jLegs.get("distance"))).get("text"));
                    hashMap.put("duration",(String) ((JSONObject) (jLegs.get("duration"))).get("text"));
                    hashMap.put("arrival_time",(String) ((JSONObject) (jLegs.get("arrival_time"))).get("text"));
                    hashMap.put("departure_time",(String) ((JSONObject) (jLegs.get("departure_time"))).get("text"));
                    routes.add(hashMap);
                }

            } catch (JSONException e) {
            } catch (Exception e) {
            }
        } else if(detailCase == MainApplication.ROUTE_PATH_DETAILS) {
            JSONArray jLegs;
            JSONArray jSteps;
            try {
                jLegs = jObject.getJSONArray("legs");
                /** Traversing all legs */
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                    /** Traversing all steps */
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);
                        String stepDirectionMode = (String) ((JSONObject) jSteps.get(k)).get("travel_mode");

                        /** Traversing all points */
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString((list.get(l)).latitude));
                            hm.put("lng", Double.toString((list.get(l)).longitude));
                            hm.put("travel_mode", stepDirectionMode);
                            routes.add(hm);
                        }
                    }
                }

            } catch (JSONException e) {
            } catch (Exception e) {
            }
        }
        return routes;
    }


    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}

