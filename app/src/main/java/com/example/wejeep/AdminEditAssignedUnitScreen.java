package com.example.wejeep;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminEditAssignedUnitScreen extends AppCompatActivity {

    private Spinner spinnerUnitnumber, spinnerPlatenumber, spinnerDriver;
    private Button btnConfirm, btnBack;
    private FirebaseFirestore db;
    private String documentId; // To store the document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_assigned_unit_screen);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Reference UI elements
        spinnerUnitnumber = findViewById(R.id.spinnerUnitnumber);
        spinnerPlatenumber = findViewById(R.id.spinnerPlatenumber);
        spinnerDriver = findViewById(R.id.spinnerDriver);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Get driver data from Intent
        documentId = getIntent().getStringExtra("documentId");
        String unitNumber = getIntent().getStringExtra("unitNumber");
        String plateNumber = getIntent().getStringExtra("plateNumber");
        String driverName = getIntent().getStringExtra("driverName");

        // Pre-fill the Spinners with received data
        setSpinnerSelection(spinnerUnitnumber, unitNumber);
        setSpinnerSelection(spinnerPlatenumber, plateNumber);
        setSpinnerSelection(spinnerDriver, driverName);

        // Handle Apply Changes button click
        btnConfirm.setOnClickListener(v -> {
            applyChanges();
        });

        // Handle Back button click
        btnBack.setOnClickListener(v -> {
            finish(); // Go back to the previous activity
        });
    }

    private void applyChanges() {
        // Get the selected items from the spinners
        String updatedUnitNumber = spinnerUnitnumber.getSelectedItem().toString().trim();
        String updatedPlatenumber = spinnerPlatenumber.getSelectedItem().toString().trim();
        String updatedDriver = spinnerDriver.getSelectedItem().toString().trim();

        if (updatedUnitNumber.isEmpty() || updatedPlatenumber.isEmpty() || updatedDriver.isEmpty()) {
            Toast.makeText(AdminEditAssignedUnitScreen.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the driver document in Firestore
        db.collection("assigns").document(documentId)
                .update("unitnumber", updatedUnitNumber,
                        "platenumber", updatedPlatenumber,
                        "driver", updatedDriver)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminEditAssignedUnitScreen.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish(); // Go back after successful update
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminEditAssignedUnitScreen.this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to set the selected item in a spinner based on the provided value
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }
}
