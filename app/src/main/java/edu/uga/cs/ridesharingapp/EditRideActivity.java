package edu.uga.cs.ridesharingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditRideActivity extends AppCompatActivity {

    private EditText editTextFrom, editTextTo;
    private TextView textViewDate, textViewTime, textViewValidation;
    private Button buttonPickDate, buttonPickTime, buttonSubmit;
    private ImageView backArrow, homeButton, profileButton;
    private RadioGroup radioGroupType;

    private Calendar selectedDateTime;
    private FirebaseAuth mAuth;
    private Ride currentRide;
    private boolean isOffer; // true if ride is an offer, false if it's a request

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ride);

        // Initialize views
        editTextFrom = findViewById(R.id.editTextFrom);
        editTextTo = findViewById(R.id.editTextTo);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTime = findViewById(R.id.textViewTime);
        textViewValidation = findViewById(R.id.textViewValidation);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        buttonPickTime = findViewById(R.id.buttonPickTime);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        backArrow = findViewById(R.id.backArrow);
        homeButton = findViewById(R.id.homeButton);
        profileButton = findViewById(R.id.profileButton);
        radioGroupType = findViewById(R.id.radioGroupType);

        mAuth = FirebaseAuth.getInstance();
        selectedDateTime = Calendar.getInstance();

        // Handle back/home/profile buttons
        backArrow.setOnClickListener(v -> navigateToMainActivity());
        homeButton.setOnClickListener(v -> navigateToMainActivity());
        profileButton.setOnClickListener(v -> navigateToProfileActivity());

        // Get the ride object from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("ride")) {
            currentRide = (Ride) intent.getSerializableExtra("ride");
            populateFields();
        } else {
            Toast.makeText(this, "Error: No ride data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Handle date and time picking
        buttonPickDate.setOnClickListener(v -> showDatePickerDialog());
        buttonPickTime.setOnClickListener(v -> showTimePickerDialog());

        // Handle submit
        buttonSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void populateFields() {
        editTextFrom.setText(currentRide.getFromLocation());
        editTextTo.setText(currentRide.getToLocation());
        textViewDate.setText(currentRide.getDate());
        textViewTime.setText(currentRide.getTime());

        // Assume driverId ≠ null means it's an offer, else request
        String userId = mAuth.getCurrentUser().getUid();
        if (currentRide.getDriverId() != null && currentRide.getDriverId().equals(userId)) {
            radioGroupType.check(R.id.radioOffer);
            isOffer = true;
        } else if (currentRide.getRiderId() != null && currentRide.getRiderId().equals(userId)) {
            radioGroupType.check(R.id.radioRequest);
            isOffer = false;
        }
    }

    private void showDatePickerDialog() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateTimeLabels();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(now.getTimeInMillis());
        datePicker.show();
    }

    private void showTimePickerDialog() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            selectedDateTime.set(Calendar.SECOND, 0);
            selectedDateTime.set(Calendar.MILLISECOND, 0);
            updateDateTimeLabels();
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
        timePicker.show();
    }

    private void updateDateTimeLabels() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        textViewDate.setText(dateFormat.format(selectedDateTime.getTime()));
        textViewTime.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void handleSubmit() {
        String fromLocation = editTextFrom.getText().toString().trim();
        String toLocation = editTextTo.getText().toString().trim();
        String date = textViewDate.getText().toString();
        String time = textViewTime.getText().toString();
        int selectedTypeId = radioGroupType.getCheckedRadioButtonId();

        if (fromLocation.isEmpty() || toLocation.isEmpty() || date.isEmpty() || time.isEmpty() || selectedTypeId == -1) {
            Toast.makeText(this, "Please fill all fields and select ride type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isDateTimeValid()) {
            textViewValidation.setText("Pick a time at least 15 minutes in the future!");
            textViewValidation.setVisibility(TextView.VISIBLE);
            return;
        }
        textViewValidation.setVisibility(TextView.GONE);

        // Update the ride object
        currentRide.setFromLocation(fromLocation);
        currentRide.setToLocation(toLocation);
        currentRide.setDate(date);
        currentRide.setTime(time);

        // Determine offer or request based on radio selection
        if (selectedTypeId == R.id.radioOffer) {
            currentRide.setDriverId(mAuth.getCurrentUser().getUid());
            currentRide.setRiderId(null);
            isOffer = true;
        } else if (selectedTypeId == R.id.radioRequest) {
            currentRide.setRiderId(mAuth.getCurrentUser().getUid());
            currentRide.setDriverId(null);
            isOffer = false;
        }

        // Update to Firebase
        if (isOffer) {
            UpdateRideOfferTask task = new UpdateRideOfferTask(currentRide.getRideKey());
            task.execute(currentRide);
        } else {
            UpdateRideRequestTask task = new UpdateRideRequestTask(currentRide.getRideKey());
            task.execute(currentRide);
        }

        Toast.makeText(this, "Ride updated successfully.", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private boolean isDateTimeValid() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 15);
        return selectedDateTime.after(now);
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void navigateToProfileActivity() {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }
}
