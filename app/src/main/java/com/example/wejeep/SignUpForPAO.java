package com.example.wejeep;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignUpForPAO extends AppCompatActivity {

    private TextInputEditText etEmail, etName, etPassword;
    private Button btnSignPao, btnGooglePao, btnBack;
    private ProgressBar pbSignUp;
    private GoogleSignInHelperForPAO googleSignInHelperForPAO;
    private FirebaseAuth AuthForPAO;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_for_pao);

        // Initialize Firebase Auth and Firestore
        AuthForPAO = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        googleSignInHelperForPAO = new GoogleSignInHelperForPAO(this);

        // Initialize the views
        etEmail = findViewById(R.id.etEmailSU);
        etName = findViewById(R.id.etNameSU);
        etPassword = findViewById(R.id.etPasswordSU);
        btnSignPao = findViewById(R.id.btnSignPao);
        btnGooglePao = findViewById(R.id.btnGooglePao);
        pbSignUp = findViewById(R.id.pbSU);
        btnBack = findViewById(R.id.btnNewAction);

        // Set up button click listeners
        btnSignPao.setOnClickListener(v -> signUpUser());
        btnGooglePao.setOnClickListener(v -> {
            pbSignUp.setVisibility(View.VISIBLE); // Show progress bar
            googleSignInHelperForPAO.signIn();
        });
        btnBack.setOnClickListener(v -> navigateBack());
    }

    private void signUpUser() {
        // Show progress bar while processing
        pbSignUp.setVisibility(View.VISIBLE);

        // Get user input
        String email = etEmail.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input
        if (validateInput(email, name, password)) {
            AuthForPAO.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = AuthForPAO.getCurrentUser();
                            if (user != null) {
                                // Get the current date in the desired format
                                String dateAdded = new SimpleDateFormat("MMMM dd yyyy", Locale.getDefault()).format(new Date());

                                // Create and save user object in Firestore with the date added
                                UserModel newUser = new UserModel(email, name, "pao", dateAdded);
                                saveUserToFirestore(user, newUser);
                                sendVerificationEmail(user); // Send verification email
                            }
                        } else {
                            pbSignUp.setVisibility(View.GONE);
                            Toast.makeText(SignUpForPAO.this, "Sign-up-PAO failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            pbSignUp.setVisibility(View.GONE);
            Toast.makeText(SignUpForPAO.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to save user data to Firestore
    private void saveUserToFirestore(FirebaseUser user, UserModel newUser) {
        db.collection("users").document(user.getUid())
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    pbSignUp.setVisibility(View.GONE);
                    Toast.makeText(SignUpForPAO.this, "PAO registered successfully! Please verify your email.", Toast.LENGTH_SHORT).show();
                    // Optional: You can also navigate to another activity here
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent); // Set result to OK
                    finish(); // Finish the activity
                })
                .addOnFailureListener(e -> {
                    pbSignUp.setVisibility(View.GONE);
                    Toast.makeText(SignUpForPAO.this, "Error saving PAO data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to send verification email
    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpForPAO.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SignUpForPAO.this, "Failed to send verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleSignInHelperForPAO.handleSignInResult(requestCode, resultCode, data, new GoogleSignInHelperForPAO.SignInCallback() {
            @Override
            public void onSignInSuccess(FirebaseUser user) {
                pbSignUp.setVisibility(View.GONE); // Hide progress bar
                Toast.makeText(SignUpForPAO.this, "Sign-in-PAO successful: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                // Create a new user and save it in Firestore
                String dateAdded = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());
                UserModel newUser = new UserModel(user.getEmail(), user.getDisplayName(), "pao", dateAdded);
                saveUserToFirestore(user, newUser); // Save user details to Firestore
            }

            @Override
            public void onSignInFailure(Exception e) {
                pbSignUp.setVisibility(View.GONE); // Hide progress bar
                Toast.makeText(SignUpForPAO.this, "Sign-in-PAO failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Example input validation method
    private boolean validateInput(String email, String name, String password) {
        return !email.isEmpty() && !name.isEmpty() && !password.isEmpty();
    }

    private void navigateBack() {
        Intent intent = new Intent(SignUpForPAO.this, AdminManagePAO.class); // Create intent to navigate to AdminManagePAO
        startActivity(intent); // Start the new activity
        finish(); // Optionally, close the current activity
    }
}

class UserModel {
    private String email;
    private String name;
    private String role;
    private String dateAdded;  // New field for date added

    public UserModel(String email, String name, String role, String dateAdded) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.dateAdded = dateAdded; // Initialize the dateAdded field
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getDateAdded() {
        return dateAdded; // Getter for dateAdded
    }
}

