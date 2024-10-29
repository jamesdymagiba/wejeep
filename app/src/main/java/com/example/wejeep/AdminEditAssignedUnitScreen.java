package com.example.wejeep;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminEditAssignedUnitScreen extends AppCompatActivity {

    private Spinner spinnerUnitnumber, spinnerPlatenumber, spinnerDriver, spinnerConductor, spinnerToday, spinnerFromday, spinnerTotime, spinnerFromtime;
    private Button btnConfirm, btnBack;
    private FirebaseFirestore db;
    private String documentId;

    // Data lists for spinners
    private ArrayList<String> unitnumberList = new ArrayList<>();
    private ArrayList<String> platenumberList = new ArrayList<>();
    private ArrayList<String> driverList = new ArrayList<>();
    private ArrayList<String> conductorList = new ArrayList<>();
    private ArrayList<String> fromdayList = new ArrayList<>();
    private ArrayList<String> todayList = new ArrayList<>();
    private ArrayList<String> fromtimeList = new ArrayList<>();
    private ArrayList<String> totimeList = new ArrayList<>();


    // Adapters for spinners
    private ArrayAdapter<String> unitnumberAdapter, platenumberAdapter, driverAdapter, conductorAdapter, fromdayAdapter, todayAdapter, fromtimeAdapter, totimeAdapter ;

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
        spinnerConductor = findViewById(R.id.spinnerConductor);
        spinnerFromday = findViewById(R.id.spinnerFromday);
        spinnerToday = findViewById(R.id.spinnerToday);
        spinnerTotime = findViewById(R.id.spinnerTotime);
        spinnerFromtime = findViewById(R.id.spinnerFromtime);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Get driver data from Intent
        documentId = getIntent().getStringExtra("documentId");
        String unitNumber = getIntent().getStringExtra("unitNumber");
        String plateNumber = getIntent().getStringExtra("plateNumber");
        String driverName = getIntent().getStringExtra("driverName");
        String conductorName = getIntent().getStringExtra("conductorName");
        String fromDay = getIntent().getStringExtra("driverName");
        String toDay = getIntent().getStringExtra("driverName");
        String fromTime = getIntent().getStringExtra("driverName");
        String toTime = getIntent().getStringExtra("driverName");


        // Set up adapters
        unitnumberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitnumberList);
        unitnumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnitnumber.setAdapter(unitnumberAdapter);

        platenumberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, platenumberList);
        platenumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlatenumber.setAdapter(platenumberAdapter);

        driverAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, driverList);
        driverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDriver.setAdapter(driverAdapter);

        // Fetch data for spinners
        fetchUnits(unitNumber);
        fetchPlatenumber(plateNumber);
        fetchDrivers(driverName);

        // Handle Apply Changes button click
        btnConfirm.setOnClickListener(v -> applyChanges());

        // Handle Back button click
        btnBack.setOnClickListener(v -> finish());
    }

    private void fetchUnits(String unitNumber) {
        db.collection("units")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        unitnumberList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String unitnumber = document.getString("unitNumber");
                            if (unitnumber != null) {
                                unitnumberList.add(unitnumber);
                            }
                        }
                        unitnumberAdapter.notifyDataSetChanged();
                        setSpinnerSelection(spinnerUnitnumber, unitNumber);
                    }
                });
    }

    private void fetchPlatenumber(String plateNumber) {
        db.collection("units")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        platenumberList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String platenumber = document.getString("plateNumber");
                            if (platenumber != null) {
                                platenumberList.add(platenumber);
                            }
                        }
                        platenumberAdapter.notifyDataSetChanged();
                        setSpinnerSelection(spinnerPlatenumber, plateNumber);
                    }
                });
    }

    private void fetchDrivers(String driverName) {
        db.collection("drivers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        driverList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String driver = document.getString("name");
                            if (driver != null) {
                                driverList.add(driver);
                            }
                        }
                        driverAdapter.notifyDataSetChanged();
                        setSpinnerSelection(spinnerDriver, driverName);
                    }
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

        // Update the document in Firestore
        db.collection("assigns").document(documentId)
                .update("unitnumber", updatedUnitNumber,
                        "platenumber", updatedPlatenumber,
                        "driver", updatedDriver)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminEditAssignedUnitScreen.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AdminEditAssignedUnitScreen.this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Set the selected item in a spinner based on the provided value
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        }
    }
}
