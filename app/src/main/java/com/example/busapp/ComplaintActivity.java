package com.example.busapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class ComplaintActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complaints_view);
    }
}
