package com.example.wejeep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminAssignUnitScreen extends AppCompatActivity {
    private AssignAdapter assignAdapter;
    private ArrayList<AssignModel> assignList;
    private Spinner spinnerDriver, spinnerConductor, spinnerPlatenumber, spinnerUnitnumber, spinnerSchedule;
    private EditText EditTextFromtime,EditTextTotime,EditTextFromday,EditTextToday;
    private FirebaseFirestore db;
    private ArrayList<String> scheduleList = new ArrayList<>();
    private ArrayList<String> unitnumberList = new ArrayList<>();
    private ArrayList<String> conductorList = new ArrayList<>();
    private ArrayList<String> platenumberList = new ArrayList<>();
    private ArrayList<String> driverList = new ArrayList<>();
    private ArrayAdapter<String>  driverAdapter, conductorAdapter, platenumberAdapter, unitnumberAdapter, scheduleAdapter;
    private Button btnConfirm, btnBack;
    private String selectedDriver, selectedPlatenumber,selectedConductor,selectedUnitnumber, selectedConductorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_assign_unit_screen);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();


        // Initialize the spinners
        spinnerConductor = findViewById(R.id.spinnerConductor);
        spinnerDriver = findViewById(R.id.spinnerDriver);
        spinnerPlatenumber = findViewById(R.id.spinnerPlatenumber);
        spinnerUnitnumber = findViewById(R.id.spinnerUnitnumber);
        spinnerSchedule = findViewById(R.id.spinnerSchedule);
        EditTextFromtime = findViewById(R.id.etFromtime);
        EditTextTotime = findViewById(R.id.etTotime);
        EditTextFromday = findViewById(R.id.etFromday);
        EditTextToday = findViewById(R.id.etToday);
        View btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        EditTextFromday.setEnabled(false);
        EditTextToday.setEnabled(false);
        EditTextFromtime.setEnabled(false);
        EditTextTotime.setEnabled(false);
        spinnerPlatenumber.setEnabled(false);

        // Set up adapters for spinners

        driverAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, driverList);
        driverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDriver.setAdapter(driverAdapter);

        conductorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, conductorList);
        conductorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerConductor.setAdapter(conductorAdapter);

        platenumberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, platenumberList);
        platenumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlatenumber.setAdapter(platenumberAdapter);

        unitnumberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitnumberList);
        unitnumberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnitnumber.setAdapter(unitnumberAdapter);

        scheduleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scheduleList);
        scheduleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchedule.setAdapter(scheduleAdapter);

        // Fetch the schedule data from Firestore
        fetchSchedules();
        fetchDriver();
        fetchUnits();
        fetchPlatenumber();
        fetchConductor();
        fetchSchedules();
        fetchAssignedData();

        spinnerDriver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedDriver = driverList.get(position); // Get the selected driver and store it
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        spinnerPlatenumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedPlatenumber = platenumberList.get(position); // Get the selected driver and store it
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        spinnerUnitnumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedUnitnumber = unitnumberList.get(position); // Get the selected driver and store it
                db.collection("units")
                        .whereEqualTo("unitNumber", selectedUnitnumber)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                // Retrieve the plateNumber from the document
                                String plateNumber = task.getResult().getDocuments().get(0).getString("plateNumber");
                                if (plateNumber != null) {
                                    // Update spinnerPlatenumber to show this plate number
                                    platenumberList.clear();
                                    platenumberList.add(plateNumber);
                                    platenumberAdapter.notifyDataSetChanged();

                                    // Set the selected plate number
                                    selectedPlatenumber = plateNumber;
                                    spinnerPlatenumber.setSelection(0); // Set the first (and only) item as selected
                                }
                            } else {
                                Toast.makeText(AdminAssignUnitScreen.this, "Failed to fetch plate number", Toast.LENGTH_SHORT).show();
                            }
                        });


            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        // Modify the spinnerConductor listener
        spinnerConductor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedConductor = conductorList.get(position); // Get the selected conductor name

                // Query Firestore to fetch the email for the selected conductor
                db.collection("users")
                        .whereEqualTo("name", selectedConductor)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                // Fetch the email field from the document
                                selectedConductorEmail = task.getResult().getDocuments().get(0).getString("email");
                            } else {
                                selectedConductorEmail = null; // Reset email if not found
                                Toast.makeText(AdminAssignUnitScreen.this, "Failed to fetch email for the selected conductor", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Reset the selected conductor's email if nothing is selected
                selectedConductorEmail = null;
            }
        });

        spinnerSchedule.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected schedule value
                String selectedSchedule = scheduleList.get(position);

                // Query Firestore for the document with the selected schedule
                db.collection("schedules")
                        .whereEqualTo("schedule", selectedSchedule)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                // Retrieve the Fromday from the first matched document
                                String fromDay = task.getResult().getDocuments().get(0).getString("Fromday");
                                String toDay = task.getResult().getDocuments().get(0).getString("Today");
                                String fromTime = task.getResult().getDocuments().get(0).getString("Fromtime");
                                String toTime = task.getResult().getDocuments().get(0).getString("Totime");

                                if (fromDay != null) {
                                    // Set the Fromday in EditTextFromday
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
                                }

                            } else {
                                // Clear EditTextFromday if no matching document is found
                                EditTextFromday.setText("");
                                EditTextToday.setText("");
                                EditTextFromtime.setText("");
                                EditTextTotime.setText("");
                                Toast.makeText(AdminAssignUnitScreen.this, "Failed to fetch Fromday", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Clear EditTextFromday if nothing is selected
                EditTextFromday.setText("");
            }
        });



        // Handle the button click to save the selected driver in the "assign" collection
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the value from EditText fields
                String selectedFromDay = EditTextFromday.getText().toString().trim();
                String selectedToDay = EditTextToday.getText().toString().trim();
                String selectedToTime = EditTextTotime.getText().toString().trim();
                String selectedFromTime = EditTextFromtime.getText().toString().trim();
                String selectedSchedule = (String) spinnerSchedule.getSelectedItem(); // Get selected schedule from spinner

                // Validate if all required fields are selected
                if (selectedDriver != null && !selectedDriver.isEmpty() &&
                        selectedPlatenumber != null && !selectedPlatenumber.isEmpty() &&
                        selectedUnitnumber != null && !selectedUnitnumber.isEmpty() &&
                        selectedConductor != null && !selectedConductor.isEmpty() &&
                        selectedConductorEmail != null && !selectedConductorEmail.isEmpty() &&
                        selectedToDay != null && !selectedToDay.isEmpty() &&
                        selectedToTime != null && !selectedToTime.isEmpty() &&
                        selectedFromTime != null && !selectedFromTime.isEmpty() &&
                        selectedFromDay != null && !selectedFromDay.isEmpty() &&
                        selectedSchedule != null && !selectedSchedule.isEmpty()) { // Check if schedule is selected

                    // Fetch the vehicleModel from the "units" collection
                    db.collection("units")
                            .whereEqualTo("unitNumber", selectedUnitnumber)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    // Retrieve the vehicleModel from the document
                                    String vehicleModel = task.getResult().getDocuments().get(0).getString("vehicleModel");

                                    if (vehicleModel != null) {
                                        // Prepare the data to be saved in the 'assigns' collection
                                        Map<String, Object> assignData = new HashMap<>();
                                        assignData.put("unitnumber", selectedUnitnumber);
                                        assignData.put("platenumber", selectedPlatenumber);
                                        assignData.put("driver", selectedDriver);
                                        assignData.put("conductor", selectedConductor);
                                        assignData.put("email", selectedConductorEmail);
                                        assignData.put("fromday", selectedFromDay);
                                        assignData.put("today", selectedToDay);
                                        assignData.put("fromtime", selectedFromTime);
                                        assignData.put("totime", selectedToTime);
                                        assignData.put("vehiclemodel", vehicleModel); // Include the vehicle model
                                        assignData.put("schedule", selectedSchedule); // Include the schedule

                                        // Save the combined data in one document in the "assigns" collection
                                        db.collection("assigns")
                                                .add(assignData)
                                                .addOnSuccessListener(documentReference -> {
                                                    // Data saved successfully
                                                    Toast.makeText(AdminAssignUnitScreen.this, "Driver and Plate number assigned successfully", Toast.LENGTH_SHORT).show();

                                                    // Navigate to AdminManageActiveUnitList activity after success
                                                    Intent intent = new Intent(AdminAssignUnitScreen.this, AdminManageActiveUnitList.class);
                                                    startActivity(intent);
                                                    finish(); // Optionally, finish the current activity to prevent going back
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Failed to save data
                                                    Toast.makeText(AdminAssignUnitScreen.this, "Failed to assign driver and plate number", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        // Handle the case where vehicleModel is null
                                        Toast.makeText(AdminAssignUnitScreen.this, "Vehicle model not found for the selected unit", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Handle failure to fetch data from "units" collection
                                    Toast.makeText(AdminAssignUnitScreen.this, "Failed to fetch vehicle model", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Some fields are missing, notify the user
                    Toast.makeText(AdminAssignUnitScreen.this, "Please select all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });




        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminAssignUnitScreen.this, AdminManageActiveUnitList.class));
            }
        });


    }

    private void fetchAssignedData() {
        db.collection("assigns")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> assignedDrivers = new ArrayList<>();
                        ArrayList<String> assignedPlateNumbers = new ArrayList<>();
                        ArrayList<String> assignedUnitNumbers = new ArrayList<>();
                        ArrayList<String> assignedConductors = new ArrayList<>();
                        ArrayList<String> assignedSchedules = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            assignedDrivers.add(document.getString("driver"));
                            assignedPlateNumbers.add(document.getString("platenumber"));
                            assignedUnitNumbers.add(document.getString("unitnumber"));
                            assignedConductors.add(document.getString("conductor"));
                            assignedSchedules.add(document.getString("schedule"));
                        }

                        // Now fetch and filter data for spinners
                        fetchFilteredData(assignedDrivers, assignedPlateNumbers, assignedUnitNumbers, assignedConductors, assignedSchedules);
                    } else {
                        Toast.makeText(this, "Failed to fetch assigned data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchFilteredData(
            ArrayList<String> assignedDrivers,
            ArrayList<String> assignedPlateNumbers,
            ArrayList<String> assignedUnitNumbers,
            ArrayList<String> assignedConductors,
            ArrayList<String> assignedSchedules
    ) {
        // Fetch drivers
        db.collection("drivers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        driverList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String driver = document.getString("name");
                            if (driver != null && !assignedDrivers.contains(driver)) {
                                driverList.add(driver);
                            }
                        }
                        driverAdapter.notifyDataSetChanged();
                    }
                });

        // Fetch plate numbers
        db.collection("units")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        platenumberList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String plateNumber = document.getString("plateNumber");
                            if (plateNumber != null) {
                                // Count occurrences in the "assigns" collection
                                db.collection("assigns")
                                        .whereEqualTo("platenumber", plateNumber)
                                        .get()
                                        .addOnCompleteListener(assignTask -> {
                                            if (assignTask.isSuccessful() && assignTask.getResult().size() < 2) {
                                                platenumberList.add(plateNumber);
                                                platenumberAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    }
                });

        // Fetch unit numbers
        db.collection("units")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        unitnumberList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String unitNumber = document.getString("unitNumber");
                            if (unitNumber != null) {
                                // Count occurrences in the "assigns" collection
                                db.collection("assigns")
                                        .whereEqualTo("unitnumber", unitNumber)
                                        .get()
                                        .addOnCompleteListener(assignTask -> {
                                            if (assignTask.isSuccessful() && assignTask.getResult().size() < 2) {
                                                unitnumberList.add(unitNumber);
                                                unitnumberAdapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                    }
                });

        // Fetch conductors
        db.collection("users")
                .whereEqualTo("role", "pao")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        conductorList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String conductor = document.getString("name");
                            if (conductor != null && !assignedConductors.contains(conductor)) {
                                conductorList.add(conductor);
                            }
                        }
                        conductorAdapter.notifyDataSetChanged();
                    }
                });

        // Fetch schedules
        db.collection("schedules")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        scheduleList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String schedule = document.getString("schedule");
                            if (schedule != null && !assignedSchedules.contains(schedule)) {
                                scheduleList.add(schedule);
                            }
                        }
                        scheduleAdapter.notifyDataSetChanged();
                    }
                });
    }




    private void fetchSchedules() {
        db.collection("schedules")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        scheduleList.clear(); // Clear the previous list to avoid duplicates

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String schedule = document.getString("schedule");

                            if (schedule != null) {
                                scheduleList.add(schedule); // Add only the "schedule" field to the list
                            }
                        }

                        // Notify the adapter that data has changed
                        scheduleAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(AdminAssignUnitScreen.this, "Failed to fetch schedules", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void fetchUnits() {
        db.collection("units")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        unitnumberList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String unitnumber = document.getString("unitNumber");
                            if (unitnumber != null) {
                                unitnumberList.add(unitnumber);  // Add plate number to the list
                            }
                        }
                        unitnumberAdapter.notifyDataSetChanged();  // Notify adapter that the data has changed
                    }
                });

        // Handle selection of a plate number
    }

    private void fetchPlatenumber(){
        db.collection("units")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        platenumberList.clear(); // Clear the previous list to avoid duplicates

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String platenumber = document.getString("plateNumber");
                            if (platenumber != null) {
                                platenumberList.add(platenumber); // Add driver names to the correct list
                            }
                        }
                        platenumberAdapter.notifyDataSetChanged(); // Notify the adapter to update the spinner
                    }
                });

    }

    private void fetchDriver() {
        db.collection("drivers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        driverList.clear(); // Clear the previous list to avoid duplicates

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String driver = document.getString("name");
                            if (driver != null) {
                                driverList.add(driver); // Add driver names to the correct list
                            }
                        }
                        driverAdapter.notifyDataSetChanged(); // Notify the adapter to update the spinner
                    }
                });
    }

    private void fetchConductor() {
        db.collection("users")
                .whereEqualTo("role", "pao")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        conductorList.clear(); // Clear previous data to avoid duplicates

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String conductorName = document.getString("name");
                            if (conductorName != null) {
                                conductorList.add(conductorName); // Add the conductor's name to the list
                            }
                        }
                        conductorAdapter.notifyDataSetChanged(); // Notify the adapter to update the spinner
                    } else {
                        Toast.makeText(AdminAssignUnitScreen.this, "Failed to fetch conductors", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}