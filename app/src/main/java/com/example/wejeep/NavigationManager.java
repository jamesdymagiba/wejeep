package com.example.wejeep;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NavigationManager {

    private Context context;
    private FirebaseUser user;
    private FirebaseAuth auth;
    public NavigationManager(Context context) {
        this.context = context;
    }

    public boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itmHomeHSP:
                showToast("Home");
                startActivity(HSPassenger.class);
                return true;
            case R.id.itmSignoutHSP:
                showToast("Signout");
                removeUserLocationFromFirestore();
                FirebaseAuth.getInstance().signOut();
                startActivity(MainActivity.class);
                return true;
            case R.id.itmProfileHSP:
                showToast("Profile");
                startActivity(PPassenger.class);
                return true;
            case R.id.itmAdminDashboardHSP:
                showToast("Admin Dashboard");
                startActivity(AdminDashboard.class);
                return true;
            case R.id.itmManageDriverHSP:
                showToast("Manage Driver");
                startActivity(AdminManageDriver.class);
                return true;
            case R.id.itmManageUnitHSP:
                showToast("Manage Unit");
                startActivity(AdminManageUnitScreen.class);
                return true;
            case R.id.itmManageScheduleHSP:
                showToast("Manage Schedule");
                startActivity(AdminManageScheduleScreen.class);
                return true;
            case R.id.itmAssignScheduleHSP:
                showToast("Assign Schedule");
                startActivity(AdminManageActiveUnitList.class);
                return true;
            default:
                return false;
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
    }

    private void removeUserLocationFromFirestore() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        db.collection("locations").document(userId).delete()
                .addOnSuccessListener(aVoid -> Log.d("SUCESS", "Location removed on sign out"))
                .addOnFailureListener(e -> Log.w("ERROR", "Error removing location on sign out", e));
    }
}
