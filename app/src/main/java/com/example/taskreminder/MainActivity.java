package com.example.taskreminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    EditText etTask;
    Button btnDate, btnTime, btnSetReminder;
    TextView tvDate, tvTime;

    Calendar calendar = Calendar.getInstance();

    int selectedYear;
    int selectedMonth;
    int selectedDay;
    int selectedHour;
    int selectedMinute;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        etTask = findViewById(R.id.etTask);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });
        btnSetReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setReminder();
            }
        });

    }

    @SuppressLint("ScheduleExactAlarm")
    private void setReminder() {
        String task = etTask.getText().toString().trim();

        if (task.isEmpty()) {
            etTask.setError("Enter task");
            return;
        }

        if (tvDate.getText().toString().equals("Date not selected")) {
            Toast.makeText(
                    this,
                    "Please select date",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (tvTime.getText().toString().equals("Time not selected")) {
            Toast.makeText(
                    this,
                    "Please select time",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Calendar reminderCalendar = Calendar.getInstance();

        reminderCalendar.set(
                selectedYear,
                selectedMonth,
                selectedDay,
                selectedHour,
                selectedMinute,
                0
        );

        if (reminderCalendar.getTimeInMillis()
                <= System.currentTimeMillis()) {

            Toast.makeText(
                    this,
                    "Please select a future date and time",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        Intent intent = new Intent(
                MainActivity.this,
                ReminderReceiver.class
        );

        intent.putExtra("task", task);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        (int) System.currentTimeMillis(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderCalendar.getTimeInMillis(),
                    pendingIntent
            );

            Toast.makeText(
                    this,
                    "Reminder set successfully!",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void showTimePicker() {
        Calendar currentTime = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {

                    selectedHour = hourOfDay;
                    selectedMinute = minute;

                    String time = String.format(
                            "%02d:%02d",
                            hourOfDay,
                            minute
                    );

                    tvTime.setText(time);

                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void showDatePicker() {
        Calendar currentDate = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = dayOfMonth;

                    tvDate.setText(
                            dayOfMonth + "/" +
                                    (month + 1) + "/" +
                                    year
                    );

                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();

    }
}