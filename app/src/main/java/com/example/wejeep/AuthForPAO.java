package com.example.wejeep;

import android.app.Activity;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthForPAO {
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