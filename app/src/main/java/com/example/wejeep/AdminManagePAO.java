package com.example.wejeep;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class AdminManagePAO extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerViewPAO;
    private PAOAdapter paoAdapter;
    private ArrayList<PAOModel> paoList;
    private Button btnAddPao;
    private NavigationView navigationView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_pao);

        Toolbar toolbar = findViewById(R.id.toolbarAdminManagesPao);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the views
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerViewPAO = findViewById(R.id.recyclerViewPAO);
        btnAddPao = findViewById(R.id.btnAddPao);
        navigationView = findViewById(R.id.nav_view);

        // Set up the navigation drawer toggle
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Initialize the PAO list and adapter
        paoList = new ArrayList<>();
        paoAdapter = new PAOAdapter(paoList, db);
        recyclerViewPAO.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPAO.setAdapter(paoAdapter);

        // Load PAOs from Firestore
        loadPAOsFromFirestore();

        // Set up NavigationView item selection listener (same structure as AdminManageDriver)
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.itmHomeHSP:
                    startActivity(new Intent(AdminManagePAO.this, HSPassenger.class));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                case R.id.itmSignoutHSP:
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(AdminManagePAO.this, MainActivity.class));
                    finish();
                    return true;
                case R.id.itmProfileHSP:
                    startActivity(new Intent(AdminManagePAO.this, PPassenger.class));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                case R.id.itmManageDriverHSP:
                    startActivity(new Intent(AdminManagePAO.this, AdminManageDriver.class));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                case R.id.itmManagePAOHSP:
                    startActivity(new Intent(AdminManagePAO.this, AdminManagePAO.class));
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                default:
                    return false;
            }
        });

        // Set up the Add PAO button to start SignUpForPAO activity
        btnAddPao.setOnClickListener(v -> {
            Intent intent = new Intent(AdminManagePAO.this, SignUpForPAO.class);
            startActivityForResult(intent, 1); // Start for result
        });

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
        }
    }

    private void loadPAOsFromFirestore() {
        db.collection("users")
                .whereEqualTo("role", "pao")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        paoList.clear(); // Clear the existing list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String documentId = document.getId(); // Get document ID
                            if (name != null && email != null) { // Check for null values
                                paoList.add(new PAOModel(name, email, documentId)); // Include document ID
                            }
                        }
                        paoAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                    } else {
                        Toast.makeText(AdminManagePAO.this, "Error loading PAOs: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadPAOsFromFirestore(); // Refresh the list after new PAO sign up
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