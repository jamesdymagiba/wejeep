package com.example.wejeep;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddScheduleScreen extends AppCompatActivity {
    private ArrayList<ScheduleModel> scheduleList;
    private ScheduleAdapter scheduleAdapter;
    private EditText etFromDay, etToDay ,etFromTime, etToTime, etSchedule;
    private Button btnAddSchedule, btnBack;
    private FirebaseFirestore db;


    private void checkAndSetScheduleValue() {
        db.collection("schedules")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> existingSchedules = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String schedule = document.getString("schedule");
                            if (schedule != null && schedule.startsWith("schedule ")) {
                                existingSchedules.add(schedule);
                            }
                        }

                        // Determine the next available schedule
                        String nextSchedule = findNextAvailableSchedule(existingSchedules);
                        etSchedule.setText(nextSchedule);
                    } else {
                        Toast.makeText(AdminAddScheduleScreen.this, "Error fetching schedules: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String findNextAvailableSchedule(List<String> existingSchedules) {
        boolean[] scheduleFlags = new boolean[101]; // Array to mark which schedules exist (1-100)
        Arrays.fill(scheduleFlags, false);

        for (String schedule : existingSchedules) {
            try {
                int scheduleNumber = Integer.parseInt(schedule.replace("schedule ", ""));
                if (scheduleNumber >= 1 && scheduleNumber <= 100) {
                    scheduleFlags[scheduleNumber] = true; // Mark the existing schedule
                }
            } catch (NumberFormatException e) {
                // Ignore invalid formats
            }
        }

        // Find the first missing schedule
        for (int i = 1; i <= 100; i++) {
            if (!scheduleFlags[i]) {
                return "schedule " + i;
            }
        }

        return "schedule 101"; // If all 1-100 schedules exist, suggest schedule 101
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_schedule_screen);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize driverList and driverAdapter
        scheduleList = new ArrayList<>(); // Initialize the list here
        scheduleAdapter = new ScheduleAdapter(scheduleList); // Pass the list to the adapter

        // Reference UI elements
        etFromDay = findViewById(R.id.etfromday);
        etToDay = findViewById(R.id.ettoday);
        etFromTime = findViewById(R.id.etfromtime);
        etToTime = findViewById(R.id.ettotime);
        etSchedule = findViewById(R.id.etSchedule);
        btnAddSchedule = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        etSchedule.setEnabled(false);


        checkAndSetScheduleValue();

        // Handle back button click
        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(AdminAddScheduleScreen.this, AdminManageScheduleScreen.class));
        });

        // etFromDay OnClickListener
        // etFromDay OnClickListener
        etFromDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Array of days of the week
                String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                // Variable to keep track of the selected day (default -1, no day selected)
                final int[] selectedDayFrom = {-1};

                // Create the SingleChoiceDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminAddScheduleScreen.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminAddScheduleScreen.this);
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(AdminAddScheduleScreen.this,
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(AdminAddScheduleScreen.this,
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



        // Handle Add Driver button click
        btnAddSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromDay = etFromDay.getText().toString().trim();
                String toDay = etToDay.getText().toString().trim();
                String fromTime = etFromTime.getText().toString().trim();
                String toTime = etToTime.getText().toString().trim();

                // Check if all fields are filled
                if (fromDay.isEmpty() || toDay.isEmpty() || fromTime.isEmpty() || toTime.isEmpty()) {
                    // Display an error message if any field is empty
                    Toast.makeText(AdminAddScheduleScreen.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    // If all fields are filled, add schedule to Firestore and start the next activity
                    addScheduleToFirestore();
                    startActivity(new Intent(AdminAddScheduleScreen.this, AdminManageScheduleScreen.class));
                }
            }
        });

    }

    private void addScheduleToFirestore() {
        String fromDay = etFromDay.getText().toString().trim();
        String toDay = etToDay.getText().toString().trim();
        String fromTime = etFromTime.getText().toString().trim();
        String toTime = etToTime.getText().toString().trim();
        String Schedules = etSchedule.getText().toString().trim();

        if (fromDay.isEmpty() || toDay.isEmpty() || fromTime.isEmpty() || Schedules.isEmpty() || toTime.isEmpty() ) {
            Toast.makeText(AdminAddScheduleScreen.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a driver object
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("Fromday", fromDay);
        schedule.put("Today", toDay);
        schedule.put("Fromtime", fromTime);
        schedule.put("Totime" , toTime);
        schedule.put("schedule", Schedules);

        // Add a new document in the "drivers" collection
        db.collection("schedules")
                .add(schedule)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference docRef = task.getResult();
                        ScheduleModel newSchedule = new ScheduleModel(fromDay, toDay, fromTime, toTime,Schedules, docRef.getId());
                        scheduleList.add(newSchedule); // Add the new driver to your list
                        scheduleAdapter.notifyItemInserted(scheduleList.size() - 1); // Notify the adapter
                        Toast.makeText(AdminAddScheduleScreen.this, "Schedule added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminAddScheduleScreen.this, "Error adding schedule: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
