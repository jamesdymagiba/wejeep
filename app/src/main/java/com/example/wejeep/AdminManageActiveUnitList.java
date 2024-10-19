package com.example.wejeep;

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

public class AdminManageActiveUnitList extends AppCompatActivity {
    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
    private DrawerLayout drawerLayout;
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
                boolean handled = navigationManager.handleNavigationItemSelected(item);
                drawerLayout.closeDrawer(GravityCompat.START);
                return handled;
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
    private void fetchScheduleFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("schedules").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    AssignModel assign = document.toObject(AssignModel.class);
                    assign.setDocumentId(document.getId()); // Set the document ID
                    assignList.add(assign);
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
            fetchScheduleFromFirestore(); // Fetch updated data
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
