package com.example.busapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BusDetailsActivity extends Activity {
    TextView busNumberTextview, driverNameTextview, busColorTextview, busCapacityTextview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_details_view);
        Integer bus_id = Integer.parseInt(getIntent().getStringExtra("bus_id"));

        // Get widgets
        busNumberTextview = findViewById(R.id.bus_no_box);
        driverNameTextview = findViewById(R.id.bus_driver_name_box);
        busColorTextview = findViewById(R.id.bus_color_box);
        busCapacityTextview = findViewById(R.id.bus_capacity_box);

        String query_url = "https://busappcol740.000webhostapp.com/get_all_buses.php?case=2&ID=" + bus_id;
        new FetchSQLQuery(BusDetailsActivity.this, new FetchSQLQuery.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                // Parse JSON string and fill information
                try {
                    JSONArray jsonArray = (new JSONObject(output)).getJSONArray("busdetail");

                    for(int i = 0; i < jsonArray.length(); i++) {
                        String bus_number = (String) ((JSONObject) jsonArray.get(0)).get("Bus_Number");
                        String driver_name = (String) ((JSONObject) jsonArray.get(0)).get("Driver_Name");
                        String bus_color = (String) ((JSONObject) jsonArray.get(0)).get("Bus_Color");
                        String bus_capacity = (String) ((JSONObject) jsonArray.get(0)).get("Capacity");

                        // Populate text boxes
                        busNumberTextview.setText(bus_number);
                        driverNameTextview.setText(driver_name);
                        busColorTextview.setText(bus_color);
                        busCapacityTextview.setText(bus_capacity);
                    }
                } catch (JSONException e) {
                }
            }
        }).execute(query_url);
    }
}
