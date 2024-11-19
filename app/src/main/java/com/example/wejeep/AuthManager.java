package com.example.wejeep;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

    private long lastSignUpTime = 0;
    private static final long DEBOUNCE_DELAY = 2000; // 2 seconds debounce delay

    public void signUpUser(String email, String password, String name, final CustomLoadingDialog customLoadingDialog) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSignUpTime < DEBOUNCE_DELAY) {
            Toast.makeText(context, "Please wait before signing up again.", Toast.LENGTH_SHORT).show();
            customLoadingDialog.hideLoadingScreen();
            return; // Exit if called within the debounce delay
        }
        lastSignUpTime = currentTime; // Update last sign-up time

        checkIfUserExists(email, new UserCheckCallback() {
            @Override
            public void onUserCheck(boolean exists) {
                if (exists) {
                    customLoadingDialog.hideLoadingScreen();
                    Toast.makeText(context, "User already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    // Proceed with user sign up if the user does not exist
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    customLoadingDialog.hideLoadingScreen();
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            sendVerificationEmail(user, name);
                                        }
                                    } else {
                                        Toast.makeText(context, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }


    public void signInUser(String email, String password, final CustomLoadingDialog customLoadingDialog) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        customLoadingDialog.hideLoadingScreen();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference userRef = db.collection("users").document(user.getUid());
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                String role = document.getString("role");
                                                if (role != null && role.equals("admin")) {
                                                    // If the user is an admin, redirect to AdminDashboard
                                                    Toast.makeText(context, "Login successful as an admin.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(context, AdminDashboard.class);
                                                    context.startActivity(intent);
                                                } else {
                                                    // If the user is not an admin, redirect to HSPassenger
                                                    Toast.makeText(context, "Login successful.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(context, HSPassenger.class);
                                                    context.startActivity(intent);
                                                }
                                                if (context instanceof AppCompatActivity) {
                                                    ((AppCompatActivity) context).finish();
                                                }
                                            } else {
                                                Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(context, "Failed to fetch user role.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else if (user != null && !user.isEmailVerified()) {
                                Toast.makeText(context, "Please verify your email address.", Toast.LENGTH_SHORT).show();
                                promptForEmailVerification(user);
                            } else {
                                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void promptForEmailVerification(FirebaseUser user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your email is not verified. Would you like to receive a verification email?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If the user agrees, send a verification email
                        sendVerificationEmailOnly(user);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User declined the email
                        Toast.makeText(context, "You can verify your email later.", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }
    //sending of verification email and creating of account
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
    //sending of verification email only
    private void sendVerificationEmailOnly(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("AuthManager", "Verification email sent.");
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
                            String profilePicture = "";
                            UserProfile userProfile = new UserProfile(name, email, role, profilePicture);
                            db.collection("users").document(user.getUid())
                                    .set(userProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("AuthManager", "User profile added.");
                                        Toast.makeText(context, "Sign up Successfully. Please check your email for verification.", Toast.LENGTH_SHORT).show();
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
        private String profilePicture;

        public UserProfile() {
            // Default constructor required for Firestore serialization
        }

        public UserProfile(String name, String email, String role, String profilePicture) {
            this.name = name;
            this.email = email;
            this.role = role;
            this.profilePicture = profilePicture;
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

        public String getProfilePicture() {return profilePicture;}

        public void setProfilePicture(String profilePicture) {this.profilePicture = profilePicture;}
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