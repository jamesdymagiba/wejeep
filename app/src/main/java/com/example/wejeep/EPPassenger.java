package com.example.wejeep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class EPPassenger extends AppCompatActivity {
    Button btnBackEPP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eppassenger);

        btnBackEPP = findViewById(R.id.btnBackEPP);

        btnBackEPP.setOnClickListener(view -> {
            startActivity(new Intent(EPPassenger.this, PPassenger.class));
        });


    }
}