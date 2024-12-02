package com.dygroup.wejeep;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

public class BackPressHandler {

    public static void handleBackPress(final Context context) {
        // Create the confirmation dialog
        new AlertDialog.Builder(context)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false) // Prevent dismissing the dialog by tapping outside
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If user clicks "Yes", finish the activity or exit the app
                        if (context instanceof android.app.Activity) {
                            ((android.app.Activity) context).finish(); // Close the current activity
                        } else {
                            // Optionally exit the app completely if it's not an activity
                            System.exit(0);
                        }
                    }
                })
                .setNegativeButton("No", null) // If "No", dismiss the dialog and stay on the current screen
                .show();
    }
}

