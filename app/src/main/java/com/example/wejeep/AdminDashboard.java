package com.example.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboard extends AppCompatActivity {
    private static final String TAG = "AdminDashboard";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarAdminDashboard);
        db = FirebaseFirestore.getInstance();
        // Initialize the views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Set up the navigation drawer toggle
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Check if user is logged in
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            // User is not logged in, redirect to Login activity
            startActivity(new Intent(getApplicationContext(), Login.class));
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

            // Fetch user role to set menu visibility
            fetchUserRole();
        }
        // Set up NavigationView item selection listener
        navigationView.setNavigationItemSelectedListener(this::handleNavigationItemSelected);
    }

    private boolean handleNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itmHomeHSP:
                startActivity(new Intent(AdminDashboard.this, HSPassenger.class));
                break;
            case R.id.itmSignoutHSP:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AdminDashboard.this, MainActivity.class));
                finish();
                break;
            case R.id.itmProfileHSP:
                startActivity(new Intent(AdminDashboard.this, PPassenger.class));
                break;
            case R.id.itmManageDriverHSP:
                startActivity(new Intent(AdminDashboard.this, AdminManageDriver.class));
                break;
            case R.id.itmManagePAOHSP:
                // Do nothing, already in this activity
                break;
            default:
                return false;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fetchUserRole() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
