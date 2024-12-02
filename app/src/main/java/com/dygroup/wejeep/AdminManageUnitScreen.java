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
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class AdminManageUnitScreen extends AppCompatActivity {
    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private ArrayList<UnitModel> unitList;
    private UnitAdapter unitAdapter;
    Button btnAddUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_unit_screen);

        Toolbar toolbar = findViewById(R.id.toolbarAdminManageUnitScreen);

        btnAddUnit = findViewById(R.id.btnAddUnit);
        btnAddUnit.setOnClickListener(view -> {
            Intent intent = new Intent(AdminManageUnitScreen.this, AdminAddUnitScreen.class);
            startActivity(intent);
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.item_unit);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        unitList = new ArrayList<>();
        unitAdapter = new UnitAdapter(unitList);
        recyclerView.setAdapter(unitAdapter);

        // Fetch data from Firestore and populate the RecyclerView
        fetchUnitsFromFirestore();

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
                boolean handled = navigationManager.handleNavigationItemSelected(item, AdminManageUnitScreen.this);
                drawerLayout.closeDrawer(GravityCompat.START);
                return handled;
            }
        });

        //Add profile picture and name from firestore in header
        UserProfileManager.checkAuthAndUpdateUI(FirebaseAuth.getInstance(), navigationView, this);
    }
    // Method to fetch driver data from Firestore
    private void fetchUnitsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("units").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    UnitModel unit = document.toObject(UnitModel.class);
                    unit.setDocumentId(document.getId()); // Set the document ID
                    unitList.add(unit);
                }
                unitAdapter.notifyDataSetChanged(); // Update the RecyclerView with data
            } else {
                Toast.makeText(AdminManageUnitScreen.this, "Error getting units.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override //override code to notify succesful editing of driver and refresh the list
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh the driver list
            unitList.clear(); // Clear the current list
            fetchUnitsFromFirestore(); // Fetch updated data
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
