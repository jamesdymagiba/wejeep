package com.example.wejeep;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.drawerlayout.widget.DrawerLayout;
public class EPPassenger extends AppCompatActivity {
    Button btnBackEPP;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eppassenger);

        btnBackEPP = findViewById(R.id.btnBackEPP);
        drawerLayout = findViewById(R.id.drawer_layout);

        // Disable swipe to open the drawer
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        btnBackEPP.setOnClickListener(view -> {
            startActivity(new Intent(EPPassenger.this, PPassenger.class));
        });
    }
}
