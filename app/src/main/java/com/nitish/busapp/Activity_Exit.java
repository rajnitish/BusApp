package com.nitish.busapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.ImageView;

public class Activity_Exit extends Activity {

    ImageView image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exitlayout);

        image = (ImageView) findViewById(R.id.imageView1);
        image.setImageResource(R.drawable.exitimg);
        int finishTime = 3; //10 secs
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, finishTime * 1000);


    }

    private void closeApp()
    {
        SystemClock.sleep(6000);
        this.finishAffinity();
        System.exit(1);
    }

}

