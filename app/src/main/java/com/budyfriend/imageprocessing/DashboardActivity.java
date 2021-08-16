package com.budyfriend.imageprocessing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DashboardActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        CardView detectCard = findViewById(R.id.detectCard);
        CardView aboutCard = findViewById(R.id.aboutCard);
        CardView symptoms = findViewById(R.id.symptomsCard);
        CardView preventionCard = findViewById(R.id.preventionCard);

    }

    public void detectGoTo(View view) {
        Intent a = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(a);
    }

    public void aboutGoTo(View view) {
        Intent a = new Intent(getApplicationContext(), aboutActivity.class);
        startActivity(a);
    }

    public void symptomsGoTo(View view) {
        Intent a = new Intent(getApplicationContext(), symptomsActivity.class);
        startActivity(a);
    }

    public void preventionGoTo(View view) {
        Intent a = new Intent(getApplicationContext(), preventionActivity.class);
        startActivity(a);
    }
}