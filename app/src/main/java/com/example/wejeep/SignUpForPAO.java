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
                        Log.d("Admin", "Token: "+idToken);
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
                                        Toast.makeText(SignUpForPAO.this, "User created successfully with PAO role", Toast.LENGTH_LONG).show();
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
