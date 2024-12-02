package com.dygroup.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {

    private AuthManager authManager;
    private GoogleSignInHelper googleSignInHelper;
    private TextInputEditText etEmailSU, etPasswordSU, etNameSU;
    private Button btnSignupSU, btnGoogleSU;
    private CustomLoadingDialog customLoadingDialog;
    private boolean valid = true;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        customLoadingDialog = new CustomLoadingDialog(this);

        authManager = new AuthManager(this);
        googleSignInHelper = new GoogleSignInHelper(this);

        etEmailSU = findViewById(R.id.etEmailSU);
        etPasswordSU = findViewById(R.id.etPasswordSU);
        etNameSU = findViewById(R.id.etNameSU);
        btnSignupSU = findViewById(R.id.btnSignupSU);
        btnGoogleSU = findViewById(R.id.btnGoogleSU);

        btnSignupSU.setOnClickListener(view -> {
            customLoadingDialog.showLoadingScreen();
            String email = String.valueOf(etEmailSU.getText());
            String password = String.valueOf(etPasswordSU.getText());
            String name = String.valueOf(etNameSU.getText());

            if (validateInputs(email, password, name)) {
                authManager.signUpUser(email, password, name, customLoadingDialog);
            } else {
                customLoadingDialog.hideLoadingScreen();
            }
        });

        btnGoogleSU.setOnClickListener(v -> {
            customLoadingDialog.showLoadingScreen();
            googleSignInHelper.signIn();
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            googleSignInHelper.handleSignInResult(requestCode, resultCode, data, new GoogleSignInHelper.SignInCallback() {
                @Override
                public void onSignInSuccess(FirebaseUser user) {
                    customLoadingDialog.hideLoadingScreen();
                    Toast.makeText(SignUp.this, "Sign-in successful: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUp.this, HSPassenger.class);
                    startActivity(intent);
                    finish(); // Close SignUp activity
                }

                @Override
                public void onSignInFailure(Exception e) {
                    customLoadingDialog.hideLoadingScreen();
                    Toast.makeText(SignUp.this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public boolean checkField(TextInputEditText textField) {
        if (textField.getText().toString().isEmpty()) {
            Toast.makeText(SignUp.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else {
            valid = true;
        }
        return valid;
    }

    public boolean isValidEmail(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidPassword(CharSequence password) {
        if (password == null) return false;

        // Password must be at least 8 characters long
        return password.length() >= 8;
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
