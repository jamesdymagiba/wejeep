
package com.example.wejeep;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminManagePAO extends AppCompatActivity {

    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerViewPAO;
    private PAOAdapter paoAdapter;
    private ArrayList<PAOModel> paoList;
    private Button btnAddPao;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_pao);

        Toolbar toolbar = findViewById(R.id.toolbarAdminManagePao);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the views
        btnAddPao = findViewById(R.id.btnAddPao);
        btnAddPao.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManagePAO.this, SignUpForPAO.class);
            startActivityForResult(intent, 1);  // Start for result to refresh PAO list after adding
        });

        // Setup RecyclerView
        setupRecyclerView();

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
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = navigationManager.handleNavigationItemSelected(item, AdminManagePAO.this);
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer after selection
            return handled;
        });

        //Add profile picture and name from firestore in header
        UserProfileManager.checkAuthAndUpdateUI(FirebaseAuth.getInstance(), navigationView, this);

        // Load PAOs from Firestore
        loadPAOsFromFirestore();
    }

    private void setupRecyclerView() {
        recyclerViewPAO = findViewById(R.id.recyclerViewPAO);
        recyclerViewPAO.setLayoutManager(new LinearLayoutManager(this));
        paoList = new ArrayList<>();
        paoAdapter = new PAOAdapter(paoList, db, this);
        recyclerViewPAO.setAdapter(paoAdapter);
    }

    private void checkUserAuthentication(NavigationView navigationView) {
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

            tvNameHSP.setText(user.getDisplayName() != null ? user.getDisplayName() : "User"); // Handle null display name
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivProfilePictureHSP);
            }
        }

    }


    private void loadPAOsFromFirestore() {
        db.collection("users")
                .whereEqualTo("role", "pao")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        paoList.clear();  // Clear the existing list before adding new ones
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String documentId = document.getId();  // Get document ID

                            String dateAdded = document.getString("dateAdded");
                            if (name != null && email != null && dateAdded != null) {  // Check for null values
                                paoList.add(new PAOModel(name, email, documentId, dateAdded));  // Include date added

                            String dateadded = document.getString("dateadded"); // Retrieve date added
                            if (name != null && email != null && dateadded != null) {  // Check for null values
                                paoList.add(new PAOModel(name, email, documentId, dateadded));  // Include date added
                            }
                        }
                        paoAdapter.notifyDataSetChanged();  // Notify adapter of data changes
                    } else {
                        Toast.makeText(AdminManagePAO.this, "Error loading PAOs: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadPAOsFromFirestore();  // Refresh the list after new PAO sign up
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
