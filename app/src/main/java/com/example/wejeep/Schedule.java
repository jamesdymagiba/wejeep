package com.example.wejeep;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Schedule extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView tvDay, tvTime, tvModel, tvPlate, tvDriver;
    private static final String TAG = "ScheduleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize TextViews
        tvDay = findViewById(R.id.tvday);
        tvTime = findViewById(R.id.tvtime);
        tvModel = findViewById(R.id.tvmodel);
        tvPlate = findViewById(R.id.tvplate);
        tvDriver = findViewById(R.id.tvdriver);

        // Initialize the toolbar
        Toolbar toolbar = findViewById(R.id.toolbarSchedule);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set up DrawerToggle for Navigation Drawer
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set up NavigationView item selection listener
        navigationView.setNavigationItemSelectedListener(item -> handleNavigationItemSelected(item));

        // Check user authentication and set the profile info
        checkUserAuthentication();

        // Retrieve schedule data from Firestore
        fetchScheduleData();
    }

    private void fetchScheduleData() {
        db.collection("assigns")
                .document("crm12yeZau2ZAewOia55")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve data and set it to TextViews
                        String day = documentSnapshot.getString("fromday") + " - " + documentSnapshot.getString("today");
                        String time = documentSnapshot.getString("fromtime") + " - " + documentSnapshot.getString("totime");
                        String model = documentSnapshot.getString("unitnumber");
                        String plate = documentSnapshot.getString("platenumber");
                        String driver = documentSnapshot.getString("driver");

                        // Set the data to TextViews
                        tvDay.setText(day);
                        tvTime.setText(time);
                        tvModel.setText(model);
                        tvPlate.setText(plate);
                        tvDriver.setText(driver);
                    }
                })
                .addOnFailureListener(e -> {
                    // Log the error
                    Log.e(TAG, "Error fetching schedule data", e);
                });
    }

    private void fetchUserRole() {
        // Get the current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            // Fetch the user's role from Firestore
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userRole = documentSnapshot.getString("role");
                            setMenuVisibility(userRole);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching user role", e));
        } else {
            Log.e(TAG, "User is not authenticated");
        }
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
            menu.setGroupVisible(R.id.admin, true);
        }
    }

    private boolean handleNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itmHomeHSP:
                startActivity(new Intent(Schedule.this, HSPassenger.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.itmSignoutHSP:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Schedule.this, MainActivity.class));
                finish();
                return true;
            case R.id.itmProfileHSP:
                startActivity(new Intent(Schedule.this, PPassenger.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.itmManageDriverHSP:
                startActivity(new Intent(Schedule.this, AdminManageDriver.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.itmManagePAOHSP:
                startActivity(new Intent(Schedule.this, AdminManagePAO.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.itmScheduleHSP:
                finish();  // Avoid launching the same activity
                startActivity(new Intent(Schedule.this, Schedule.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            default:
                return false;
        }
    }

    private void checkUserAuthentication() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

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
            fetchUserRole();  // Fetch user role to set menu visibility
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
