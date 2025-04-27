package edu.uga.cs.ridesharingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddRideActivity extends AppCompatActivity {

    private EditText editTextFrom, editTextTo;
    private TextView textViewDate, textViewTime, textViewValidation;
    private RadioGroup radioGroupType;
    private Button buttonSubmit, buttonPickDate, buttonPickTime;
    private Calendar selectedDateTime;
    private ImageView backArrow;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);

        mAuth = FirebaseAuth.getInstance();

        editTextFrom = findViewById(R.id.editTextFrom);
        editTextTo = findViewById(R.id.editTextTo);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTime = findViewById(R.id.textViewTime);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        buttonPickTime = findViewById(R.id.buttonPickTime);
        radioGroupType = findViewById(R.id.radioGroupType);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewValidation = findViewById(R.id.textViewValidation);
        backArrow = findViewById(R.id.backArrow);

        selectedDateTime = Calendar.getInstance();

        // Handle back arrow to MainActivity
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(AddRideActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        buttonPickDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateTimeLabels();
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
            datePicker.show();
        });

        buttonPickTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                selectedDateTime.set(Calendar.SECOND, 0);
                selectedDateTime.set(Calendar.MILLISECOND, 0);
                updateDateTimeLabels();
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
            timePicker.show();
        });

        buttonSubmit.setOnClickListener(v -> {
            if (!isDateTimeValid()) {
                textViewValidation.setText("Pick a time at least 15 minutes in the future!");
                textViewValidation.setVisibility(TextView.VISIBLE);
                return;
            }
            textViewValidation.setVisibility(TextView.GONE);

            String from = editTextFrom.getText().toString().trim();
            String to = editTextTo.getText().toString().trim();
            String date = textViewDate.getText().toString();
            String time = textViewTime.getText().toString();
            int selectedTypeId = radioGroupType.getCheckedRadioButtonId();

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = user.getUid();
            String driverId = ""; // initially empty
            boolean accepted = false;
            boolean riderCompleted = false;
            boolean driverCompleted = false;

            Ride newRide = new Ride(date, time, from, to, userId, driverId, accepted, riderCompleted, driverCompleted);

            if (selectedTypeId == R.id.radioOffer) {
                new StoreRideOfferTask().execute(newRide);
            } else if (selectedTypeId == R.id.radioRequest) {
                new StoreRideRequestTask().execute(newRide);
            } else {
                Toast.makeText(this, "Please select ride type", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Ride added!", Toast.LENGTH_SHORT).show();
            finish();
        });

        ImageView homeButton = findViewById(R.id.homeButton);
        ImageView profileButton = findViewById(R.id.profileButton);

        homeButton.setOnClickListener(v -> {
            startActivity(new Intent(AddRideActivity.this, MainActivity.class));
            finish();
        });

        profileButton.setOnClickListener(v -> {
            startActivity(new Intent(AddRideActivity.this, ProfileActivity.class));
            finish();
        });
    }

    private void updateDateTimeLabels() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        textViewDate.setText(dateFormat.format(selectedDateTime.getTime()));
        textViewTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private boolean isDateTimeValid() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 15);
        return selectedDateTime.after(now);
    }
}
