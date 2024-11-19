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

        // Initialize driverList and driverAdapter
        driverList = new ArrayList<>(); // Initialize the list here
        driverAdapter = new DriverAdapter(driverList); // Pass the list to the adapter

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

        // Set today's date in etDateAdded
        String todayDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
        etDateAdded.setText(todayDate);
        etDateAdded.setEnabled(false); // Disable editing of the date field

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

        String driverNamePattern = "^[a-zA-Z ]{2,}$";
        if (!driverName.matches(driverNamePattern)) {
            etDriverName.setError("Name must be at least 2 characters and contain only letters and spaces");
            return;
        }

        String phoneNumberPattern = "^09\\d{9}$";
        if (!driverContact.matches(phoneNumberPattern)) {
            etDriverContact.setError("Phone number must be 11 digits and start with '09'");
            return;
        }

        if (driverName.length() <2 || !driverName.matches("[a-zA-Z ]+")) {
            Toast.makeText(this, "Name must be at least 2 characters and contain only letters", Toast.LENGTH_SHORT).show();
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
                        startActivity(new Intent(AdminAddDriver.this, AdminManageDriver.class));
                    } else {
                        Toast.makeText(AdminAddDriver.this, "Error adding driver: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
