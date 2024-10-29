package com.example.wejeep;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminAddUnitScreen extends AppCompatActivity {
    private ArrayList<UnitModel> unitList;
    private UnitAdapter unitAdapter;
    private EditText etVehicleModel, etPlateNumber, etUnitNumber, etDateAdded;
    private Button btnAddUnit, btnBack;
    private FirebaseFirestore db;

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
        etVehicleModel = findViewById(R.id.etVehicleModel);
        etPlateNumber = findViewById(R.id.etPlateNumber);
        etUnitNumber = findViewById(R.id.etUnitNumber);
        etDateAdded = findViewById(R.id.etUnitDateAdded);
        btnAddUnit = findViewById(R.id.btnAddUnit);
        btnBack = findViewById(R.id.btnBack);

        // Handle back button click
        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(AdminAddUnitScreen.this, AdminManageUnitScreen.class));
        });

        // Handle date picker for "Date Added"
        // Set today's date in etDateAdded
        String todayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        etDateAdded.setText(todayDate);
        etDateAdded.setEnabled(false); // Disable editing of the date field

        // Handle Add Unit button click
        btnAddUnit.setOnClickListener(v -> addUnitToFirestore());
    }

    private void addUnitToFirestore() {
        String unitVehicleModel = etVehicleModel.getText().toString().trim();
        String unitPlateNumber = etPlateNumber.getText().toString().trim();
        String unitNumber = etUnitNumber.getText().toString().trim();
        String dateAdded = etDateAdded.getText().toString().trim();

        // Validate fields
        if (unitVehicleModel.isEmpty() || unitPlateNumber.isEmpty() || dateAdded.isEmpty() || unitNumber.isEmpty()) {
            Toast.makeText(AdminAddUnitScreen.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Regex for unit number: only 2-digit numbers (00-99)
        String unitNumberPattern = "^[0-9]{2}$";
        if (!unitNumber.matches(unitNumberPattern)) {
            etUnitNumber.setError("Unit number must be a 2-digit number (e.g., '01', '99')");
            return;
        }

        // Regex for plate number format "ABC 1234"
        String plateNumberPattern = "^[A-Z]{3} [0-9]{4}$";
        if (!unitPlateNumber.matches(plateNumberPattern)) {
            etPlateNumber.setError("Invalid plate number format. Use 'ABC 1234'");
            return;
        }

        // Create a unit object
        Map<String, Object> unit = new HashMap<>();
        unit.put("vehicleModel", unitVehicleModel);
        unit.put("plateNumber", unitPlateNumber);
        unit.put("unitNumber", unitNumber);
        unit.put("dateAdded", dateAdded);

        // Add a new document in the "units" collection
        db.collection("units")
                .add(unit)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference docRef = task.getResult();
                        UnitModel newUnit = new UnitModel(unitVehicleModel, unitPlateNumber, unitNumber, dateAdded, docRef.getId());
                        unitList.add(newUnit);
                        unitAdapter.notifyItemInserted(unitList.size() - 1);
                        Toast.makeText(AdminAddUnitScreen.this, "Unit added successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminAddUnitScreen.this, AdminManageUnitScreen.class));
                    } else {
                        Toast.makeText(AdminAddUnitScreen.this, "Error adding unit: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
