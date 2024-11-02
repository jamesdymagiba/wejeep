package com.example.wejeep;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class PPassenger extends AppCompatActivity {
    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
    private FirebaseAuth auth;
    private FirebaseUser user;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    Button btnEditProfilePP;
    private static final String TAG = "PPassenger";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppassenger);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbarPP);
        btnEditProfilePP = findViewById(R.id.btnEditProfilePP);

        btnEditProfilePP.setOnClickListener(view -> {
            startActivity(new Intent(PPassenger.this, EPPassenger.class));
        });

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
                boolean handled = navigationManager.handleNavigationItemSelected(item);
                drawerLayout.closeDrawer(GravityCompat.START);
                return handled;
            }
        });

        //Add profile picture and name from firestore in header
        UserProfileManager.checkAuthAndUpdateUI(FirebaseAuth.getInstance(), navigationView, this);

        updateUserProfileUI();
    }
    private void updateUserProfileUI() {
        String userId = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String name = task.getResult().getString("name");
                String profilePicture = task.getResult().getString("profilePicture");

                ImageView ivProfilePicturePP = findViewById(R.id.ivProfilePicturePP);
                TextView tvNamePP = findViewById(R.id.tvNamePP);

                // Set the name from Firestore
                tvNamePP.setText(name);

                // Load the profile picture from Firestore or use a placeholder if it doesn't exist
                if (profilePicture != null && !profilePicture.isEmpty()) {
                    Glide.with(this)
                            .load(profilePicture)
                            .apply(RequestOptions.circleCropTransform())
                            .into(ivProfilePicturePP);
                } else {
                    // Optionally set a placeholder image if no profile picture exists
                    ivProfilePicturePP.setImageResource(R.drawable.placeholder_image);
                }
            } else {
                Toast.makeText(this, "Error fetching user information", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Error fetching user information from Firestore", task.getException());
            }
        });
    }
}
