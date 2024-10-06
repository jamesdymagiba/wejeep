package com.example.wejeep;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminEditUnitScreen extends AppCompatActivity {

    private EditText etvehicleModel, etplateNumber, etDateAdded,  etunitNumber;
    private Button btnApplyChanges, btnBack;
    private FirebaseFirestore db;
    private String documentId; // To store the document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_unit_screen);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Reference UI elements
        etvehicleModel = findViewById(R.id.etvehicleModel);
        etplateNumber = findViewById(R.id.etPlateNumber);
        etDateAdded = findViewById(R.id.etDateAdded);
        etunitNumber = findViewById(R.id.etUnitNumber);
        btnApplyChanges = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Get driver data from Intent
        documentId = getIntent().getStringExtra("documentId");
        String vehicleModel = getIntent().getStringExtra("vehicleModel");
        String unitNumber = getIntent().getStringExtra("unitNumber");
        String plateNumber = getIntent().getStringExtra("plateNumber");
        String dateAdded = getIntent().getStringExtra("dateAdded");

        // Pre-fill the EditTexts with received data
        etvehicleModel.setText(vehicleModel);
        etplateNumber.setText(plateNumber);
        etunitNumber.setText(unitNumber);
        etDateAdded.setText(dateAdded);

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
        String updatedvehicleModel = etvehicleModel.getText().toString().trim();
        String updatedplateNumber = etplateNumber.getText().toString().trim();
        String updatedunitNumber = etunitNumber.getText().toString().trim();
        String updatedDateAdded = etDateAdded.getText().toString().trim();

        if (updatedvehicleModel.isEmpty() || updatedplateNumber.isEmpty() || updatedDateAdded.isEmpty()) {
            Toast.makeText(AdminEditUnitScreen.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the driver document in Firestore
        db.collection("units").document(documentId)
                .update("vehicleModel", updatedvehicleModel,
                        "plateNumber", updatedplateNumber,
                        "dateAdded", updatedDateAdded, "unitNumber" , updatedunitNumber)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminEditUnitScreen.this, "Unit updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish(); // Go back after successful update
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminEditUnitScreen.this, "Error updating unit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
