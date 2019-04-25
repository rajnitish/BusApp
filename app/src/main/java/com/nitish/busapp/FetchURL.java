package com.nitish.busapp;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class FetchURL extends AsyncTask<String, Void, String> {
    Context mContext;
    int detailCase;

    public FetchURL(Context mContext, int detailCase) {
        this.mContext = mContext;
        this.detailCase = detailCase;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        PointsParser parserTask = new PointsParser(mContext, detailCase);
        // Invokes the thread for parsing the JSON data
        parserTask.execute(s);
    }

    @Override
    protected String doInBackground(String... strings) {

        String data = "";
        if(detailCase == BusApplication.ROUTE_MINOR_DETAILS) {
            try {
                // Fetching the data from web service
                data = downloadUrl(strings[0]);
            } catch (Exception e) {
            }
        } else if(detailCase == BusApplication.ROUTE_PATH_DETAILS) {
            // The string is a json object. Simply pass it to points parser
            data = strings[0];
        }
        return data;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}

