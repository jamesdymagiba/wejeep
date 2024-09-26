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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AdminAddUnitScreen extends AppCompatActivity {
    private ArrayList<UnitModel> unitList;
    private UnitAdapter unitAdapter;
    private EditText etVehicleModel, etPlateNumber, etDateAdded;
    private Button btnAddUnit, btnBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_unit_screen);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize driverList and driverAdapter
        unitList = new ArrayList<>(); // Initialize the list here
        unitAdapter = new UnitAdapter(unitList); // Pass the list to the adapter

        // Reference UI elements
        etVehicleModel = findViewById(R.id.etVehicleModel);
        etPlateNumber = findViewById(R.id.etPlateNumber);
        etDateAdded = findViewById(R.id.etUnitDateAdded);
        btnAddUnit = findViewById(R.id.btnAddUnit);
        btnBack = findViewById(R.id.btnBack);

        // Handle back button click
        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(AdminAddUnitScreen.this, AdminManageUnitScreen.class));
        });

        // Handle date picker for "Date Added"
        etDateAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AdminAddUnitScreen.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                            etDateAdded.setText(date);
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        // Handle Add Driver button click
        btnAddUnit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) { addUnitToFirestore();

                startActivity(new Intent(AdminAddUnitScreen.this,AdminManageUnitScreen.class));
            }

        });
    }

    private void addUnitToFirestore() {
        String unitVehicleModel = etVehicleModel.getText().toString().trim();
        String unitPlateNumber = etPlateNumber.getText().toString().trim();
        String dateAdded = etDateAdded.getText().toString().trim();

        if (unitVehicleModel.isEmpty() || unitPlateNumber.isEmpty() || dateAdded.isEmpty()) {
            Toast.makeText(AdminAddUnitScreen.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a driver object
        Map<String, Object> unit = new HashMap<>();
        unit.put("vehicleModel", unitVehicleModel);
        unit.put("plateNumber", unitPlateNumber);
        unit.put("dateAdded", dateAdded);

        // Add a new document in the "drivers" collection
        db.collection("units")
                .add(unit)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference docRef = task.getResult();
                        UnitModel newUnit = new UnitModel(unitVehicleModel, unitPlateNumber, dateAdded, docRef.getId());
                        unitList.add(newUnit); // Add the new driver to your list
                        unitAdapter.notifyItemInserted(unitList.size() - 1); // Notify the adapter
                        Toast.makeText(AdminAddUnitScreen.this, "Unit added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminAddUnitScreen.this, "Error adding unit: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
