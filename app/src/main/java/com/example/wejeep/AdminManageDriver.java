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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminManageDriver extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    Button btnAddDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_driver);

        Toolbar toolbar = findViewById(R.id.toolbarAdminManageDriver);
        setSupportActionBar(toolbar);

        btnAddDriver = findViewById(R.id.btnAddDriver);
        btnAddDriver.setOnClickListener(view -> {
            Intent intent = new Intent(AdminManageDriver.this, AdminAddDriver.class);
            startActivity(intent);
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

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
            ImageView ivProfilePicturePP = findViewById(R.id.ivProfilePicturePP);

            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivProfilePicturePP);

            TextView tvNameHSP = headerView.findViewById(R.id.tvNameHSP);
            TextView tvNamePP = findViewById(R.id.tvNamePP);

            tvNamePP.setText(user.getDisplayName());
            tvNameHSP.setText(user.getDisplayName());

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivProfilePictureHSP);
            }
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
