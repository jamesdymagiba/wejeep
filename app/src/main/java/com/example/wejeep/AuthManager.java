package com.example.wejeep;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthManager {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Context context;

    public AuthManager(Context context) {
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    public void signUpUser(String email, String password, String name, final ProgressBarHandler progressBarHandler) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarHandler.hideProgressBar();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                sendVerificationEmail(user, name);
                            }
                        } else {
                            Toast.makeText(context, "Sign up failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signInUser(String email, String password, final ProgressBarHandler progressBarHandler) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarHandler.hideProgressBar();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                Toast.makeText(context, "Login successful.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, HSPassenger.class);
                                context.startActivity(intent);
                                if (context instanceof AppCompatActivity) {
                                    ((AppCompatActivity) context).finish();
                                }
                            } else if (user != null) {
                                Toast.makeText(context, "Please verify your email address.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user, String name) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("AuthManager", "Verification email sent.");
                            updateUserName(user, name);
                        } else {
                            Log.w("AuthManager", "Error sending verification email.", task.getException());
                            Toast.makeText(context, "Error sending verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserName(FirebaseUser user, String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("AuthManager", "User profile updated.");
                            addUserProfile(user, name, user.getEmail(), "passenger");
                        } else {
                            Toast.makeText(context, "Profile update failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserProfile(FirebaseUser user, String name, String email, String role) {
        // Check if the user already exists
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            // User doesn't exist, create a new profile
                            UserProfile userProfile = new UserProfile(name, email, role);
                            db.collection("users").document(user.getUid())
                                    .set(userProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("AuthManager", "User profile added.");
                                        Toast.makeText(context, "Please check your email for verification.", Toast.LENGTH_SHORT).show();
                                        // Start login activity after user profile is added
                                        Intent intent = new Intent(context, Login.class);
                                        context.startActivity(intent);
                                        if (context instanceof AppCompatActivity) {
                                            ((AppCompatActivity) context).finish();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("AuthManager", "Error adding user profile.", e);
                                        Toast.makeText(context,  "Error adding user profile.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // User already exists
                            Log.d("AuthManager", "User already exists, not adding.");
                        }
                    } else {
                        Log.w("AuthManager", "Error checking if user exists", task.getException());
                    }
                });
    }


    public boolean isUserSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public static class UserProfile {
        private String name;
        private String email;
        private String role;

        public UserProfile() {
            // Default constructor required for Firestore serialization
        }

        public UserProfile(String name, String email, String role) {
            this.name = name;
            this.email = email;
            this.role = role;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
    public void checkIfUserExists(String email, final UserCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            callback.onUserCheck(true);
                        } else {
                            callback.onUserCheck(false);
                        }
                    } else {
                        callback.onUserCheck(false);
                    }
                });
    }

    public interface UserCheckCallback {
        void onUserCheck(boolean exists);
    }

}
