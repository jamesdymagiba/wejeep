package com.example.wejeep;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

public class AdminAddScheduleScreen extends AppCompatActivity {
    private ArrayList<ScheduleModel> scheduleList;
    private ScheduleAdapter scheduleAdapter;
    private EditText etFromDay, etToDay, etFromTime, etToTime;
    private Button btnAddSchedule, btnBack;
    private FirebaseFirestore db;

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
        btnAddSchedule = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Handle back button click
        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(AdminAddScheduleScreen.this, AdminManageScheduleScreen.class));
        });
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
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminAddScheduleScreen.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(AdminAddScheduleScreen.this);
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(AdminAddScheduleScreen.this,
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

                TimePickerDialog timePickerDialog = new TimePickerDialog(AdminAddScheduleScreen.this,
                        (view, selectedHour, selectedMinute) -> {
                            String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                            etToTime.setText(time);
                        }, hour, minute, true); // 'true' for 24-hour format, set 'false' for AM/PM format
                timePickerDialog.show();
            }
        });


        // Handle Add Driver button click
        btnAddSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addScheduleToFirestore();
                startActivity(new Intent(AdminAddScheduleScreen.this, AdminManageScheduleScreen.class));
            }
        });
    }

    private void addScheduleToFirestore() {
        String fromDay = etFromDay.getText().toString().trim();
        String toDay = etToDay.getText().toString().trim();
        String fromTime = etFromTime.getText().toString().trim();
        String toTime = etToTime.getText().toString().trim();

        if (fromDay.isEmpty() || toDay.isEmpty() || fromTime.isEmpty() || toTime.isEmpty() ) {
            Toast.makeText(AdminAddScheduleScreen.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a driver object
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("Fromday", fromDay);
        schedule.put("Today", toDay);
        schedule.put("Fromtime", fromTime);
        schedule.put("Totime" , toTime);

        // Add a new document in the "drivers" collection
        db.collection("schedules")
                .add(schedule)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentReference docRef = task.getResult();
                        ScheduleModel newSchedule = new ScheduleModel(fromDay, toDay, fromTime, toTime, docRef.getId());
                        scheduleList.add(newSchedule); // Add the new driver to your list
                        scheduleAdapter.notifyItemInserted(scheduleList.size() - 1); // Notify the adapter
                        Toast.makeText(AdminAddScheduleScreen.this, "Schedule added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminAddScheduleScreen.this, "Error adding schedule: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
