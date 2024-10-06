package com.example.wejeep;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;

public class AdminEditSchedule extends AppCompatActivity {

    private EditText etFromDay, etToDay, etFromTime, etToTime;
    private Button btnConfirm, btnBack;
    private FirebaseFirestore db;
    private String documentId; // To store the document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_schedule);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Reference UI elements
        etFromDay = findViewById(R.id.etFromDay);
        etToDay = findViewById(R.id.etToDay);
        etFromTime = findViewById(R.id.etFromTime);
        etToTime = findViewById(R.id.etToTime);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Get driver data from Intent
        documentId = getIntent().getStringExtra("documentId");
        String fromDay = getIntent().getStringExtra("fromDay");
        String toDay = getIntent().getStringExtra("toDay");
        String fromTime = getIntent().getStringExtra("fromTime");
        String toTime = getIntent().getStringExtra("toTime");

        // Pre-fill the EditTexts with received data
        etFromDay.setText(fromDay);
        etToDay.setText(toDay);
        etFromTime.setText(fromTime);
        etToTime.setText(toTime);

        etFromDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Array of days of the week
                String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                // Boolean array for initial selected state (all false)
                boolean[] selectedDays = new boolean[daysOfWeek.length];

                // List to keep track of selected days
                ArrayList<String> selectedDaysList = new ArrayList<>();

                // Create the MultiChoiceDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminEditSchedule.this);
                builder.setTitle("Select Days of the Week")
                        .setMultiChoiceItems(daysOfWeek, selectedDays, (dialog, which, isChecked) -> {
                            if (isChecked) {
                                // Add selected day to the list
                                selectedDaysList.add(daysOfWeek[which]);
                            } else {
                                // Remove unselected day from the list
                                selectedDaysList.remove(daysOfWeek[which]);
                            }
                        })
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Join the selected days into a comma-separated string
                            String selectedDaysString = String.join(", ", selectedDaysList);
                            etFromDay.setText(selectedDaysString);
                        })
                        .setNegativeButton("Cancel", null);

                // Show the dialog
                builder.create().show();
            }
        });


        etToDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Array of days of the week
                String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                // Boolean array for initial selected state (all false)
                boolean[] selectedDays = new boolean[daysOfWeek.length];

                // List to keep track of selected days
                ArrayList<String> selectedDaysList = new ArrayList<>();

                // Create the MultiChoiceDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminEditSchedule.this);
                builder.setTitle("Select Days of the Week")
                        .setMultiChoiceItems(daysOfWeek, selectedDays, (dialog, which, isChecked) -> {
                            if (isChecked) {
                                // Add selected day to the list
                                selectedDaysList.add(daysOfWeek[which]);
                            } else {
                                // Remove unselected day from the list
                                selectedDaysList.remove(daysOfWeek[which]);
                            }
                        })
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Join the selected days into a comma-separated string
                            String selectedDaysString = String.join(", ", selectedDaysList);
                            etToDay.setText(selectedDaysString);
                        })
                        .setNegativeButton("Cancel", null);

                // Show the dialog
                builder.create().show();
            }
        });


        etFromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AdminEditSchedule.this,
                        (view, selectedHour, selectedMinute) -> {
                            String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                            etFromTime.setText(time);
                        }, hour, minute, true); // 'true' for 24-hour format, set 'false' for AM/PM format
                timePickerDialog.show();
            }
        });

        etToTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AdminEditSchedule.this,
                        (view, selectedHour, selectedMinute) -> {
                            String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                            etToTime.setText(time);
                        }, hour, minute, true); // 'true' for 24-hour format, set 'false' for AM/PM format
                timePickerDialog.show();
            }
        });

        // Handle Apply Changes button click
        btnConfirm.setOnClickListener(v -> {
            applyChanges();
        });

        // Handle Back button click
        btnBack.setOnClickListener(v -> {
            finish(); // Go back to the previous activity
        });

    }

    private void applyChanges() {
        String updatedFromDay = etFromDay.getText().toString().trim();
        String updatedToDay = etToDay.getText().toString().trim();
        String updatedFromTime = etFromTime.getText().toString().trim();
        String updateToTime = etToTime.getText().toString().trim();

        if (updatedFromDay.isEmpty() || updatedToDay.isEmpty() || updatedFromTime.isEmpty() || updateToTime.isEmpty()) {
            Toast.makeText(AdminEditSchedule.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the driver document in Firestore
        db.collection("schedules").document(documentId)
                .update("Fromday", updatedFromDay,
                        "Today", updatedToDay,
                        "Fromtime", updatedFromTime, "Totime" ,updateToTime)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AdminEditSchedule.this, "Schedule updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish(); // Go back after successful update
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminEditSchedule.this, "Error updating schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
