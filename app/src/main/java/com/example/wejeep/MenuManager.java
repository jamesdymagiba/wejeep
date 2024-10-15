package com.example.wejeep;

import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import android.view.Menu;
public class MenuManager {
    public static void setMenuVisibility(NavigationView navigationView, boolean userRole) {
        Menu menu = navigationView.getMenu();

        // Hide all groups first
        menu.setGroupVisible(R.id.passenger, false);
        menu.setGroupVisible(R.id.pao, false);
        menu.setGroupVisible(R.id.admin, false);

        // Show the relevant group based on the user's role
        if ("passenger".equals(userRole)) {
            menu.setGroupVisible(R.id.passenger, true);
        } else if ("pao".equals(userRole)) {
            menu.setGroupVisible(R.id.passenger, true);
            menu.setGroupVisible(R.id.pao, true);
        } else if ("admin".equals(userRole)) {
            menu.setGroupVisible(R.id.admin, true);
        }
    }
}
