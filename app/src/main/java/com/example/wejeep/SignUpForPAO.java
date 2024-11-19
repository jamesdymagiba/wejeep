package com.example.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import java.util.HashMap;

public class SignUpForPAO extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFunctions firebaseFunctions;
    private TextInputEditText etEmailSU, etPasswordSU, etNameSU;
    private Button btnSignupSU;
    private CustomLoadingDialog customLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


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

        customLoadingDialog = new CustomLoadingDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFunctions = FirebaseFunctions.getInstance();

        etEmailSU = findViewById(R.id.etEmailSU);
        etPasswordSU = findViewById(R.id.etPasswordSU);
        etNameSU = findViewById(R.id.etNameSU);
        btnSignupSU = findViewById(R.id.btnSignupSU);

        btnSignupSU.setOnClickListener(view -> {
            customLoadingDialog.showLoadingScreen();
            String email = String.valueOf(etEmailSU.getText());
            String password = String.valueOf(etPasswordSU.getText());
            String name = String.valueOf(etNameSU.getText());

            if (validateInputs(email, password, name)) {
                createPaoUser(email, password, name);
            } else {
                customLoadingDialog.hideLoadingScreen();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Get the ID token asynchronously
            user.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // The ID token is valid and contains custom claims
                            GetTokenResult tokenResult = task.getResult();
                            if (tokenResult != null) {
                                // Retrieve custom claims
                                if (tokenResult.getClaims().containsKey("admin") &&
                                        tokenResult.getClaims().get("admin").equals(true)) {
                                    Log.d("Admin", "User has admin privileges");
                                } else {
                                    Log.d("Admin", "User does not have admin privileges");
                                }
                            }
                        } else {
                            Log.e("Error", "Failed to get ID token: " + task.getException().getMessage());
                        }
                    });
        }

    }

    private void createPaoUser(String email, String password, String name) {
        // Check if user is signed in
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(SignUpForPAO.this, "User must be signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get ID token from the currently signed-in user
        user.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String idToken = task.getResult().getToken();  // Retrieve the ID token

                        // Prepare data to send to the Firebase function
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("email", email);
                        data.put("password", password);
                        data.put("name", name);
                        data.put("idToken", idToken);  // Send the ID token as part of the data

                        // Call the Firebase function with the ID token
                        firebaseFunctions
                                .getHttpsCallable("createPaoUser")
                                .call(data)
                                .addOnCompleteListener(task1 -> {
                                    customLoadingDialog.hideLoadingScreen();
                                    if (task1.isSuccessful()) {
                                        // User created successfully
                                        Toast.makeText(SignUpForPAO.this, "User created successfully.", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(SignUpForPAO.this, AdminManagePAO.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(SignUpForPAO.this, "Error creating user: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        customLoadingDialog.hideLoadingScreen();
                        Log.e("Error", "Failed to get ID token: " + task.getException().getMessage());
                        Toast.makeText(SignUpForPAO.this, "Failed to get ID token.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean validateInputs(String email, String password, String name) {
        boolean emailValid = isValidEmail(email) && checkField(etEmailSU);
        boolean passwordValid = isValidPassword(password) && checkField(etPasswordSU);
        boolean nameValid = checkField(etNameSU);

        if (!emailValid) {
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
        }
        if (!passwordValid) {
            Toast.makeText(this, "Password must be at least 8 characters long.", Toast.LENGTH_LONG).show();
        }

        return emailValid && passwordValid && nameValid;
    }

    public boolean checkField(TextInputEditText textField) {
        return !textField.getText().toString().isEmpty();
    }

    public boolean isValidEmail(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidPassword(CharSequence password) {
        return password != null && password.length() >= 8;
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


    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
