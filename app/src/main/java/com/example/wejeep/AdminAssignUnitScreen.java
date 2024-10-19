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
    private Spinner spinnerFromtime, spinnerTotime, spinnerToday, spinnerFromday, spinnerDriver, spinnerConductor, spinnerPlatenumber, spinnerUnitnumber;
    private FirebaseFirestore db;
    private ArrayList<String> fromdayList = new ArrayList<>();
    private ArrayList<String> todayList = new ArrayList<>();
    private ArrayList<String> fromtimeList = new ArrayList<>();
    private ArrayList<String> totimeList = new ArrayList<>();
    private ArrayList<String> unitnumberList = new ArrayList<>();
    private ArrayList<String> conductorList = new ArrayList<>();
    private ArrayList<String> platenumberList = new ArrayList<>();
    private ArrayList<String> driverList = new ArrayList<>();
    private ArrayAdapter<String> fromtimeAdapter, totimeAdapter, fromdayAdapter, todayAdapter, driverAdapter, conductorAdapter, platenumberAdapter, unitnumberAdapter;
    private Button btnConfrim, btnBack;
    private String selectedDriver, selectedPlatenumber,selectedFromday,selectedToday,selectedFromtime,selectedTotime,selectedConductor,selectedUnitnumber;

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
        spinnerFromtime = findViewById(R.id.spinnerFromtime);
        spinnerTotime = findViewById(R.id.spinnerTotime);
        spinnerFromday = findViewById(R.id.spinnerFromday);
        spinnerToday = findViewById(R.id.spinnerToday);
        View btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Set up adapters for spinners
        fromtimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fromtimeList);
        fromtimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromtime.setAdapter(fromtimeAdapter);

        totimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, totimeList);
        totimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTotime.setAdapter(totimeAdapter);

        fromdayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fromdayList);
        fromdayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFromday.setAdapter(fromdayAdapter);

        todayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, todayList);
        todayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerToday.setAdapter(todayAdapter);

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

        // Fetch the schedule data from Firestore
        fetchSchedules();
        fetchDriver();
        fetchUnits();
        fetchPlatenumber();

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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        spinnerConductor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedConductor = conductorList.get(position); // Get the selected driver and store it
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        spinnerFromday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedFromday = fromdayList.get(position); // Get the selected driver and store it
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        spinnerToday.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedToday = todayList.get(position); // Get the selected driver and store it
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        spinnerFromtime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedFromtime = fromtimeList.get(position); // Get the selected driver and store it
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
        spinnerTotime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedTotime = totimeList.get(position); // Get the selected driver and store it
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });


        // Handle the button click to save the selected driver in the "assign" collection
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate if all required fields are selected
                if (selectedDriver != null && !selectedDriver.isEmpty() &&
                        selectedPlatenumber != null && !selectedPlatenumber.isEmpty() &&
                        selectedUnitnumber != null && !selectedUnitnumber.isEmpty() &&
                        //selectedConductor != null && !selectedConductor.isEmpty() &&
                        selectedFromday != null && !selectedFromday.isEmpty() &&
                        selectedToday != null && !selectedToday.isEmpty() &&
                        selectedFromtime != null && !selectedFromtime.isEmpty() &&
                        selectedTotime != null && !selectedTotime.isEmpty()) {

                    // Prepare the data to be saved in the 'assigns' collection
                    Map<String, Object> assignData = new HashMap<>();
                    assignData.put("unitnumber", selectedUnitnumber);
                    assignData.put("platenumber", selectedPlatenumber);
                    assignData.put("driver", selectedDriver);
                    //assignData.put("conductor", selectedConductor);
                    assignData.put("fromday", selectedFromday);
                    assignData.put("today", selectedToday);
                    assignData.put("fromtime", selectedFromtime);
                    assignData.put("totime", selectedTotime);

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


    private void fetchSchedules() {
        db.collection("schedules")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fromtimeList.clear();
                        totimeList.clear();
                        fromdayList.clear();
                        todayList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String fromtime = document.getString("Fromtime");
                            String totime = document.getString("Totime");
                            String fromday = document.getString("Fromday");
                            String today = document.getString("Today");

                            if (fromtime != null) {
                                fromtimeList.add(fromtime);
                            }

                            if (totime != null) {
                                totimeList.add(totime);
                            }

                            if (fromday != null) {
                                fromdayList.add(fromday);
                            }

                            if (today != null) {
                                todayList.add(today);
                            }
                        }

                        // Notify adapters that data has changed
                        fromtimeAdapter.notifyDataSetChanged();
                        totimeAdapter.notifyDataSetChanged();
                        fromdayAdapter.notifyDataSetChanged();
                        todayAdapter.notifyDataSetChanged();
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




}