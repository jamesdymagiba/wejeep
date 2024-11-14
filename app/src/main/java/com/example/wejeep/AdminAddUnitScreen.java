package com.example.wejeep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;

public class AdminAddUnitScreen extends AppCompatActivity {
    private ArrayList<UnitModel> unitList;
    private UnitAdapter unitAdapter;
    private Spinner spinnerVehicleModel;
    private EditText etPlateNumber, etUnitNumber, etDateAdded;
    private Button btnAddUnit, btnBack, btnAddVehicleModel;
    private FirebaseFirestore db;
    private static final int MAX_UNITS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_unit_screen);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize unitList and unitAdapter
        unitList = new ArrayList<>();
        unitAdapter = new UnitAdapter(unitList);

        // Reference UI elements
        spinnerVehicleModel = findViewById(R.id.spinnerVehicleModel);
        etPlateNumber = findViewById(R.id.etPlateNumber);
        etUnitNumber = findViewById(R.id.etUnitNumber);
        etDateAdded = findViewById(R.id.etUnitDateAdded);
        btnAddUnit = findViewById(R.id.btnAddUnit);
        btnBack = findViewById(R.id.btnBack);
        btnAddVehicleModel = findViewById(R.id.btnAddVehicleModel);

        // Set up the back button
        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(AdminAddUnitScreen.this, AdminManageUnitScreen.class));
        });

        btnAddVehicleModel.setOnClickListener(view -> {
            startActivity(new Intent(AdminAddUnitScreen.this, AdminAddVehicleUnit.class));
        });

        // Set today's date in etDateAdded
        String todayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        etDateAdded.setText(todayDate);
        etDateAdded.setEnabled(false); // Disable editing of the date field

        // Set the initial unit number based on the next available value
        setInitialUnitNumber();

        // Load vehicle models into the spinner
        loadVehicleModelsIntoSpinner();

        // Handle Add Unit button click
        btnAddUnit.setOnClickListener(v -> addUnitToFirestore());
    }

    private void loadVehicleModelsIntoSpinner() {
        db.collection("vehicles").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> vehicleModels = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    String model = document.getString("vehiclemodel");
                    if (model != null) {
                        vehicleModels.add(model);
                    }
                }

                // Populate the spinner with vehicle models
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleModels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerVehicleModel.setAdapter(adapter);

            } else {
                Toast.makeText(this, "Failed to load vehicle models.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setInitialUnitNumber() {
        db.collection("units").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ArrayList<Integer> existingUnitNumbers = new ArrayList<>();

                for (DocumentSnapshot doc : task.getResult()) {
                    String unitNumberStr = doc.getString("unitNumber");
                    if (unitNumberStr != null && unitNumberStr.matches("\\d+")) {
                        existingUnitNumbers.add(Integer.parseInt(unitNumberStr));
                    }
                }

                int nextAvailableNumber = findNextAvailableNumber(existingUnitNumbers);
                etUnitNumber.setText(String.valueOf(nextAvailableNumber));
                etUnitNumber.setEnabled(false);

            } else {
                Toast.makeText(this, "Error retrieving unit numbers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int findNextAvailableNumber(ArrayList<Integer> existingUnitNumbers) {
        Collections.sort(existingUnitNumbers);
        for (int i = 1; i <= MAX_UNITS; i++) {
            if (!existingUnitNumbers.contains(i)) {
                return i;
            }
        }
        return existingUnitNumbers.size() + 1;
    }

    private void addUnitToFirestore() {
        String unitVehicleModel = spinnerVehicleModel.getSelectedItem() != null ? spinnerVehicleModel.getSelectedItem().toString() : "";
        String unitPlateNumber = etPlateNumber.getText().toString().trim();
        String unitNumber = etUnitNumber.getText().toString().trim();
        String dateAdded = etDateAdded.getText().toString().trim();

        if (unitVehicleModel.isEmpty() || unitPlateNumber.isEmpty() || dateAdded.isEmpty() || unitNumber.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String plateNumberPattern = "^[A-Z]{3} [0-9]{4}$";
        if (!unitPlateNumber.matches(plateNumberPattern)) {
            etPlateNumber.setError("Invalid plate number format. Use 'ABC 1234'");
            return;
        }

        Map<String, Object> unit = new HashMap<>();
        unit.put("vehicleModel", unitVehicleModel);
        unit.put("plateNumber", unitPlateNumber);
        unit.put("unitNumber", unitNumber);
        unit.put("dateAdded", dateAdded);

        db.collection("units")
                .add(unit)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Unit added successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminAddUnitScreen.this, AdminManageUnitScreen.class));
                    } else {
                        Toast.makeText(this, "Error adding unit: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
