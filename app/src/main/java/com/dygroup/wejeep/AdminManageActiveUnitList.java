package com.dygroup.wejeep;

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
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class AdminManageActiveUnitList extends AppCompatActivity {
    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    private RecyclerView recyclerView;
    private ArrayList<AssignModel> assignList;
    private AssignAdapter assignAdapter;
    Button btnAssignUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_active_unit_list);

        Toolbar toolbar = findViewById(R.id.toolbarAdminManageActiveUnitList);



        btnAssignUnit = findViewById(R.id.btnAssignUnit);
        btnAssignUnit.setOnClickListener(view -> {
            Intent intent = new Intent(AdminManageActiveUnitList.this, AdminAssignUnitScreen.class);
            startActivity(intent);
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.item_assign);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assignList = new ArrayList<>();
        assignAdapter = new AssignAdapter(assignList);
        recyclerView.setAdapter(assignAdapter);

        // Initialize navigationManager and navigationView for menuVisibilityManager
        //jamesdyandwelberinbranch
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


        fetchDriverFromFirestore();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean handled = navigationManager.handleNavigationItemSelected(item, AdminManageActiveUnitList.this);
                drawerLayout.closeDrawer(GravityCompat.START);
                return handled;
            }
        });
        //Add profile picture and name from firestore in header
        UserProfileManager.checkAuthAndUpdateUI(FirebaseAuth.getInstance(), navigationView, this);
    }
    // Method to fetch driver data from Firestore
    private void fetchDriverFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("assigns").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    AssignModel assign = document.toObject(AssignModel.class);
                    assign.setDocumentId(document.getId()); // Set the document ID
                    assignList.add(assign);
                    Log.d("adminassign","assignedList,"+assignList);
                }
                assignAdapter.notifyDataSetChanged(); // Update the RecyclerView with data
            } else {
                Toast.makeText(AdminManageActiveUnitList.this, "Error getting schedules.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override //override code to notify succesful editing of driver and refresh the list
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh the driver list
            assignList.clear(); // Clear the current list
            fetchDriverFromFirestore(); // Fetch updated data
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
