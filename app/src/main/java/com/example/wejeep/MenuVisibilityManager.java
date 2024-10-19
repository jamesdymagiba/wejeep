package com.example.wejeep;

import android.content.Context;
import android.util.Log;
import android.view.Menu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MenuVisibilityManager {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Context context; // Add context field

    // Constructor that takes Context as a parameter
    public MenuVisibilityManager(Context context) {
        this.context = context; // Initialize context
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    // Updated method to fetch user role
    public void fetchUserRole(Menu menu) {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userRole = documentSnapshot.getString("role");
                            setMenuVisibility(menu, userRole);
                            Log.d("MenuVisibilityManager", userRole + userId);
                            Log.d("MenuVisibilityManager", "Menu object: " + menu);
                        } else {
                            Log.w("MenuVisibilityManager", "No role found for user: " + userId);
                        }
                    })
                    .addOnFailureListener(e -> Log.w("MenuVisibilityManager", "Error fetching user role for user: " + userId, e));
        } else {
            Log.w("MenuVisibilityManager", "No user is currently signed in.");
        }
    }

    // Method to set menu visibility based on user role
    public static void setMenuVisibility(Menu menu, String userRole) {
        if (menu == null || userRole == null) {
            Log.w("MenuVisibilityManager", "Menu or user role is null. Cannot set visibility.");
            return;
        }

        // Hide all groups first
        menu.setGroupVisible(R.id.passenger, false);
        menu.setGroupVisible(R.id.pao, false);
        menu.setGroupVisible(R.id.admin, false);

        // Set visibility based on user role
        switch (userRole) {
            case "passenger":
                menu.setGroupVisible(R.id.passenger, true);
                // Set other passenger-specific menu items visible
                break;
            case "pao":
                menu.setGroupVisible(R.id.passenger, true);
                menu.setGroupVisible(R.id.pao, true);
                // Set other PAO-specific menu items visible
                break;
            case "admin":
                menu.setGroupVisible(R.id.admin, true);
                // Set other admin-specific menu items visible
                break;
            default:
                Log.w("MenuVisibilityManager", "Unknown user role: " + userRole);
                break;
        }
    }
}
