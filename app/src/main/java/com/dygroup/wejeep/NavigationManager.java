package com.dygroup.wejeep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;

import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.views.MapView;


public class NavigationManager {

    private Context context;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private MapView mapView;

    public NavigationManager(Context context) {
        this.context = context;
    }
    public NavigationManager(Context context, FusedLocationProviderClient fusedLocationClient, LocationCallback locationCallback, MapView mapView) {
        this.context = context;
        this.fusedLocationClient = fusedLocationClient;
        this.locationCallback = locationCallback;
        this.mapView = mapView;
    }

    public boolean handleNavigationItemSelected(@NonNull MenuItem item, Activity currentActivity) {
        int id = item.getItemId();
        switch (id) {
            case R.id.itmHomeHSP:
                showToast("Home");
                finishAndStartActivity(currentActivity, HSPassenger.class);
                return true;
            case R.id.itmSignoutHSP:
                showToast("Signout");
                removeUserLocationFromFirestore();
                Log.d("NavigationManager", "Signout clicked, calling stopLocationUpdates()");
                stopLocationUpdates();
                clearMap();
                FirebaseAuth.getInstance().signOut();
                finishAndStartActivity(currentActivity, MainActivity.class);
                return true;
            case R.id.itmProfileHSP:
                showToast("Profile");
                finishAndStartActivity(currentActivity, PPassenger.class);
                return true;
            case R.id.itmActiveModernJeepHSP:
                showToast("Active Modern Jeep/s");
                finishAndStartActivity(currentActivity, ActiveModernJeeps.class);
                return true;
            case R.id.itmAdminDashboardHSP:
                showToast("Admin Dashboard");
                finishAndStartActivity(currentActivity, AdminDashboard.class);
                return true;
            case R.id.itmManageDriverHSP:
                showToast("Manage Driver");
                finishAndStartActivity(currentActivity, AdminManageDriver.class);
                return true;
            case R.id.itmManagePAOHSP:
                showToast("Manage PAO");
                finishAndStartActivity(currentActivity, AdminManagePAO.class);
                return true;
            case R.id.itmManageUnitHSP:
                showToast("Manage Unit");
                finishAndStartActivity(currentActivity, AdminManageUnitScreen.class);
                return true;
            case R.id.itmManageScheduleHSP:
                showToast("Manage Schedule");
                finishAndStartActivity(currentActivity, AdminManageScheduleScreen.class);
                return true;
            case R.id.itmAssignScheduleHSP:
                showToast("Assign Schedule");
                finishAndStartActivity(currentActivity, AdminManageActiveUnitList.class);
                return true;
            case R.id.itmScheduleHSP:
                showToast("Schedule");
                finishAndStartActivity(currentActivity, Schedule.class);
                return true;
            default:
                return false;
        }
    }

    private void removeUserLocationFromFirestore() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = user.getUid();
            db.collection("locations").document(userId).delete()

                    .addOnSuccessListener(aVoid -> Log.d("SUCCESS", "Location removed on sign out"))
                    .addOnFailureListener(e -> Log.w("ERROR", "Error removing location on sign out", e));
        }
    }
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            Log.d("LocationUpdates", "Attempting to stop location updates...");
            fusedLocationClient.removeLocationUpdates(locationCallback)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("LocationUpdates", "Location updates stopped successfully.");
                        } else {
                            Log.e("LocationUpdates", "Failed to stop location updates.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LocationUpdates", "Error stopping location updates: ", e);
                    });
        } else {
            Log.e("LocationUpdates", "FusedLocationClient or LocationCallback is null.");
        }
    }

    private void clearMap() {
        if (mapView != null) {
            mapView.getOverlays().clear();
            mapView.invalidate();
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void finishAndStartActivity(Activity currentActivity, Class<?> targetActivity) {
        // Finish the current activity
        currentActivity.finish();
        // Start the new activity
        Intent intent = new Intent(context, targetActivity);
        context.startActivity(intent);
    }
}
