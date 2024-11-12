package com.example.wejeep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set up DrawerToggle for Navigation Drawer
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set up NavigationManager and MenuVisibilityManager
        navigationManager = new NavigationManager(this);
        menuVisibilityManager = new MenuVisibilityManager(this);
        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = navigationManager.handleNavigationItemSelected(item);
            drawerLayout.closeDrawer(GravityCompat.START);
            return handled;
        });

        // Check user authentication and set the profile info
        checkUserAuthentication();

        // Retrieve schedule data from Firestore using the authenticated user's name
        fetchScheduleData();
    }

    private void fetchScheduleData() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String conductorName = user.getDisplayName(); // Get the current user's display name

            // Query Firestore collection with a filter by conductor name
            db.collection("assigns")
                    .whereEqualTo("conductor", conductorName)  // Query by the conductor field
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Assuming only one document is returned, get the first document
                            com.google.firebase.firestore.DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

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
                        } else {
                            // Handle case where no schedule is found
                            Toast.makeText(Schedule.this, "No schedule found for this user.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Log the error and show a message
                        Log.e(TAG, "Error fetching schedule data", e);
                        Toast.makeText(Schedule.this, "Error loading schedule. Please try again.", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            // Handle case where user is not authenticated
            Toast.makeText(Schedule.this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void checkUserAuthentication() {
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
            // Fetch the user's role to update menu visibility
            menuVisibilityManager.fetchUserRole(navigationView.getMenu());
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
