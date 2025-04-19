package edu.uga.cs.ridesharingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AddRideActivity extends AppCompatActivity {

    private EditText editTextFrom, editTextTo, editTextDate, editTextTime;
    private RadioGroup radioGroupType;
    private Button buttonSubmit;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);

        mAuth = FirebaseAuth.getInstance();

        editTextFrom = findViewById(R.id.editTextFrom);
        editTextTo = findViewById(R.id.editTextTo);
        editTextDate = findViewById(R.id.editTextDate);
        editTextTime = findViewById(R.id.editTextTime);
        radioGroupType = findViewById(R.id.radioGroupType);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(v -> {
            String from = editTextFrom.getText().toString().trim();
            String to = editTextTo.getText().toString().trim();
            String date = editTextDate.getText().toString().trim();
            String time = editTextTime.getText().toString().trim();
            int selectedTypeId = radioGroupType.getCheckedRadioButtonId();

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = user.getUid();
            String riderName = user.getDisplayName() != null ? user.getDisplayName() : "Unknown";

            Ride newRide = new Ride(from, to, date, time, userId, riderName);

            if (selectedTypeId == R.id.radioOffer) {
                new StoreRideOfferTask().execute(newRide);
            } else if (selectedTypeId == R.id.radioRequest) {
                new StoreRideRequestTask().execute(newRide);
            } else {
                Toast.makeText(this, "Please select ride type", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, "Ride added!", Toast.LENGTH_SHORT).show();
            finish(); // Return to previous screen
        });
    }
}
