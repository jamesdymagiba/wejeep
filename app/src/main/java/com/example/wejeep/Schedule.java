package com.example.wejeep;

import androidx.annotation.NonNull;
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
    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
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

        // Initialize navigationManager and navigationView for menuVisibilityManager
        navigationManager = new NavigationManager(this);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Initialize MenuVisibilityManager with the NavigationView
        Menu menu = navigationView.getMenu();
        menuVisibilityManager = new MenuVisibilityManager(this);
        menuVisibilityManager.fetchUserRole(menu);

        //Initialize Drawer for menu
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean handled = navigationManager.handleNavigationItemSelected(item, Schedule.this);
                drawerLayout.closeDrawer(GravityCompat.START);
                return handled;
            }
        });

        UserProfileManager.checkAuthAndUpdateUI(FirebaseAuth.getInstance(), navigationView, this);

        // Retrieve schedule data from Firestore
        fetchScheduleData();
    }

    private void fetchScheduleData() {
        // Adjust document ID to dynamically retrieve schedule if needed
        db.collection("assigns")
                .document("5UKWKxmroLNiltYJBcJL") // Replace with actual document ID or retrieve dynamically
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
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            BackPressHandler.handleBackPress(this);
        }
    }
}
