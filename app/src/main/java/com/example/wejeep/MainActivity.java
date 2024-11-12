package com.example.wejeep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    @Override
    public void onStart() {
        super.onStart();
        // If the user is already signed in, check their role and redirect accordingly
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            checkUserRoleAndRedirect(user);
        }
    }
    private void checkUserRoleAndRedirect(FirebaseUser user) {
        String userId = user.getUid();
        db = FirebaseFirestore.getInstance();

        // Fetch the user's role from Firestore
        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String role = task.getResult().getString("role");
                if ("admin".equals(role)) {
                    // If user is an admin, redirect to AdminDashboard
                    startActivity(new Intent(getApplicationContext(), AdminDashboard.class));
                } else {
                    // Default redirection to HSPassenger for other users
                    startActivity(new Intent(getApplicationContext(), HSPassenger.class));
                }
                finish(); // Close MainActivity after redirecting
            } else {
                // Handle the case where the user role does not exist or an error occurred
                startActivity(new Intent(getApplicationContext(), HSPassenger.class)); // Redirect to HSPassenger by default
                finish(); // Close MainActivity
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSignup,btnLogin;

        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,Login.class);
                startActivity(intent);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SignUp.class);
                        startActivity(intent);
            }
        });
    }
    @Override
    public void onBackPressed() {
        BackPressHandler.handleBackPress(this);
    }
}