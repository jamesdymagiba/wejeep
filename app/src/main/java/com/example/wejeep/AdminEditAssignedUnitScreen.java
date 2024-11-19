package com.example.wejeep;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminEditAssignedUnitScreen extends AppCompatActivity {

    private Spinner spinnerUnitnumber, spinnerPlatenumber, spinnerDriver, spinnerConductor, spinnerSchedule;
    private Button btnConfirm, btnBack;
    private FirebaseFirestore db;
    private String documentId, selectedConductor, selectedPlateNumber, selectedDriver;
    private EditText EditTextFromday,EditTextToday,EditTextTotime,EditTextFromtime;

    // Data lists for spinners
    private ArrayList<String> unitnumberList = new ArrayList<>();
    private ArrayList<String> platenumberList = new ArrayList<>();
    private ArrayList<String> driverList = new ArrayList<>();
    private ArrayList<String> conductorList = new ArrayList<>();
    private ArrayList<String> scheduleList = new ArrayList<>();


    // Adapters for spinners
    private ArrayAdapter<String> unitnumberAdapter, platenumberAdapter, driverAdapter, conductorAdapter, scheduleAdapter;

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
        spinnerSchedule = findViewById(R.id.spinnerSchedule);
        EditTextFromday = findViewById(R.id.etFromday);
        EditTextToday = findViewById(R.id.etToday);
        EditTextTotime = findViewById(R.id.etTotime);
        EditTextFromtime = findViewById(R.id.etFromtime);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        EditTextFromday.setEnabled(false);
        EditTextToday.setEnabled(false);
        EditTextFromtime.setEnabled(false);
        EditTextTotime.setEnabled(false);

        // Get driver data from Intent
        documentId = getIntent().getStringExtra("documentId");
        String unitNumber = getIntent().getStringExtra("unitNumber");
        String plateNumber = getIntent().getStringExtra("plateNumber");
        String driverName = getIntent().getStringExtra("driverName");
        String conductorName = getIntent().getStringExtra("conductorName");
        String fromDay = getIntent().getStringExtra("fromDay");
        String toDay = getIntent().getStringExtra("toDay");
        String fromTime = getIntent().getStringExtra("fromTime");
        String toTime = getIntent().getStringExtra("toTime");

        // Set EditText values
        if (fromDay != null) EditTextFromday.setText(fromDay);
        if (toDay != null) EditTextToday.setText(toDay);
        if (fromTime != null) EditTextFromtime.setText(fromTime);
        if (toTime != null) EditTextTotime.setText(toTime);

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

        conductorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, conductorList);
        conductorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerConductor.setAdapter(conductorAdapter);

        scheduleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scheduleList);
        scheduleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchedule.setAdapter(scheduleAdapter);

        // Fetch data for spinners and set initial selections
        fetchUnits(unitNumber);
        fetchPlatenumber(plateNumber);
        fetchDrivers(driverName);
        fetchConductor(conductorName);
        fetchSchedules();

        // Handle Apply Changes button click
        btnConfirm.setOnClickListener(v -> applyChanges());

        // Handle Back button click
        btnBack.setOnClickListener(v -> finish());
    }

    // Updated methods to set initial selection
    private void fetchConductor(String conductorName) {
        db.collection("users")
                .whereEqualTo("role", "pao")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        conductorList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            if (name != null) {
                                conductorList.add(name);
                            }
                        }
                        conductorAdapter.notifyDataSetChanged();
                        setSpinnerSelection(spinnerConductor, conductorName);
                    }
                });
    }

    private void fetchSchedules() {
        db.collection("schedules")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        scheduleList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String schedule = document.getString("schedule");
                            if (schedule != null) {
                                scheduleList.add(schedule);
                            }
                        }
                        scheduleAdapter.notifyDataSetChanged();
                    }
                });
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
        String updatedConductor = spinnerConductor.getSelectedItem().toString().trim();
        String updatedFromday = EditTextFromday.getText().toString().trim();
        String updatedFromtime = EditTextFromtime.getText().toString().trim();
        String updatedTotime = EditTextTotime.getText().toString().trim();
        String updatedToday = EditTextToday.getText().toString().trim();



        if (updatedUnitNumber.isEmpty() || updatedPlatenumber.isEmpty() || updatedDriver.isEmpty() || updatedFromday.isEmpty() || updatedTotime.isEmpty() || updatedToday.isEmpty() || updatedFromday.isEmpty() || updatedConductor.isEmpty()) {
            Toast.makeText(AdminEditAssignedUnitScreen.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the document in Firestore
        db.collection("assigns").document(documentId)
                .update("unitnumber", updatedUnitNumber,
                        "platenumber", updatedPlatenumber,
                        "driver", updatedDriver, "conductor", updatedConductor, "fromday", updatedFromday,
                "today", updatedToday, "fromtime", updatedFromtime, "totime", updatedTotime)
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
