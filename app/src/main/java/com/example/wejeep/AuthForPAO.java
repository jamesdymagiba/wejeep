package com.example.wejeep;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthForPAO {

    private FirebaseAuth mAuth;

    public AuthForPAO() {
        mAuth = FirebaseAuth.getInstance();  // Initialize FirebaseAuth
    }

    // Sign up the user with email and password
    public void signUpWithEmailPassword(String email, String password, AppCompatActivity activity, SignUpCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        // Send verification email
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            callback.onSignUpSuccess(user.getEmail());
                                        } else {
                                            callback.onSignUpFailure("Verification email failed.");
                                        }
                                    });
                        }
                    } else {
                        callback.onSignUpFailure(task.getException() != null ? task.getException().getMessage() : "Sign up failed");
                    }
                });
    }

    // Interface for SignUp callbacks
    public interface SignUpCallback {
        void onSignUpSuccess(String email);
        void onSignUpFailure(String error);
    private FirebaseAuth firebaseAuth;
    private Activity activity;

    public AuthForPAO(Activity activity) {
        this.activity = activity;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void signOut() {
        firebaseAuth.signOut();
        Toast.makeText(activity, "Signed out successfully.", Toast.LENGTH_SHORT).show();
    }
}
