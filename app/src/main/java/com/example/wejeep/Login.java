package com.example.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private AuthManager authManager;
    private GoogleSignInHelper googleSignInHelper;
    private CustomLoadingDialog customLoadingDialog;
    private TextInputEditText etEmailLI, etPasswordLI;
    private Button btnLoginLI, btnGoogleLI, btnForgetLI;
    private boolean valid = true;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        customLoadingDialog = new CustomLoadingDialog(this);

        authManager = new AuthManager(this);
        googleSignInHelper = new GoogleSignInHelper(this);

        etEmailLI = findViewById(R.id.etEmailLI);
        etPasswordLI = findViewById(R.id.etPasswordLI);
        btnLoginLI = findViewById(R.id.btnLoginLI);
        btnGoogleLI = findViewById(R.id.btnGoogleLI);
        btnForgetLI = findViewById(R.id.btnForgetLI);

        Toolbar toolbar2 = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        btnLoginLI.setOnClickListener(view -> {

            String email = String.valueOf(etEmailLI.getText());
            String password = String.valueOf(etPasswordLI.getText());

            if (validateInputs()) {
                customLoadingDialog.showLoadingScreen();
                authManager.signInUser(email, password, customLoadingDialog);
            } else {
                customLoadingDialog.hideLoadingScreen();
            }
        });

        btnGoogleLI.setOnClickListener(v -> {
            customLoadingDialog.showLoadingScreen();
            googleSignInHelper.signIn();
        });
        btnForgetLI.setOnClickListener(v -> {
            String email = etEmailLI.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(Login.this, "Please enter your email to reset the password.", Toast.LENGTH_SHORT).show();
            } else {
                customLoadingDialog.showLoadingScreen();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            customLoadingDialog.hideLoadingScreen();
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Login.this, "Error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            googleSignInHelper.handleSignInResult(requestCode, resultCode, data, new GoogleSignInHelper.SignInCallback() {
                @Override
                public void onSignInSuccess(FirebaseUser user) {
                    customLoadingDialog.hideLoadingScreen();
                    if (user != null) {
                        if (user.getEmail() != null) {
                            // Check if the user is registered
                            authManager.checkIfUserExists(user.getEmail(), exists -> {
                                if (exists) {
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, "User not found.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onSignInFailure(Exception e) {
                    customLoadingDialog.hideLoadingScreen();
                    Toast.makeText(Login.this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean checkField(TextInputEditText textField) {
        if (textField.getText().toString().isEmpty()) {
            Toast.makeText(Login.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else {
            valid = true;
        }
        return valid;
    }

    private boolean validateInputs() {
        boolean emailValid = checkField(etEmailLI);
        boolean passwordValid = checkField(etPasswordLI);

        return emailValid && passwordValid;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
