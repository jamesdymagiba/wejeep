package com.example.wejeep;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.caverock.androidsvg.BuildConfig;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.HashMap;
import java.util.Map;

public class HSPassenger extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "HSPassenger";

    private FirebaseAuth auth;
    private FirebaseUser user;
    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
    private Menu menu;
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
    private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hspassenger);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        locationTimerHandler = new Handler(Looper.getMainLooper());

        Toolbar toolbar = findViewById(R.id.toolbarHSP);

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

        // Initialize navigationManager and navigationView for menuVisibilityManager
        navigationManager = new NavigationManager(this, fusedLocationClient, locationCallback, mapView);
        navigationView = findViewById(R.id.nav_view);

        // Initialize MenuVisibilityManager with the NavigationView
        Menu menu = navigationView.getMenu();
        menuVisibilityManager = new MenuVisibilityManager(this);
        menuVisibilityManager.fetchUserRole(menu);

        // Initialize drawer for menu
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        Drawable drawable = drawerToggle.getDrawerArrowDrawable();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

             navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    boolean handled = navigationManager.handleNavigationItemSelected(item, HSPassenger.this);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return handled;
                }
            });

        //Add profile picture and name from firestore in header
        UserProfileManager.checkAuthAndUpdateUI(FirebaseAuth.getInstance(), navigationView, this);

        // Configure the osmDroid library
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        // Initialize the Map
        initializeMapView();

        // Initialize the marker
        locationMarker = new Marker(mapView);
        locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Set custom icon for the marker
        locationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.people));
        // Set up the toggle button
        Button toggleLocationButton = findViewById(R.id.btnToggleLocationHSP);

        // Disable button initially until the schedule is verified
        toggleLocationButton.setEnabled(true);

        // Check user's schedule
        checkUserSchedule(toggleLocationButton);

        toggleLocationButton.setOnClickListener(v -> {
            if (isLocationEnabled) {
                disableMyLocation();
            } else {
                enableMyLocation();
            }
        });

        //Check user's role and update the marker icon
        fetchUserRole();

        // Set up the toggle button
        toggleLocationButton = findViewById(R.id.btnToggleLocationHSP);
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
    private void clearMap() {
        if (mapView != null) {
            mapView.getOverlays().clear();
            mapView.invalidate();
        }
    }
    private void initializeMapView() {
        if (mapView == null) {
            mapView = findViewById(R.id.map);
            mapView.setMultiTouchControls(true);
            mapView.getController().setZoom(19.0);
        }
        mapView.getOverlays().clear(); // Clear previous markers
        mapView.invalidate(); // Refresh the map
    }
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            isLocationEnabled = true;
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(1000); // 1 second
            locationRequest.setFastestInterval(500); // .5 seconds
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

            // Check the user's role and skip the timer for "pao" role
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userRole = documentSnapshot.getString("role");
                            if (!"pao".equalsIgnoreCase(userRole)) {
                                startLocationTimer(); // Start timer only for non-pao users
                                Toast.makeText(HSPassenger.this, "Location will be turned off automatically after 5 minutes", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

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

        removeUserLocationFromFirestore();
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

                    // Clear other users' markers
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

                                // Fetch the user's role
                                String userId = document.getId();
                                db.collection("users").document(userId).get()
                                        .addOnSuccessListener(userDocument -> {
                                            if (userDocument.exists()) {
                                                String userRole = userDocument.getString("role");
                                                Log.d(TAG, "User role: " + userRole);
                                                mapView.invalidate();

                                                try{
                                                    addMarkerToMap(geoPoint,userRole);
                                                }catch (Exception error){
                                                    Log.e("HSPassenger","The exception is: ",error);
                                                }


                                            }
                                        });
                            } else {
                                Log.w(TAG, "Latitude or Longitude is null for document: " + document.getId());
                            }
                        }
                    }
                });
    }
    private void addMarkerToMap(GeoPoint geoPoint, String userRole) {
        if (mapView == null) {
            Log.e(TAG, "mapView is null. Reinitializing...");
            mapView = new MapView(this); // or use the context for the fragment
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);
        }

        Marker otherUserMarker = new Marker(mapView);
        otherUserMarker.setPosition(geoPoint);

        if ("passenger".equals(userRole)) {
            otherUserMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.passenger_marker_icon));
        } else if ("pao".equals(userRole)) {
            otherUserMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.pao_marker));
        }

        mapView.getOverlays().add(otherUserMarker);
        mapView.invalidate();
    }
    private GeoPoint initialCenterPoint = null; // Store the initial center point

    private void updateLocationOnMap(Location location) {
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        // Center the map only once
        if (initialCenterPoint == null) {
            initialCenterPoint = geoPoint;
            centerMapOnUserLocation(initialCenterPoint);
        }

        locationMarker.setPosition(geoPoint);

        if (!mapView.getOverlays().contains(locationMarker)) {
            mapView.getOverlays().add(locationMarker);
        }
        mapView.invalidate();
    }
    private void centerMapOnUserLocation(GeoPoint geoPoint) {
        mapView.getController().setCenter(geoPoint);
        mapView.getController().setZoom(19.0);
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
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
            clearMap();
        }
        if(isLocationEnabled){
            disableMyLocation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            clearMap();
            mapView.onDetach();
        }
    }
    private void removeUserLocationFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        db.collection("locations").document(userId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Location removed in firestore"))
                .addOnFailureListener(e -> Log.w(TAG, "Error removing location in firestore", e));
    }

    private void fetchUserRole() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("role");
                        updateCurrentUserMarker(userRole);  // Update the current user's marker based on role
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching user role", e));
    }
            private void checkUserSchedule(Button toggleLocationButton) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Fetch user role from Firestore or from a locally stored reference
                db.collection("users") // Assuming you have a 'users' collection where roles are stored
                        .document(user.getUid()) // Use the current user's unique ID
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");

                                if ("pao".equalsIgnoreCase(role)) { // Check if the user is a PAO
                                    // Proceed to fetch the schedule for PAO users
                                    db.collection("assigns")
                                            .whereEqualTo("email", user.getEmail())
                                            .get()
                                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                    // Schedule exists, enable the location button
                                                    enableMyLocation();
                                                    toggleLocationButton.setEnabled(true);
                                                    toggleLocationButton.setText("Location is On");
                                                    toggleLocationButton.setBackground(ContextCompat.getDrawable(this, R.drawable.round_btn_blue));
                                                    Toast.makeText(this, "Schedule verified.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    // No schedule found
                                                    disableMyLocation();
                                                    toggleLocationButton.setEnabled(false);
                                                    toggleLocationButton.setText("Location is Off");
                                                    toggleLocationButton.setBackground(ContextCompat.getDrawable(this, R.drawable.round_btn_orange));
                                                    Toast.makeText(this, "No schedule found.", Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                // Handle errors in fetching the schedule
                                                disableMyLocation();
                                                toggleLocationButton.setEnabled(false);
                                                toggleLocationButton.setText("Location is Off");
                                                toggleLocationButton.setBackground(ContextCompat.getDrawable(this, R.drawable.round_btn_orange));
                                                Toast.makeText(this, "Error verifying schedule. Please try again later.", Toast.LENGTH_LONG).show();
                                                Log.e(TAG, "Error checking user schedule", e);
                                            });
                                } else {
                                }
                            } else {
                                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error fetching user role.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error fetching user role", e);
                        });
            }


    private void updateCurrentUserMarker(String userRole) {

        if(user!=null){
            // Set marker icon based on the logged-in user's role
            if ("passenger".equals(userRole)) {
                locationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.self_marker_icon));
            } else if ("pao".equals(userRole)) {
                locationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.pao_marker));
            } else {
                // Default marker for other roles or if role is not defined
                locationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.passenger_marker_icon));
            }

            // Add or update the marker on the map
            if (!mapView.getOverlays().contains(locationMarker)) {
                mapView.getOverlays().add(locationMarker);
            }
            mapView.invalidate();  // Refresh the map
        }else{
            Log.e("HSPassenger","Error Updating Current User Marker");
        }

    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            BackPressHandler.handleBackPress(this);
        }
    }
}
