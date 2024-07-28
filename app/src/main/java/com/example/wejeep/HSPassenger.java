package com.example.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caverock.androidsvg.BuildConfig;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

public class HSPassenger extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    FirebaseAuth auth;
    FirebaseUser user;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;

    private MapView mapView;
    private boolean isLocationEnabled = false;
    private Marker locationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure the osmDroid library
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        setContentView(R.layout.activity_hspassenger);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarHSP);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.itmHomeHSP:
                        Toast.makeText(HSPassenger.this, "Home", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(HSPassenger.this, HSPassenger.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.itmSignoutHSP:
                        Toast.makeText(HSPassenger.this, "Signout", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(HSPassenger.this, MainActivity.class));
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            // User is not logged in, redirect to Login activity
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            // User is logged in, update UI with user information
            View headerView = navigationView.getHeaderView(0);
            ImageView ivProfilePictureHSP = headerView.findViewById(R.id.ivProfilePictureHSP);
            TextView tvNameHSP = headerView.findViewById(R.id.tvNameHSP);

            tvNameHSP.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivProfilePictureHSP);
            }
        }

        // Initialize the Map
        mapView = findViewById(R.id.map);
        mapView.setMultiTouchControls(true);

        // Initialize the marker
        locationMarker = new Marker(mapView);
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set up the toggle button
        Button toggleLocationButton = findViewById(R.id.toggleLocationButton);
        toggleLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocationEnabled) {
                    disableMyLocation();
                } else {
                    enableMyLocation();
                }
            }
        });
    }

    private void enableMyLocation() {
        isLocationEnabled = true;

        // Use a fixed location for demonstration purposes
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944); // Example: Eiffel Tower
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(startPoint);

        // Add the marker to the map
        locationMarker.setPosition(startPoint);
        mapView.getOverlays().add(locationMarker);
        mapView.invalidate();
    }

    private void disableMyLocation() {
        isLocationEnabled = false;
        mapView.getOverlays().remove(locationMarker);
        mapView.invalidate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
    }
}
