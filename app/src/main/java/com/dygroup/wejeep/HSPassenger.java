package com.dygroup.wejeep;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.util.Calendar;
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
    private String locationIndicator = "off";  // Default value is "off"
    private Handler handler;
    private Runnable checkScheduleRunnable;
    private boolean isScheduleChecking = false; // To track the state
    //Obsolete code
    /**@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    MenuCompat.setGroupDividerEnabled(menu, true);
    return true;
    }
     **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hspassenger);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        locationTimerHandler = new Handler(Looper.getMainLooper());

        Toolbar toolbar = findViewById(R.id.toolbarHSP);

        // Initialize navigationManager and navigationView for menuVisibilityManager
        navigationManager = new NavigationManager(this);
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
        //Check user's role and update the marker icon
        fetchUserRole();

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
        // Start periodic schedule checking as soon as the activity is created
        startScheduleChecking(toggleLocationButton);
        stopScheduleChecking(toggleLocationButton);

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
        FirebaseFirestore dbs = FirebaseFirestore.getInstance();
        TextView passengerCountTextView = findViewById(R.id.tvPassengerCount);
        listenForPassengerCount(dbs, passengerCountTextView);  // Call the private function
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            isLocationEnabled = true;
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(1000); // 1 second
            locationRequest.setFastestInterval(500); // 0.5 seconds
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (locationCallback == null) {
                Log.d(TAG, "LocationCallback is null");
                return;
            }
            locationIndicator = "on";  // Set the location indicator to "on"
            Log.d(TAG, "Location is ON, indicator: " + locationIndicator);

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
            // Update the user's locationindication to "off" if the role is "pao"
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if ("pao".equals(role)) {
                                db.collection("users").document(userId)
                                        .update("locationindicator", "on")
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Location indication set to on"))
                                        .addOnFailureListener(e -> Log.w(TAG, "Error updating location indicator", e));
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Error fetching user data", e));
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
        locationIndicator = "off";  // Set the location indicator to "off"
        Log.d(TAG, "Location is OFF, indicator: " + locationIndicator);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();

        // Update the user's locationindication to "off" if the role is "pao"
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("pao".equals(role)) {
                            db.collection("users").document(userId)
                                    .update("locationindicator", "off")
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Location indication set to OFF"))
                                    .addOnFailureListener(e -> Log.w(TAG, "Error updating location indication", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching user data", e));

        // Remove user's location from Firestore
        db.collection("locations").document(userId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Location removed from Firestore"))
                .addOnFailureListener(e -> Log.w(TAG, "Error removing location", e));

        removeUserLocationFromFirestore();
        cancelLocationTimer();
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
    private void addOthersMarkerToMap(GeoPoint geoPoint, String userRole) {
        if (mapView == null) {
            Log.e(TAG, "mapView is null. Reinitializing...");
            mapView = new MapView(this);
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

   private void listenToOtherUsersLocations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    // Fetch the current user's role before processing other users' locations
                    String currentUserId = user.getUid();
                    db.collection("users").document(currentUserId).get()
                            .addOnSuccessListener(userDocument -> {
                                if (userDocument.exists()) {
                                    String currentUserRole = userDocument.getString("role");

                                    // Check if the currentUserRole is null
                                    if (currentUserRole == null) {
                                        Log.w(TAG, "Current user role is null");
                                        return; // Exit if current user role is null
                                    }

                                    // Clear other users' markers (won't clear the current user's marker)
                                    for (int i = mapView.getOverlays().size() - 1; i >= 0; i--) {
                                        Overlay overlay = mapView.getOverlays().get(i);
                                        if (overlay instanceof Marker && overlay != locationMarker) {
                                            mapView.getOverlays().remove(overlay);
                                        }
                                    }

                                    // Now, process the location documents
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        Double latitude = document.getDouble("latitude");
                                        Double longitude = document.getDouble("longitude");

                                        if (latitude != null && longitude != null) {
                                            GeoPoint geoPoint = new GeoPoint(latitude, longitude);

                                            // Skip the current user's location
                                            if (document.getId().equals(currentUserId)) {
                                                continue; // Skip adding marker for the current user
                                            }

                                            // Fetch the user's role
                                            String userId = document.getId();
                                            db.collection("users").document(userId).get()
                                                    .addOnSuccessListener(otherUserDocument -> {
                                                        if (otherUserDocument.exists()) {
                                                            String userRole = otherUserDocument.getString("role");
                                                            Log.d(TAG,"Current user role is: "+currentUserRole);
                                                            // If current user is PAO, they can see both PAOs and passengers
                                                            if ("pao".equals(currentUserRole)) {
                                                                try {
                                                                    addOthersMarkerToMap(geoPoint, userRole);
                                                                } catch (Exception error) {
                                                                    Log.e("HSPassenger", "The exception is: ", error);
                                                                }
                                                            }

                                                            // If current user is passenger, they can only see PAOs
                                                            if ("passenger".equals(currentUserRole) && "pao".equals(userRole)) {
                                                                try {
                                                                    addOthersMarkerToMap(geoPoint, userRole);
                                                                } catch (Exception error) {
                                                                    Log.e("HSPassenger", "The exception is: ", error);
                                                                }
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Log.w(TAG, "Latitude or Longitude is null for document: " + document.getId());
                                        }
                                    }
                                } else {
                                    Log.w(TAG, "Current user document not found");
                                }
                            })
                            .addOnFailureListener(er -> {
                                Log.e(TAG, "Error fetching current user role: ", er);
                            });
                });
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
        mapView.getController().setZoom(19.0); // Set your desired zoom level
    }
    private void listenForPassengerCount(FirebaseFirestore db, TextView tvPassengerCount) {
        // Reference to the locations collection
        CollectionReference locationsRef = db.collection("locations");

        // Real-time listener to count documents where the role is 'passenger'
        locationsRef.whereEqualTo("role", "passenger")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e("FirestoreError", "Error listening to locations collection", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        // Count the number of documents in the snapshot
                        int passengerCount = querySnapshot.size();

                        // Update the TextView with the new count
                        tvPassengerCount.setText(String.valueOf(passengerCount));
                    }
                });
    }
    private void updateLocationInFirestore(Location location) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        // Reference to the user document in the 'users' collection
        DocumentReference userRef = db.collection("users").document(userId);

        // Retrieve the user's role before updating the location
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");

                // Prepare location data including the user's role
                Map<String, Object> locationData = new HashMap<>();
                locationData.put("latitude", location.getLatitude());
                locationData.put("longitude", location.getLongitude());
                locationData.put("timestamp", System.currentTimeMillis());
                locationData.put("role", role); // Add the role to the location data

                // Update the 'locations' collection
                db.collection("locations").document(userId)
                        .set(locationData)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Location updated in Firestore"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error updating location", e));
            } else {
                Log.w(TAG, "User document not found");
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Error retrieving user role", e));
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
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelLocationTimer();
        mapView.onDetach();
        removeUserLocationFromFirestore();
    }
    private void removeUserLocationFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        db.collection("locations").document(userId).delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Location removed on sign out"))
                .addOnFailureListener(e -> Log.w(TAG, "Error removing location on sign out", e));
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
    private void startScheduleChecking(Button toggleLocationButton) {
        // Check if the user is PAO first
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        // Only proceed if the user is PAO
                        if ("pao".equalsIgnoreCase(role)) {
                            if (isScheduleChecking) {
                                // Skip starting if it's already running
                                return;
                            }

                            if (handler != null && checkScheduleRunnable != null) {
                                // Remove any existing scheduled task
                                handler.removeCallbacks(checkScheduleRunnable);
                            }

                            handler = new Handler();
                            isScheduleChecking = true;

                            // Define the periodic task that will run every 10 seconds
                            checkScheduleRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    // Log that we're checking the schedule
                                    Log.d(TAG, "Checking user schedule...");

                                    // Check the schedule and manage the toggle button state
                                    checkUserSchedule(toggleLocationButton);
                                    enableMyLocation();

                                    // Continue checking every 10 seconds for better responsiveness
                                    handler.postDelayed(this, 10000);
                                }
                            };

                            // Log that the runnable is posted
                            Log.d(TAG, "Posting checkScheduleRunnable...");
                            handler.post(checkScheduleRunnable);
                        }
                    } else {
                        // Handle case where user document doesn't exist
                        Log.e(TAG, "User not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user role", e);
                    // Handle failure without Toasts
                });
    }

    private void stopScheduleChecking(Button toggleLocationButton) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        // Only proceed if the user is PAO
                        if ("pao".equalsIgnoreCase(role)) {
                            if (!isScheduleChecking) {
                                // Skip stopping if the schedule checking is not running
                                return;
                            }

                            // Remove the scheduled runnable and stop further checks
                            if (handler != null && checkScheduleRunnable != null) {
                                handler.removeCallbacks(checkScheduleRunnable);
                            }

                            isScheduleChecking = false; // Set the state to stopped

                            // Turn off the location toggle button explicitly
                            disableMyLocation();
                            toggleLocationButton.setEnabled(false);
                            toggleLocationButton.setText("Location is Off");
                            toggleLocationButton.setBackground(ContextCompat.getDrawable(this, R.drawable.round_btn_orange));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user role", e);
                    // Handle failure, you can log it or take other actions, but no Toasts here
                });
    }

    private void checkUserSchedule(Button toggleLocationButton) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        Log.d(TAG, "Current Time: " + currentHour + ":" + currentMinute);

        // Disable mapView initially to prevent user interaction while checking the schedule
        mapView.setEnabled(false);

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        if ("pao".equalsIgnoreCase(role)) {
                            db.collection("assigns")
                                    .whereEqualTo("email", user.getEmail())
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        if (!queryDocumentSnapshots.isEmpty()) {
                                            boolean isScheduleValid = false;

                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                String fromDayStr = document.getString("fromday");
                                                String toDayStr = document.getString("today");
                                                String fromTimeStr = document.getString("fromtime");
                                                String toTimeStr = document.getString("totime");

                                                int fromDay = getDayOfWeek(fromDayStr);
                                                int toDay = getDayOfWeek(toDayStr);

                                                try {
                                                    int[] fromTime = parseTime(fromTimeStr);
                                                    int fromHour = fromTime[0], fromMinute = fromTime[1];
                                                    int[] toTime = parseTime(toTimeStr);
                                                    int toHour = toTime[0], toMinute = toTime[1];

                                                    Log.d(TAG, "From Time: " + fromHour + ":" + fromMinute);
                                                    Log.d(TAG, "To Time: " + toHour + ":" + toMinute);

                                                    // Check if current day and time are valid for the range of days
                                                    if (isScheduleValidForRange(currentDayOfWeek, currentHour, currentMinute,
                                                            fromDay, fromHour, fromMinute, toDay, toHour, toMinute)) {
                                                        isScheduleValid = true;
                                                        break;  // Schedule is valid, break the loop
                                                    }
                                                } catch (IllegalArgumentException e) {
                                                    Log.e(TAG, "Invalid time format for schedule", e);
                                                }
                                            }

                                            if (isScheduleValid) {
                                                enableMyLocation();
                                                toggleLocationButton.setEnabled(true);
                                                toggleLocationButton.setText("Location is On");
                                                toggleLocationButton.setBackground(ContextCompat.getDrawable(this, R.drawable.round_btn_blue));
                                                updateLocationIndicator("on");
                                                Log.d(TAG, "Location Indicator: on");
                                            } else {
                                                disableMyLocation();
                                                mapView.setEnabled(false);
                                                toggleLocationButton.setEnabled(false);
                                                toggleLocationButton.setText("Location is Off");
                                                toggleLocationButton.setBackground(ContextCompat.getDrawable(this, R.drawable.round_btn_orange));
                                                updateLocationIndicator("off");
                                                Log.d(TAG, "Location Indicator: off");
                                            }

                                        } else {
                                            disableMyLocation();
                                            mapView.setEnabled(false);
                                            toggleLocationButton.setEnabled(false);
                                            toggleLocationButton.setText("Location is Off");
                                            toggleLocationButton.setBackground(ContextCompat.getDrawable(this, R.drawable.round_btn_orange));
                                            updateLocationIndicator("off");
                                        }

                                        // Enable mapView after checking the schedule
                                        mapView.setEnabled(true);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error checking schedules", e);
                                        disableMyLocation();
                                        mapView.setEnabled(false);
                                        toggleLocationButton.setEnabled(false);
                                        toggleLocationButton.setText("Location is Off");
                                        updateLocationIndicator("off");

                                        // Enable mapView after error
                                        mapView.setEnabled(true);
                                    });
                        }
                    } else {
                        Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
                        // Enable mapView if the user is not found
                        mapView.setEnabled(true);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user role", e);
                    Toast.makeText(this, "Error fetching user role.", Toast.LENGTH_SHORT).show();
                    // Enable mapView after failure
                    mapView.setEnabled(true);
                });
    }



    // Helper method to update the location indicator in Firestore
    private void updateLocationIndicator(String state) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .update("locationindicator", state)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Location indicator updated in Firestore: " + state);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating location indicator", e);
                });
    }
    // Utility function to convert day names to integers
    private int getDayOfWeek(String day) {
        switch (day.toLowerCase()) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: return -1; // Invalid day name
        }
    }

    // Utility function to convert 12-hour time with AM/PM to 24-hour format
    private int[] parseTime(String timeStr) throws IllegalArgumentException {
        String[] parts = timeStr.split(" "); // Split "03:15 PM" into ["03:15", "PM"]
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid time format: " + timeStr);
        }

        String[] timeParts = parts[0].split(":"); // Split "03:15" into ["03", "15"]
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        String period = parts[1]; // AM/PM

        // Convert to 24-hour format
        if (period.equalsIgnoreCase("PM") && hour != 12) {
            hour += 12; // Convert PM to 24-hour format
        } else if (period.equalsIgnoreCase("AM") && hour == 12) {
            hour = 0; // Handle midnight (12 AM -> 0)
        }

        return new int[]{hour, minute};
    }

    // Check if the current day and time is within the assigned schedule range
    private boolean isScheduleValidForRange(int currentDayOfWeek, int currentHour, int currentMinute,
                                            int fromDay, int fromHour, int fromMinute,
                                            int toDay, int toHour, int toMinute) {

        Log.d(TAG, "Current Day: " + currentDayOfWeek + ", From Day: " + fromDay + ", To Day: " + toDay);

        // Check if the current day is within the schedule range
        boolean isDayValid = false;
        if (fromDay <= toDay) {
            isDayValid = (currentDayOfWeek >= fromDay && currentDayOfWeek <= toDay);
        } else {
            isDayValid = (currentDayOfWeek >= fromDay || currentDayOfWeek <= toDay);
        }

        Log.d(TAG, "Is Day Valid: " + isDayValid);

        // Check if the current time is within the start and end time window
        boolean isTimeValid = false;

        if (currentDayOfWeek == fromDay) {
            isTimeValid = isAfter(currentHour, currentMinute, fromHour, fromMinute);
        } else if (currentDayOfWeek == toDay) {
            isTimeValid = isBefore(currentHour, currentMinute, toHour, toMinute);
        } else if (isDayValid) {
            isTimeValid = true;
        }

        // Handle cross-midnight times
        if (fromHour > toHour || (fromHour == toHour && fromMinute > toMinute)) {
            if (currentDayOfWeek == fromDay) {
                isTimeValid = isAfter(currentHour, currentMinute, fromHour, fromMinute) ||
                        isBefore(currentHour, currentMinute, toHour, toMinute);
            } else if (currentDayOfWeek == toDay) {
                isTimeValid = isBefore(currentHour, currentMinute, toHour, toMinute);
            }
        }

        Log.d(TAG, "Is Time Valid: " + isTimeValid);

        return isDayValid && isTimeValid;
    }


    // Helper functions
        private boolean isAfter(int currentHour, int currentMinute, int hour, int minute) {
            boolean result = (currentHour > hour || (currentHour == hour && currentMinute >= minute));
            Log.d(TAG, "isAfter: " + result);
            return result;
        }

        private boolean isBefore(int currentHour, int currentMinute, int hour, int minute) {
            boolean result = (currentHour < hour || (currentHour == hour && currentMinute < minute));
            Log.d(TAG, "isBefore: " + result);
            return result;
        }

    private void updateCurrentUserMarker(String userRole) {
        // Set marker icon based on the logged-in user's role
        if ("passenger".equals(userRole)) {
            locationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.self_marker_icon));
        } else if ("pao".equals(userRole)) {
            locationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.pao_marker));
        }

        // Add or update the marker on the map
        if (!mapView.getOverlays().contains(locationMarker)) {
            mapView.getOverlays().add(locationMarker);
        }
        mapView.invalidate();  // Refresh the map
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