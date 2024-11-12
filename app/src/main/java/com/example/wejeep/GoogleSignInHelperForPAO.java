package com.example.wejeep;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class GoogleSignInHelperForPAO {

    private static final String TAG = "GoogleSignInHelperForPAO";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;
    private Activity activity;
    private static final int RC_SIGN_IN = 9001;

    public GoogleSignInHelperForPAO(Activity activity) {
        this.activity = activity;
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id)) // Your web client ID here
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
                    storeUserInFirestore(account, idToken, callback);
                } else {
                    callback.onSignInFailure(new Exception("GoogleSignInAccount is null"));
                }
            } catch (ApiException e) {
                callback.onSignInFailure(e);
            }
        }
    }

    private void storeUserInFirestore(GoogleSignInAccount account, String idToken, final SignInCallback callback) {
        // Get the user's email, name, and profile picture URL
        String email = account.getEmail();
        String name = account.getDisplayName();
        Uri photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl() : Uri.EMPTY;

        // Create a User object with the data from GoogleSignInAccount
        User userInfo = new User(email, name, photoUrl.toString(), "pao");

        // Check if user already exists in Firestore
        db.collection("users").document(idToken).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                db.collection("users").document(idToken).set(userInfo)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "User profile successfully written!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing user profile", e));
            } else {
                Log.d(TAG, "User already exists, not overwriting.");
            }
            callback.onSignInSuccess(account);
        });
    }

    public interface SignInCallback {
        void onSignInSuccess(GoogleSignInAccount account);

        void onSignInFailure(Exception e);
    }

    // User class to store user data
    public static class User {
        private String email;
        private String name;
        private String profilePicture;
        private String role;

        public User() {}

        public User(String email, String name, String profilePicture, String role) {
            this.email = email;
            this.name = name;
            this.profilePicture = profilePicture;
            this.role = role;
        }

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getProfilePicture() { return profilePicture; }
        public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
