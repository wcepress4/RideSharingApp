package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, pointsTextView;
    private Button backButton, logoutButton;
    private RecyclerView completedRidesRecyclerView;
    private RideAdapter rideAdapter; // Adapter to display completed rides

    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        backButton = findViewById(R.id.backButton);
        logoutButton = findViewById(R.id.logoutButton);
        completedRidesRecyclerView = findViewById(R.id.completedRidesRecyclerView); // NEW RecyclerView for completed rides

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = user.getUid();  // Store the current user ID

        // Initialize RecyclerView
        completedRidesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Handle back button
        backButton.setOnClickListener(v -> finish());

        // Handle logout button
        logoutButton.setOnClickListener(v -> {
            LogoutUserTask logoutTask = new LogoutUserTask(ProfileActivity.this, () -> {
                // After logout, go back to LoginActivity
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
            logoutTask.execute();
        });

        // Fetch profile info asynchronously
        new RetrieveProfile(result -> {
            nameTextView.setText("Name: " + result.name);
            emailTextView.setText("Email: " + result.email);
            pointsTextView.setText("Points: " + result.points);
        }).fetchProfile();

        // Fetch completed rides for the user
        loadCompletedRides();
    }

    private void loadCompletedRides() {
        RetrieveFilteredRidesTask task = new RetrieveFilteredRidesTask(
                new RetrieveFilteredRidesTask.RideDataCallback() {
                    @Override
                    public void onRidesRetrieved(List<Ride> rides) {
                        // Filter completed rides only
                        List<Ride> completedRides = filterCompletedRides(rides);
                        displayCompletedRides(completedRides);
                    }
                },
                RetrieveFilteredRidesTask.FilterType.COMPLETED_BY_USER,
                currentUserId,  // Pass current user ID
                false  // We don't need to specify offer or request for this task
        );
        task.fetchData(); // Fetch completed rides from Firebase
    }

    private List<Ride> filterCompletedRides(List<Ride> rides) {
        List<Ride> completedRides = new ArrayList<>();
        for (Ride ride : rides) {
            if (ride.getCompleted()) {
                completedRides.add(ride);
            }
        }
        return completedRides;
    }

    private void displayCompletedRides(List<Ride> completedRides) {
        rideAdapter = new RideAdapter(completedRides, ride -> {
            // You can add any click action for completed rides if needed
            Toast.makeText(ProfileActivity.this, "Ride details for completed ride", Toast.LENGTH_SHORT).show();
        });
        completedRidesRecyclerView.setAdapter(rideAdapter);  // Set adapter to display completed rides
    }
}
