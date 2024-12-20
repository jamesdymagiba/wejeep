package com.dygroup.wejeep;

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
        String Schedule = getIntent().getStringExtra("schedule");
        String fromDay = getIntent().getStringExtra("fromDay");
        String toDay = getIntent().getStringExtra("toDay");
        String fromTime = getIntent().getStringExtra("fromTime");
        String toTime = getIntent().getStringExtra("toTime");

        // Set EditText values
        if (fromDay != null) EditTextFromday.setText(fromDay);
        if (toDay != null) EditTextToday.setText(toDay);
        if (fromTime != null) EditTextFromtime.setText(fromTime);
        if (toTime != null) EditTextTotime.setText(toTime);
        spinnerUnitnumber.getSelectedItem();//////////////////////////
        spinnerSchedule.getSelectedItem();//////////////
        spinnerConductor.getSelectedItem();///////////////////////
        spinnerDriver.getSelectedItem();//////////////////
        spinnerSchedule.getSelectedItem();///////////////////////

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

        spinnerUnitnumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Retrieve the selected unit number
                String selectedUnitnumber = unitnumberList.get(position);

                // Query Firestore to fetch the associated plate number
                db.collection("units")
                        .whereEqualTo("unitNumber", selectedUnitnumber)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                // Retrieve the plate number from the document
                                String plateNumber = task.getResult().getDocuments().get(0).getString("plateNumber");
                                if (plateNumber != null) {
                                    // Update spinnerPlatenumber to show the fetched plate number
                                    platenumberList.clear();
                                    platenumberList.add(plateNumber);
                                    platenumberAdapter.notifyDataSetChanged();

                                    // Set the selected plate number
                                    spinnerPlatenumber.setSelection(0);
                                }
                            } else {
                                Toast.makeText(AdminEditAssignedUnitScreen.this, "No plate number found for the selected unit", Toast.LENGTH_SHORT).show();
                                platenumberList.clear();
                                platenumberAdapter.notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(AdminEditAssignedUnitScreen.this, "Failed to fetch plate number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        spinnerSchedule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected schedule
                String selectedSchedule = parent.getItemAtPosition(position).toString();

                // Query Firestore for the document with the matching schedule
                db.collection("schedules")
                        .whereEqualTo("schedule", selectedSchedule)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Retrieve the "Fromday" field
                                    String fromDay = document.getString("Fromday");
                                    String toDay = document.getString("Today");
                                    String fromTime = document.getString("Fromtime");
                                    String toTime = document.getString("Totime");
                                    if (fromDay != null) {
                                        EditTextFromday.setText(fromDay);
                                    }
                                    if (toDay != null) {
                                        EditTextToday.setText(toDay);
                                    }
                                    if (fromTime != null) {
                                        EditTextFromtime.setText(fromTime);
                                    }
                                    if (toTime != null) {
                                        EditTextTotime.setText(toTime);
                                    } else {
                                        Toast.makeText(AdminEditAssignedUnitScreen.this, "Fromday not found in the document", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(AdminEditAssignedUnitScreen.this, "Schedule document not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(AdminEditAssignedUnitScreen.this, "Error fetching schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        spinnerConductor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected conductor's name
                selectedConductor = conductorList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Reset the selected conductor if nothing is selected
                selectedConductor = null;
            }
        });


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
        db.collection("units") // Access the "units" collection
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        unitnumberList.clear(); // Clear the existing list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String unitnumber = document.getString("unitNumber"); // Retrieve the "unitNumber" field
                            if (unitnumber != null && !unitnumber.isEmpty()) {
                                unitnumberList.add(unitnumber); // Add it to the list
                            }
                        }
                        if (unitnumberList.isEmpty()) {
                            Toast.makeText(this, "No unit numbers found.", Toast.LENGTH_SHORT).show();
                        } else {
                            unitnumberAdapter.notifyDataSetChanged(); // Notify the adapter about data changes

                            // Set the initial selection if unitNumber is provided
                            if (unitNumber != null && !unitNumber.isEmpty()) {
                                setSpinnerSelection(spinnerUnitnumber, unitNumber);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch units: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching units: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void fetchPlatenumber(String plateNumber) {
        db.collection("assigns")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        platenumberList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String platenumber = document.getString("platenumber");
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
        // Get the selected items from the spinners and text fields
        String updatedUnitNumber = spinnerUnitnumber.getSelectedItem().toString().trim();
        String updatedPlatenumber = spinnerPlatenumber.getSelectedItem().toString().trim();
        String updatedDriver = spinnerDriver.getSelectedItem().toString().trim();
        String updatedConductor = spinnerConductor.getSelectedItem().toString().trim();
        String updateSchedule = spinnerSchedule.getSelectedItem().toString().trim();
        String updatedFromday = EditTextFromday.getText().toString().trim();
        String updatedFromtime = EditTextFromtime.getText().toString().trim();
        String updatedTotime = EditTextTotime.getText().toString().trim();
        String updatedToday = EditTextToday.getText().toString().trim();

        if (updatedUnitNumber.isEmpty() || updatedPlatenumber.isEmpty() || updatedDriver.isEmpty() ||
                updatedFromday.isEmpty() || updatedTotime.isEmpty() || updatedToday.isEmpty() ||
                updatedConductor.isEmpty()) {
            Toast.makeText(AdminEditAssignedUnitScreen.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the email for the selected conductor
        db.collection("users")
                .whereEqualTo("name", updatedConductor)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String conductorEmail = task.getResult().getDocuments().get(0).getString("email");

                        if (conductorEmail != null) {
                            // Update the document in Firestore
                            db.collection("assigns").document(documentId)
                                    .update("unitnumber", updatedUnitNumber,
                                            "platenumber", updatedPlatenumber,
                                            "driver", updatedDriver,
                                            "conductor", updatedConductor,
                                            "email", conductorEmail,
                                            "fromday", updatedFromday,
                                            "today", updatedToday,
                                            "fromtime", updatedFromtime,
                                            "totime", updatedTotime,
                                            "schedule", updateSchedule)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(AdminEditAssignedUnitScreen.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(AdminEditAssignedUnitScreen.this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(AdminEditAssignedUnitScreen.this, "Email not found for the selected conductor", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdminEditAssignedUnitScreen.this, "Failed to fetch email for the selected conductor", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AdminEditAssignedUnitScreen.this, "Error fetching conductor email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    // Set the selected item in a spinner based on the provided value
    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter == null || adapter.getCount() == 0) {
            // Adapter not ready, wait for it to populate
            spinner.post(() -> setSpinnerSelection(spinner, value));
            return;
        }

        int position = adapter.getPosition(value);
        if (position >= 0) {
            spinner.setSelection(position);
        } else {
            // Log for debugging
            Toast.makeText(this, "Value not found in spinner: " + value, Toast.LENGTH_SHORT).show();
        }
    }

}
