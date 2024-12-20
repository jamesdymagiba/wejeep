package com.dygroup.wejeep;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminEditDriver extends AppCompatActivity {

    private EditText etDriverName, etDriverContact, etDateAdded;
    private Button btnApplyChanges, btnBack;
    private FirebaseFirestore db;
    private String documentId; // To store the document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_driver);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Reference UI elements
        etDriverName = findViewById(R.id.etDriverName);
        etDriverContact = findViewById(R.id.etDriverContact);
        etDateAdded = findViewById(R.id.etDateAdded);
        btnApplyChanges = findViewById(R.id.btnApplyChanges);
        btnBack = findViewById(R.id.btnBack);

        // Get driver data from Intent
        documentId = getIntent().getStringExtra("documentId");
        String driverName = getIntent().getStringExtra("driverName");
        String driverContact = getIntent().getStringExtra("driverContact");
        String dateAdded = getIntent().getStringExtra("dateAdded");

        // Pre-fill the EditTexts with received data
        etDriverName.setText(driverName);
        etDriverContact.setText(driverContact);
        etDateAdded.setText(dateAdded);
        etDateAdded.setEnabled(false);

        // Handle Apply Changes button click
        btnApplyChanges.setOnClickListener(v -> {
            applyChanges();
        });

        // Handle Back button click
        btnBack.setOnClickListener(v -> {
            finish(); // Go back to the previous activity
        });
    }

    private void applyChanges() {
        String updatedName = etDriverName.getText().toString().trim();
        String updatedContact = etDriverContact.getText().toString().trim();
        String updatedDateAdded = etDateAdded.getText().toString().trim();

        if (updatedName.isEmpty() || updatedContact.isEmpty() || updatedDateAdded.isEmpty()) {
            Toast.makeText(AdminEditDriver.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }
           String driverNamePattern = "^[a-zA-Z ]{2,}$";
        if (!updatedName.matches(driverNamePattern)) {
            etDriverName.setError("Name must be at least 2 characters and contain only letters and spaces");
            return;
        }

        String phoneNumberPattern = "^09\\d{9}$";
        if (!updatedContact.matches(phoneNumberPattern)) {
            etDriverContact.setError("Phone number must be 11 digits and start with '09'");
            return;
        }



        // Update the driver document in Firestore
        db.collection("drivers").document(documentId)
                .update("name", updatedName,
                        "contact", updatedContact,
                        "dateAdded", updatedDateAdded)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminEditDriver.this, "Driver updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish(); // Go back after successful update
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminEditDriver.this, "Error updating driver: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
