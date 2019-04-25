package com.nitish.busapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BusRouteActivity extends AppCompatActivity {

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("jsondata.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route);
        final AutoCompleteTextView actview = findViewById(R.id.actv);
        final ListView listView = findViewById(R.id.contentView);
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset(this));

            JSONArray arrJson = obj.getJSONArray("bus_numbers");
            String[] busarr = new String[arrJson.length()];
            for(int i = 0; i < arrJson.length(); i++)
            {
                JSONObject jsonobject = arrJson.getJSONObject(i);
                busarr[i] = jsonobject.getString("bus_number");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,busarr );
            actview.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        actview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getWindowToken(), 0);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                String selection = (String)parent.getItemAtPosition(position);
                try {
                    JSONObject obj = new JSONObject(loadJSONFromAsset(getBaseContext()));
                    JSONArray arrJson = obj.getJSONArray("bus_numbers");
                    for(int i = 0; i < arrJson.length(); i++) {
                        JSONObject jsonobject = arrJson.getJSONObject(i);
                        String busno=(jsonobject.getString("bus_number"));
                        if(selection.equals(busno)) {
                            JSONArray arrstops = jsonobject.getJSONArray("stops");
                            String[] bus_stop_arr = new String[arrstops.length()];
                            for(int j = 0; j < arrstops.length(); j++) {
                                bus_stop_arr[j] = arrstops.getString(j);
                            }

                            ArrayAdapter<String> adapterx = new ArrayAdapter<String>(BusRouteActivity.this, android.R.layout.simple_list_item_1 , bus_stop_arr );
                            // Prints Pretty, Cool, Weird

                            listView.setAdapter(adapterx);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
