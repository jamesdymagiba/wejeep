package com.example.wejeep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignUpForPAO extends AppCompatActivity {

    private TextInputEditText etEmail, etName, etPassword;
    private Button btnSignPao, btnGooglePao, btnBack;
    private ProgressBar pbSignUp;
    private FirebaseFirestore db;
    private GoogleSignInHelperForPAO googleSignInHelper;
    private AuthForPAO authForPAO;  // Instance of AuthForPAO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_for_pao);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the views
        etEmail = findViewById(R.id.etEmailSU);
        etName = findViewById(R.id.etNameSU);
        etPassword = findViewById(R.id.etPasswordSU);
        btnSignPao = findViewById(R.id.btnSignPao);
        pbSignUp = findViewById(R.id.pbSU);
        btnBack = findViewById(R.id.btnNewAction);
        btnGooglePao = findViewById(R.id.btnGooglePao);  // Button for Google Sign-In

        // Initialize Google Sign-In helper
        googleSignInHelper = new GoogleSignInHelperForPAO(this);

        // Initialize AuthForPAO
        authForPAO = new AuthForPAO();

        // Set up button click listeners
        btnSignPao.setOnClickListener(v -> signUpPAO());
        btnGooglePao.setOnClickListener(v -> googleSignInHelper.signIn());  // Call Google sign-in when button is clicked
        btnBack.setOnClickListener(v -> navigateBack());
    }

    private void signUpPAO() {
        // Show progress bar while processing
        pbSignUp.setVisibility(View.VISIBLE);

        // Get user input
        String email = etEmail.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();  // Retrieve password

        // Validate input
        String validationError = validateInput(email, name);
        if (validationError != null) {
            pbSignUp.setVisibility(View.GONE);
            Toast.makeText(SignUpForPAO.this, validationError, Toast.LENGTH_SHORT).show();
            return;
        }

        // Call AuthForPAO to sign up the user and send the verification email
        authForPAO.signUpWithEmailPassword(email, password, this, new AuthForPAO.SignUpCallback() {
            @Override
            public void onSignUpSuccess(String email) {
                // After successful sign-up, save user data to Firestore
                String dateAdded = new SimpleDateFormat("MMMM dd yyyy", Locale.getDefault()).format(new Date());
                UserModel newUser = new UserModel(email, name, "pao", dateAdded);
                savePAOToFirestore(newUser);
                pbSignUp.setVisibility(View.GONE);
                Toast.makeText(SignUpForPAO.this, "Registration successful.", Toast.LENGTH_LONG).show();
                finish();  // Close SignUpForPAO activity
            }

            @Override
            public void onSignUpFailure(String error) {
                pbSignUp.setVisibility(View.GONE);
                Toast.makeText(SignUpForPAO.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePAOToFirestore(UserModel newUser) {
        db.collection("users")
                .add(newUser)  // Automatically generates a new document ID
                .addOnSuccessListener(documentReference -> {
                    // Do nothing here as email verification is already handled
                })
                .addOnFailureListener(e -> {
                    pbSignUp.setVisibility(View.GONE);
                    Toast.makeText(SignUpForPAO.this, "Error saving PAO data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String validateInput(String email, String name) {
        if (email.isEmpty()) {
            return "Email cannot be empty";
        }
        if (name.isEmpty()) {
            return "Name cannot be empty";
        }
        return null; // All inputs are valid
    }

    private void navigateBack() {
        Intent intent = new Intent(SignUpForPAO.this, AdminManagePAO.class); // Navigate to AdminManagePAO
        startActivity(intent); // Start the AdminManagePAO activity
        finish(); // Close the current activity
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleSignInHelper.handleSignInResult(requestCode, resultCode, data, new GoogleSignInHelperForPAO.SignInCallback() {
            @Override
            public void onSignInSuccess(GoogleSignInAccount account) {
                String email = account.getEmail();
                String name = account.getDisplayName();
                String dateAdded = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());

                // Create PAO user data from Google account
                UserModel newUser = new UserModel(email, name, "pao", dateAdded);
                savePAOToFirestore(newUser);
            }

            @Override
            public void onSignInFailure(Exception e) {
                Toast.makeText(SignUpForPAO.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}


class UserModel {
    private String email;
    private String name;
    private String role;
    private String dateAdded;

    public UserModel(String email, String name, String role, String dateAdded) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.dateAdded = dateAdded;
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
        return dateAdded;
    }
}

