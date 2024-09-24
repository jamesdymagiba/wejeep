package com.example.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class AdminManageDriver extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private ArrayList<DriverModel> driverList;
    private DriverAdapter driverAdapter;
    Button btnAddDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_driver);

        Toolbar toolbar = findViewById(R.id.toolbarAdminManageDriver);

        btnAddDriver = findViewById(R.id.btnAddDriver);
        btnAddDriver.setOnClickListener(view -> {
            Intent intent = new Intent(AdminManageDriver.this, AdminAddDriver.class);
            startActivity(intent);
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.item_driver);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        driverList = new ArrayList<>();
        driverAdapter = new DriverAdapter(driverList);
        recyclerView.setAdapter(driverAdapter);

        //Initialize Drawer for menu
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Fetch data from Firestore and populate the RecyclerView
        fetchDriversFromFirestore();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.itmHomeHSP:
                        Toast.makeText(AdminManageDriver.this, "Home", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminManageDriver.this, HSPassenger.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.itmSignoutHSP:
                        Toast.makeText(AdminManageDriver.this, "Signout", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(AdminManageDriver.this, MainActivity.class));
                        finish();
                        return true;
                    case R.id.itmProfileHSP:
                        Toast.makeText(AdminManageDriver.this, "Profile", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminManageDriver.this, PPassenger.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.itmManageDriverHSP:
                        Toast.makeText(AdminManageDriver.this, "Manage Driver", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminManageDriver.this, AdminManageDriver.class));
                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    default:
                        return false;
                }
            }
        });
        FirebaseAuth auth = FirebaseAuth.getInstance();
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
        }
    }
    // Method to fetch driver data from Firestore
    private void fetchDriversFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("drivers").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    DriverModel driver = document.toObject(DriverModel.class);
                    driver.setDocumentId(document.getId()); // Set the document ID
                    driverList.add(driver);
                }
                driverAdapter.notifyDataSetChanged(); // Update the RecyclerView with data
            } else {
                Toast.makeText(AdminManageDriver.this, "Error getting drivers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override //override code to notify succesful editing of driver and refresh the list
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh the driver list
            driverList.clear(); // Clear the current list
            fetchDriversFromFirestore(); // Fetch updated data
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
