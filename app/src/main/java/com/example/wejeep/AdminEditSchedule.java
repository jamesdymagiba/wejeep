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

                // Variable to keep track of the selected day (default -1, no day selected)
                final int[] selectedDay = {-1};

                // Create the SingleChoiceDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminEditSchedule.this);
                builder.setTitle("Select Day of the Week")
                        .setSingleChoiceItems(daysOfWeek, selectedDay[0], (dialog, which) -> {
                            // Update selected day
                            selectedDay[0] = which;
                        })
                        .setPositiveButton("OK", (dialog, which) -> {
                            if (selectedDay[0] != -1) {
                                // Set the selected day in the EditText
                                etFromDay.setText(daysOfWeek[selectedDay[0]]);
                            }
                        })
                        .setNegativeButton("Cancel", null);

                // Show the dialog
                builder.create().show();
            }
        });



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
                            }
                        })
                        .setNegativeButton("Cancel", null);

                // Show the dialog
                builder.create().show();
            }
        });

// etToDay OnClickListener
        etToDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Original array of days of the week
                String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                // Variable to keep track of the selected day in etFromDay
                final String selectedFromDay = etFromDay.getText().toString();

                // Filter out the selectedFromDay from daysOfWeek
                List<String> availableDays = new ArrayList<>(Arrays.asList(daysOfWeek));
                availableDays.remove(selectedFromDay);

                // Convert the List back to an array
                String[] filteredDaysOfWeek = availableDays.toArray(new String[0]);

                // Variable to keep track of the selected day for etToDay (default -1, no day selected)
                final int[] selectedDayTo = {-1};

                // Create the SingleChoiceDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminEditSchedule.this);
                builder.setTitle("Select Day of the Week")
                        .setSingleChoiceItems(filteredDaysOfWeek, selectedDayTo[0], (dialog, which) -> {
                            // Update selected day
                            selectedDayTo[0] = which;
                        })
                        .setPositiveButton("OK", (dialog, which) -> {
                            if (selectedDayTo[0] != -1) {
                                // Set the selected day in the EditText if a valid day was selected
                                etToDay.setText(filteredDaysOfWeek[selectedDayTo[0]]);
                            }
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
                            etFromTime.setText(time);
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
