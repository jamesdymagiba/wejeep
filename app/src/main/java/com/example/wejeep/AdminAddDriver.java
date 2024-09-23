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

public class AdminAddDriver extends AppCompatActivity {
    private ArrayList<DriverModel> driverList;
    private DriverAdapter driverAdapter;
    private EditText etDriverName, etDriverContact, etDateAdded;
    private Button btnAddDriver, btnBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_driver);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Reference UI elements
        etDriverName = findViewById(R.id.etdrivername);
        etDriverContact = findViewById(R.id.etdrivercontact);
        etDateAdded = findViewById(R.id.etdriverdateadded);
        btnAddDriver = findViewById(R.id.btnAddDriver);
        btnBack = findViewById(R.id.btnBack);

        // Handle back button click
        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(AdminAddDriver.this, AdminManageDriver.class));
        });

        // Handle date picker for "Date Added"
        etDateAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AdminAddDriver.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                            etDateAdded.setText(date);
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        // Handle Add Driver button click
        btnAddDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDriverToFirestore();
            }
        });
    }

    private void addDriverToFirestore() {
        String driverName = etDriverName.getText().toString().trim();
        String driverContact = etDriverContact.getText().toString().trim();
        String dateAdded = etDateAdded.getText().toString().trim();

        if (driverName.isEmpty() || driverContact.isEmpty() || dateAdded.isEmpty()) {
            Toast.makeText(AdminAddDriver.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a driver object
        Map<String, Object> driver = new HashMap<>();
        driver.put("name", driverName);
        driver.put("contact", driverContact);
        driver.put("dateAdded", dateAdded);

        // Add a new document in the "drivers" collection
        db.collection("drivers")
                .add(driver)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference docRef = task.getResult();
                        DriverModel newDriver = new DriverModel(driverName, driverContact, dateAdded, docRef.getId());
                        driverList.add(newDriver); // Add the new driver to your list
                        driverAdapter.notifyItemInserted(driverList.size() - 1); // Notify the adapter
                        Toast.makeText(AdminAddDriver.this, "Driver added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminAddDriver.this, "Error adding driver: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
