package com.dygroup.wejeep;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileManager {
    private static final String TAG = "UserProfileManager";

    // Single method to check auth and update user UI
    public static void checkAuthAndUpdateUI(FirebaseAuth auth, View navigationView, Context context) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // User is not logged in, redirect to Login activity
            Intent intent = new Intent(context, Login.class);
            context.startActivity(intent);
            // If in an activity, you may want to call finish() to close current activity
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        } else {
            // User is logged in, fetch user information from Firestore
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String name = task.getResult().getString("name");
                    String profilePicture = task.getResult().getString("profilePicture");

                    ImageView ivProfilePictureHSP = navigationView.findViewById(R.id.ivProfilePictureHSP);
                    TextView tvNameHSP = navigationView.findViewById(R.id.tvNameHSP);

                    // Null check for tvNameHSP before setting the text
                    if (tvNameHSP != null && name != null) {
                        tvNameHSP.setText(name);
                    }

                    // Load the profile picture from Firestore or use a placeholder if it doesn't exist
                    if (profilePicture != null && !profilePicture.isEmpty()) {
                        Glide.with(context)
                                .load(profilePicture)
                                .apply(RequestOptions.circleCropTransform())
                                .into(ivProfilePictureHSP);
                    } else {
                        // Optionally set a placeholder image if no profile picture exists
                        ivProfilePictureHSP.setImageResource(R.drawable.placeholder_image);
                    }
                } else {
                    Toast.makeText(context, "Error fetching user information", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Error fetching user information from Firestore", task.getException());
                }
            });
        }
    }
}
