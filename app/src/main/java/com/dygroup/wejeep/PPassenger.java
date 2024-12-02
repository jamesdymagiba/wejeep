package com.dygroup.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PPassenger extends AppCompatActivity {
    private NavigationManager navigationManager;
    private MenuVisibilityManager menuVisibilityManager;
    private FirebaseAuth auth;
    private FirebaseUser user;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    Button btnEditProfilePP, btnDeleteAccountPP;
    private static final String TAG = "PPassenger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppassenger);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbarPP);
        btnEditProfilePP = findViewById(R.id.btnEditProfilePP);
        btnDeleteAccountPP = findViewById(R.id.btnDeleteAccountPP);

        btnDeleteAccountPP.setOnClickListener(view -> {
            // Show confirmation dialog
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Get current user ID (UID)
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            // Delete user document from Firestore
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        // Check if the user has a profile picture
                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                        StorageReference profilePicRef = storage.getReference().child("profile_pictures/" + userId + ".jpg");

                                        // Try to check if the profile picture exists before attempting to delete
                                        profilePicRef.getMetadata()
                                                .addOnSuccessListener(metadata -> {
                                                    // If metadata is found, delete the profile picture
                                                    profilePicRef.delete()
                                                            .addOnSuccessListener(aVoid1 -> {
                                                                // Delete the account from Firebase Authentication
                                                                user.delete()
                                                                        .addOnSuccessListener(aVoid2 -> {
                                                                            Toast.makeText(view.getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                                                            finish();
                                                                            startActivity(new Intent(PPassenger.this, Login.class));
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            Toast.makeText(view.getContext(), "Failed to delete account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                                        });
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(view.getContext(), "Failed to delete profile picture: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    // If no profile picture is found, just proceed with deleting the account
                                                    user.delete()
                                                            .addOnSuccessListener(aVoid2 -> {
                                                                Toast.makeText(view.getContext(), "Account deleted successfully.", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                                startActivity(new Intent(PPassenger.this, Login.class));
                                                            })
                                                            .addOnFailureListener(e1 -> {
                                                                Toast.makeText(view.getContext(), "Failed to delete account: " + e1.getMessage(), Toast.LENGTH_LONG).show();
                                                            });
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(view.getContext(), "Failed to delete Firestore document: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    })
                    .setNegativeButton("No", null) // Do nothing if No is clicked
                    .show();
        });

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
                boolean handled = navigationManager.handleNavigationItemSelected(item, PPassenger.this);
                drawerLayout.closeDrawer(GravityCompat.START);
                return handled;
            }
        });

        //Add profile picture and name from firestore in header
        UserProfileManager.checkAuthAndUpdateUI(FirebaseAuth.getInstance(), navigationView, this);

        updateUserProfileUI();
    }

    private void updateUserProfileUI() {
        if (user != null) {
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
                        ivProfilePicturePP.setImageResource(R.drawable.placeholder_image);
                    }
                } else {
                    Toast.makeText(this, "Error fetching user information", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error fetching user information from Firestore", task.getException());
                }
            });
        } else {
            // Handle the case when the user is not logged in
            Toast.makeText(this, "User is not logged in. Redirecting to login screen.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PPassenger.this, Login.class));
            finish(); // Finish the current activity to prevent further access
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
