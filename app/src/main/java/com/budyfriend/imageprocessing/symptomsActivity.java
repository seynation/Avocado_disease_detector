package com.budyfriend.imageprocessing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class symptomsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);
        getSupportActionBar().hide();
    }
}