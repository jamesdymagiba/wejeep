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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AdminEditSchedule extends AppCompatActivity {

    private EditText etFromDay, etToDay, etFromTime, etToTime,etSchedule;
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
        etSchedule = findViewById(R.id.etSchedule);

        // Disable manual editing of etSchedule
        etSchedule.setEnabled(false);

        // Get document ID from Intent
        documentId = getIntent().getStringExtra("documentId");

        if (documentId != null && !documentId.isEmpty()) {
            // Fetch the document data from Firestore
            db.collection("schedules").document(documentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Populate the EditTexts with data from Firestore
                            String fromDay = documentSnapshot.getString("Fromday");
                            String toDay = documentSnapshot.getString("Today");
                            String fromTime = documentSnapshot.getString("Fromtime");
                            String toTime = documentSnapshot.getString("Totime");
                            String schedule = documentSnapshot.getString("schedule");

                            etFromDay.setText(fromDay != null ? fromDay : "");
                            etToDay.setText(toDay != null ? toDay : "");
                            etFromTime.setText(fromTime != null ? fromTime : "");
                            etToTime.setText(toTime != null ? toTime : "");
                            etSchedule.setText(schedule != null ? schedule : "");
                        } else {
                            Toast.makeText(this, "Document not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error loading schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Invalid document ID.", Toast.LENGTH_SHORT).show();
        }

        etFromDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Array of days of the week
                String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                // Variable to keep track of the selected day (default -1, no day selected)
                final int[] selectedDayFrom = {-1};

                // Create the SingleChoiceDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminEditSchedule.this);
                builder.setTitle("Select Day of the Week")
                        .setSingleChoiceItems(daysOfWeek, selectedDayFrom[0], (dialog, which) -> {
                            // Update selected day
                            selectedDayFrom[0] = which;
                        })
                        .setPositiveButton("OK", (dialog, which) -> {
                            if (selectedDayFrom[0] != -1) {
                                // Set the selected day in the EditText
                                etFromDay.setText(daysOfWeek[selectedDayFrom[0]]);

                                // Automatically calculate the "To Day"
                                int toDayIndex = (selectedDayFrom[0] + 2) % 7; // Add 2 days and wrap around the week
                                etToDay.setText(daysOfWeek[toDayIndex]); // Update etToDay with the calculated day
                            }
                        })
                        .setNegativeButton("Cancel", null);

                // Show the dialog
                builder.create().show();
            }
        });

        /*etToDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Array of days of the week
                String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                // Get the currently calculated "To Day"
                String selectedFromDay = etFromDay.getText().toString();
                if (selectedFromDay.isEmpty()) {
                    Toast.makeText(AdminAddScheduleScreen.this, "Please select From Day first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Find the index of the "From Day" in the daysOfWeek array
                int fromDayIndex = Arrays.asList(daysOfWeek).indexOf(selectedFromDay);

                // Calculate the "To Day" index
                int toDayIndex = (fromDayIndex + 2) % 7; // Add 2 days and wrap around the week

                // Pre-select the calculated "To Day"
                final int[] selectedDayTo = {toDayIndex};

                // Create the SingleChoiceDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminEditSchedule.this);
                builder.setTitle("Select Day of the Week")
                        .setSingleChoiceItems(daysOfWeek, selectedDayTo[0], (dialog, which) -> {
                            // Update selected day
                            selectedDayTo[0] = which;
                        })
                        .setPositiveButton("OK", (dialog, which) -> {
                            // Set the selected day in the EditText
                            etToDay.setText(daysOfWeek[selectedDayTo[0]]);
                        })
                        .setNegativeButton("Cancel", null);

                // Show the dialog
                builder.create().show();
            }
        });*/

        etFromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AdminEditSchedule.this,
                        (view, selectedHour, selectedMinute) -> {
                            // Convert 24-hour format to 12-hour format with AM/PM
                            String amPm;
                            if (selectedHour >= 12) {
                                amPm = "PM";
                                if (selectedHour > 12) selectedHour -= 12;
                            } else {
                                amPm = "AM";
                                if (selectedHour == 0) selectedHour = 12;
                            }
                            String fromTime = String.format("%02d:%02d %s", selectedHour, selectedMinute, amPm);
                            etFromTime.setText(fromTime);

                            // Calculate new time by adding 7 hours and 30 minutes
                            Calendar newCalendar = Calendar.getInstance();
                            newCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                            newCalendar.set(Calendar.MINUTE, selectedMinute);

                            // Add 7 hours and 30 minutes
                            newCalendar.add(Calendar.HOUR_OF_DAY, 7);
                            newCalendar.add(Calendar.MINUTE, 30);

                            // Get the adjusted time
                            int newHour24 = newCalendar.get(Calendar.HOUR_OF_DAY);
                            int newMinute = newCalendar.get(Calendar.MINUTE);

                            // Convert to 12-hour format and determine AM/PM
                            String newAmPm;
                            int newHour12;
                            if (newHour24 >= 12) {
                                newAmPm = "PM";
                                newHour12 = (newHour24 > 12) ? newHour24 - 12 : newHour24; // Convert to 12-hour format
                            } else {
                                newAmPm = "AM";
                                newHour12 = (newHour24 == 0) ? 12 : newHour24; // Handle midnight case
                            }

                            String toTime = String.format("%02d:%02d %s", newHour12, newMinute, newAmPm);
                            etToTime.setText(toTime);
                        }, hour, minute, false); // 'false' for AM/PM format
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
                            // Convert 24-hour format to 12-hour format with AM/PM
                            String amPm;
                            if (selectedHour >= 12) {
                                amPm = "PM";
                                if (selectedHour > 12) selectedHour -= 12;
                            } else {
                                amPm = "AM";
                                if (selectedHour == 0) selectedHour = 12;
                            }
                            String time = String.format("%02d:%02d %s", selectedHour, selectedMinute, amPm);
                            etToTime.setText(time);
                        }, hour, minute, false); // 'false' for AM/PM format
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
        String updateSchedules = etSchedule.getText().toString().trim();

        if (updatedFromDay.isEmpty() || updateSchedules.isEmpty() || updatedToDay.isEmpty() || updatedFromTime.isEmpty() || updateToTime.isEmpty()) {
            Toast.makeText(AdminEditSchedule.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the driver document in Firestore
        db.collection("schedules").document(documentId)
                .update("Fromday", updatedFromDay,
                        "Today", updatedToDay,
                        "Fromtime", updatedFromTime, "Totime" ,updateToTime, "schedule", updateSchedules)
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
