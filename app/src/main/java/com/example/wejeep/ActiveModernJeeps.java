package com.example.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ActiveModernJeeps extends AppCompatActivity {
    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
    private FirebaseAuth auth;
    private FirebaseUser user;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    private RecyclerView recyclerView;
    private ActiveModernJeepAdapter adapter;
    private List<ActiveModernJeepModel> activeModernJeepList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_modern_jeeps);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbarAMJ);

        // Initialize navigationManager and navigationView for menuVisibilityManager
        navigationManager = new NavigationManager(this);
        navigationView = findViewById(R.id.nav_view);

        // Initialize MenuVisibilityManager with the NavigationView
        Menu menu = navigationView.getMenu();
        menuVisibilityManager = new MenuVisibilityManager(this);
        menuVisibilityManager.fetchUserRole(menu);

        //Initialize Drawer for menu
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        Drawable drawable = drawerToggle.getDrawerArrowDrawable();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean handled = navigationManager.handleNavigationItemSelected(item, ActiveModernJeeps.this);
                drawerLayout.closeDrawer(GravityCompat.START);
                return handled;
            }
        });
        //Add profile picture and name from firestore in header
        UserProfileManager.checkAuthAndUpdateUI(FirebaseAuth.getInstance(), navigationView, this);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.item_activeModernJeep);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the list
        activeModernJeepList = new ArrayList<>();

        // Set up the adapter
        adapter = new ActiveModernJeepAdapter(activeModernJeepList);
        recyclerView.setAdapter(adapter);

        // Fetch data from Firestore
        fetchActiveModernJeepData();
    }

    private void fetchActiveModernJeepData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch users with role 'pao' and 'locationindication' set to "on"
        db.collection("users")
                .whereEqualTo("role", "pao")
                .whereEqualTo("locationindicator", "on")
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        List<String> paoEmails = new ArrayList<>();

                        // Extract email addresses of matching users
                        for (QueryDocumentSnapshot userDoc : userTask.getResult()) {
                            String email = userDoc.getString("email");
                            if (email != null) {
                                paoEmails.add(email);
                            }
                        }

                        // Check if emails are in the assigns collection
                        db.collection("assigns").get()
                                .addOnCompleteListener(assignTask -> {
                                    if (assignTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot assignDoc : assignTask.getResult()) {
                                            String email = assignDoc.getString("email");

                                            if (email != null && paoEmails.contains(email)) {
                                                String unitNumber = assignDoc.getString("unitnumber");
                                                String vehicleModel = assignDoc.getString("vehiclemodel");
                                                String driverName = assignDoc.getString("driver");
                                                String paoName = assignDoc.getString("conductor");
                                                String plateNumber = assignDoc.getString("platenumber");
                                                String documentId = assignDoc.getId();

                                                // Create the model object
                                                ActiveModernJeepModel jeepModel = new ActiveModernJeepModel(
                                                        unitNumber, vehicleModel, driverName, paoName, plateNumber, documentId
                                                );

                                                // Add to the list
                                                activeModernJeepList.add(jeepModel);
                                            }
                                        }
                                        // Notify the adapter that data has changed
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        // Handle the error
                                        Toast.makeText(ActiveModernJeeps.this, "Error getting assigns data.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Handle the error
                        Toast.makeText(ActiveModernJeeps.this, "Error getting user data.", Toast.LENGTH_SHORT).show();
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