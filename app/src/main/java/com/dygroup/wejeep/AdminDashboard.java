package com.dygroup.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminDashboard extends AppCompatActivity {
    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
    private DrawerLayout drawerLayout;
    private FirebaseFirestore db; // Declare Firestore reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();


        GridLayout dashboardGrid = findViewById(R.id.dashboardGrid);

        LinearLayout boxDrivers = findViewById(R.id.box_drivers);
        LinearLayout boxPAOs = findViewById(R.id.box_paos);
        LinearLayout boxUnits = findViewById(R.id.box_units);
        LinearLayout boxSchedules = findViewById(R.id.box_schedules);
        LinearLayout boxAssignedSchedules = findViewById(R.id.box_assignedSchedules);

        Toolbar toolbar = findViewById(R.id.toolbarAdminDashboard);

        // Initialize navigationManager and navigationView for menuVisibilityManager
        navigationManager = new NavigationManager(this);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Initialize MenuVisibilityManager with the NavigationView
        Menu menu = navigationView.getMenu();
        menuVisibilityManager = new MenuVisibilityManager(this);
        menuVisibilityManager.fetchUserRole(menu);

        // Initialize Drawer for menu
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState(); // Sync state after the listener is added

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean handled = navigationManager.handleNavigationItemSelected(item, AdminDashboard.this);
                drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer after selection
                return handled;
            }
        });

        //Add profile picture and name from firestore in header
        UserProfileManager.checkAuthAndUpdateUI(FirebaseAuth.getInstance(), navigationView, this);

        updateDriverCount();
        updatePAOCount();
        updateUnitCount();
        updateScheduleCount();
        updateAssignedScheduleCount();
        boxDrivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboard.this, AdminManageDriver.class);
                startActivity(intent);
            }
        });
        boxPAOs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboard.this, AdminManagePAO.class);
                startActivity(intent);
            }
        });
        boxUnits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboard.this, AdminManageUnitScreen.class);
                startActivity(intent);
            }
        });
        boxSchedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboard.this, AdminManageScheduleScreen.class);
                startActivity(intent);
            }
        });
        boxAssignedSchedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboard.this, AdminManageActiveUnitList.class);
                startActivity(intent);
            }
        });
    }
    private void updateDriverCount() {
        // Reference to the TextView
        TextView driversCountTextView = findViewById(R.id.tvDriverCount);

        // Query to get all documents in the collection
        db.collection("drivers")
                .get()  // Get all documents in the collection
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the result of the query
                        QuerySnapshot querySnapshot = task.getResult();

                        // Get the count of documents
                        int driversCount = querySnapshot.size();

                        // Update the TextView with the count
                        driversCountTextView.setText(String.valueOf(driversCount));
                    } else {
                        // Handle error if the query fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }
    private void updatePAOCount() {
        TextView paoCountTextView = findViewById(R.id.tvPAOCount);

        db.collection("users")
                .whereEqualTo("role", "pao")  // Query for users where role = "pao"
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        int paoCount = querySnapshot.size();  // Get the count of users with "pao" role
                        paoCountTextView.setText(String.valueOf(paoCount));  // Set the count in TextView
                    } else {
                        Log.d("Firestore", "Error getting users documents: ", task.getException());
                    }
                });
    }
    private void updateUnitCount() {
        // Reference to the TextView
        TextView unitsCountTextView = findViewById(R.id.tvUnitCount);

        // Query to get all documents in the collection
        db.collection("units")
                .get()  // Get all documents in the collection
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the result of the query
                        QuerySnapshot querySnapshot = task.getResult();

                        // Get the count of documents
                        int unitsCount = querySnapshot.size();

                        // Update the TextView with the count
                        unitsCountTextView.setText(String.valueOf(unitsCount));
                    } else {
                        // Handle error if the query fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }
    private void updateScheduleCount() {
        // Reference to the TextView
        TextView schedulesCountTextView = findViewById(R.id.tvScheduleCount);

        // Query to get all documents in the collection
        db.collection("schedules")
                .get()  // Get all documents in the collection
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the result of the query
                        QuerySnapshot querySnapshot = task.getResult();

                        // Get the count of documents
                        int schedulesCount = querySnapshot.size();

                        // Update the TextView with the count
                        schedulesCountTextView.setText(String.valueOf(schedulesCount));
                    } else {
                        // Handle error if the query fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }
    private void updateAssignedScheduleCount() {
        // Reference to the TextView
        TextView assignedSchedulesCountTextView = findViewById(R.id.tvAssignedScheduleCount);

        // Query to get all documents in the collection
        db.collection("assigns")
                .get()  // Get all documents in the collection
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the result of the query
                        QuerySnapshot querySnapshot = task.getResult();

                        // Get the count of documents
                        int assignedSchedulesCount = querySnapshot.size();

                        // Update the TextView with the count
                        assignedSchedulesCountTextView.setText(String.valueOf(assignedSchedulesCount));
                    } else {
                        // Handle error if the query fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
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
