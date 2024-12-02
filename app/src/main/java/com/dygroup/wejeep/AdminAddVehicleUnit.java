package com.dygroup.wejeep;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminAddVehicleUnit extends AppCompatActivity {

    private EditText etVehicleModel;
    private Button btnAddVehicle, btnBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_vehicle_unit);

        etVehicleModel = findViewById(R.id.etVehicleModel);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
        btnBack = findViewById(R.id.btnBack);
        db = FirebaseFirestore.getInstance();

        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(AdminAddVehicleUnit.this, AdminAddUnitScreen.class));
        });

        btnAddVehicle.setOnClickListener(view -> addVehicleToFirestore());
    }

    private void addVehicleToFirestore() {
        String vehiclemodel = etVehicleModel.getText().toString().trim();

        // Validate input
        if (vehiclemodel.isEmpty()) {
            Toast.makeText(this, "Please fill in the vehicle model", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a data map for Firestore
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("vehiclemodel", vehiclemodel);

        // Add the vehicle data to the "vehicles" collection in Firestore
        db.collection("vehicles")
                .add(vehicle)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Vehicle added successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminAddVehicleUnit.this, AdminAddUnitScreen.class));
                    } else {
                        Toast.makeText(this, "Error adding vehicle: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
