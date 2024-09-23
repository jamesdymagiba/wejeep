package com.example.wejeep;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class AdminAddDriver extends AppCompatActivity {

    private EditText etDateAdded;
    Button btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_driver);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(view -> {
            startActivity(new Intent(AdminAddDriver.this, AdminManageDriver.class));
        });

        etDateAdded = findViewById(R.id.etdriverdateadded);

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
    }
}
