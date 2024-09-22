package com.example.wejeep;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.caverock.androidsvg.BuildConfig;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import android.graphics.drawable.Drawable;

import java.util.HashMap;
import java.util.Map;

public class HSPassenger extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "HSPassenger";

    private FirebaseAuth auth;
    private FirebaseUser user;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    private MapView mapView;
    private boolean isLocationEnabled = false;
    private Marker locationMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Handler locationTimerHandler;
    private Runnable locationTimerRunnable;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuCompat.setGroupDividerEnabled(menu, true);

        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hspassenger);

        locationTimerHandler = new Handler(Looper.getMainLooper());

        Toolbar toolbar = findViewById(R.id.toolbarHSP);


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        Drawable drawable = drawerToggle.getDrawerArrowDrawable();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
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
                    case R.id.itmProfileHSP:
                        Toast.makeText(HSPassenger.this, "Profile", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(HSPassenger.this, PPassenger.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
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
            fetchUserRole();
        }
        // Configure the osmDroid library
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        // Initialize the Map
        mapView = findViewById(R.id.map);
        mapView.setMultiTouchControls(true);

        // Initialize the marker
        locationMarker = new Marker(mapView);
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Set custom icon for the marker
        locationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.people));

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Set up location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocationOnMap(location);
                    updateLocationInFirestore(location);
                }
            }
        };
        // Set up the toggle button
        Button toggleLocationButton = findViewById(R.id.btnToggleLocationHSP);
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



        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            listenToOtherUsersLocations();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            isLocationEnabled = true;
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000); // 10 seconds
            locationRequest.setFastestInterval(5000); // 5 seconds
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (locationCallback == null) {
                Log.d(TAG, "LocationCallback is null");
                return;
            }

            try {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } catch (SecurityException e) {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "SecurityException in enableMyLocation", e);
            }
            Button toggleLocationButton = findViewById(R.id.btnToggleLocationHSP);
            toggleLocationButton.setText("Location is On");
            toggleLocationButton.setBackground(ContextCompat.getDrawable(this, R.drawable.round_btn_blue));

            startLocationTimer();
            Toast.makeText(this, "Location will be turned off automatically after 5 minutes", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableMyLocation() {
        isLocationEnabled = false;
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        } catch (SecurityException e) {
            Toast.makeText(this, "Failed to stop location updates", Toast.LENGTH_SHORT).show();
        }
        mapView.getOverlays().remove(locationMarker);
        mapView.invalidate();

        Button toggleLocationButton = findViewById(R.id.btnToggleLocationHSP);
        toggleLocationButton.setText("Location is Off");
        toggleLocationButton.setBackground(ContextCompat.getDrawable(this, R.drawable.round_btn_orange));

        cancelLocationTimer();
    }
    private void listenToOtherUsersLocations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    // CLear other user's markers
                    for (int i = mapView.getOverlays().size() - 1; i >= 0; i--) {
                        Overlay overlay = mapView.getOverlays().get(i);
                        if (overlay instanceof Marker && overlay != locationMarker) {
                            mapView.getOverlays().remove(overlay);
                        }
                    }

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        if (!document.getId().equals(user.getUid())) { // Skip the current user's location
                            Double latitude = document.getDouble("latitude");
                            Double longitude = document.getDouble("longitude");

                            if (latitude != null && longitude != null) {
                                GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                                // Create a marker for other users' locations
                                Marker otherUserMarker = new Marker(mapView);
                                otherUserMarker.setPosition(geoPoint);
                                otherUserMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.people));
                                otherUserMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                                // Add the marker to the map
                                mapView.getOverlays().add(otherUserMarker);
                            } else {
                                Log.w(TAG, "Latitude or Longitude is null for document: " + document.getId());
                            }
                        }
                    }
                    mapView.invalidate();
                });
    }

    private void updateLocationOnMap(Location location) {
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().setZoom(19.0);
        mapView.getController().setCenter(geoPoint);

        locationMarker.setPosition(geoPoint);
        if (!mapView.getOverlays().contains(locationMarker)) {
            mapView.getOverlays().add(locationMarker);
        }
        mapView.invalidate();
    }

    private void updateLocationInFirestore(Location location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        locationData.put("timestamp", System.currentTimeMillis());

        db.collection("locations").document(userId)
                .set(locationData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Location updated in Firestore"))
                .addOnFailureListener(e -> Log.w(TAG, "Error updating location", e));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);  // Call super method

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void startLocationTimer() {
        locationTimerRunnable = new Runnable() {
            @Override
            public void run() {
                // Automatically disable location after 5 minutes
                disableMyLocation();
                Toast.makeText(HSPassenger.this, "Location turned off", Toast.LENGTH_SHORT).show();
            }
        };
        locationTimerHandler.postDelayed(locationTimerRunnable, 300000);
    }

    private void cancelLocationTimer() {
        if (locationTimerHandler != null && locationTimerRunnable != null) {
            locationTimerHandler.removeCallbacks(locationTimerRunnable);
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
        if (isLocationEnabled) {
            disableMyLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelLocationTimer();
        mapView.onDetach();
    }
    private void fetchUserRole() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("role");
                        setMenuVisibility(userRole);
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching user role", e));
    }
    private void setMenuVisibility(String userRole) {
        Menu menu = navigationView.getMenu();

        // Hide all groups first
        menu.setGroupVisible(R.id.passenger, false);
        menu.setGroupVisible(R.id.pao, false);
        menu.setGroupVisible(R.id.admin, false);

        // Show the relevant group based on the user's role
        if ("passenger".equals(userRole)) {
            menu.setGroupVisible(R.id.passenger, true);
        } else if ("pao".equals(userRole)) {
            menu.setGroupVisible(R.id.passenger, true);
            menu.setGroupVisible(R.id.pao, true);
        } else if ("admin".equals(userRole)) {
            menu.setGroupVisible(R.id.passenger, true);
            menu.setGroupVisible(R.id.pao, true);
            menu.setGroupVisible(R.id.admin, true);
        }
    }


}
