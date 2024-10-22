package com.example.wejeep;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class GoogleSignInHelper {

    private static final String TAG = "GoogleSignInHelper";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Activity activity;
    private static final int RC_SIGN_IN = 9001;

    public GoogleSignInHelper(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void signIn() {
        // Clear existing sign-in state to ensure account selection
        mGoogleSignInClient.signOut().addOnCompleteListener(activity, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }
    public void handleSignInResult(int requestCode, int resultCode, Intent data, final SignInCallback callback) {
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String idToken = account.getIdToken();
                    firebaseAuthWithGoogle(idToken, callback);
                } else {
                    callback.onSignInFailure(new Exception("GoogleSignInAccount is null"));
                }
            } catch (ApiException e) {
                callback.onSignInFailure(e);
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken, final SignInCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(activity, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Fetch user role and redirect accordingly
                    checkUserRoleAndRedirect(user, callback);
                } else {
                    callback.onSignInFailure(new Exception("FirebaseUser is null"));
                }
            } else {
                callback.onSignInFailure(task.getException());
            }
        });
    }

    private void checkUserRoleAndRedirect(FirebaseUser user, final SignInCallback callback) {
        String userId = user.getUid();

        // Fetch the user's role from Firestore
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Get the user's role from Firestore
                String role = task.getResult().getString("role");

                if ("admin".equals(role)) {
                    // If user is an admin, redirect to AdminDashboard
                    Intent adminIntent = new Intent(activity, AdminDashboard.class);
                    activity.startActivity(adminIntent);
                    activity.finish();
                } else {
                    // Default redirection to HSPassenger for other users
                    Intent passengerIntent = new Intent(activity, HSPassenger.class);
                    activity.startActivity(passengerIntent);
                    activity.finish();
                }

                callback.onSignInSuccess(user);
            } else {
                storeUserInFirestore(user);
                Intent intent = new Intent(activity, HSPassenger.class); // Default redirection after first sign-in
                activity.startActivity(intent);
            }
        });
    }

    private void storeUserInFirestore(FirebaseUser user) {
        String userId = user.getUid();
        String email = user.getEmail();
        String name = user.getDisplayName();
        String profilePicture = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "";

        // Check if user already exists in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    // User doesn't exist, create a new one with role "passenger"
                    String role = "passenger";
                    User userInfo = new User(email, name, profilePicture, role);
                    db.collection("users").document(userId).set(userInfo)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile successfully written!"))
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing user profile", e));
                } else {
                    // User already exists
                    Log.d(TAG, "User already exists, not overwriting.");
                }
            } else {
                Log.w(TAG, "Error checking if user exists", task.getException());
            }
        });
    }

    public interface SignInCallback {
        void onSignInSuccess(FirebaseUser user);
        void onSignInFailure(Exception e);
    }

    // User class to store user data
    public static class User {
        private String email;
        private String name;
        private String profilePicture;
        private String role;

        // No-argument constructor needed for Firestore serialization
        public User() {
        }

        public User(String email, String name, String profilePicture, String role) {
            this.email = email;
            this.name = name;
            this.profilePicture = profilePicture;
            this.role = role;
        }

        // Getters and Setters
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}