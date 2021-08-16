package com.budyfriend.imageprocessing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class preventionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prevention);
        getSupportActionBar().hide();
    }
}