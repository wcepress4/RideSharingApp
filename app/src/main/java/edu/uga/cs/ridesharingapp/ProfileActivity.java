package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, pointsTextView;
    private Button logoutButton;
    private ImageView backArrow, homeButton, addRideButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Button myRidesButton = findViewById(R.id.myRidesButton);
        Button ridesHistoryButton = findViewById(R.id.ridesHistoryButton);

        myRidesButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyRideActivity.class);
            startActivity(intent);
        });

        ridesHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RidesHistoryActivity.class);
            startActivity(intent);
        });


        // Initialize views
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        logoutButton = findViewById(R.id.logoutButton);
        backArrow = findViewById(R.id.backArrow);
        homeButton = findViewById(R.id.homeButton);
        addRideButton = findViewById(R.id.addRideButton);

        // Handle logout
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle back arrow to MainActivity
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Bottom Nav: Home
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Bottom Nav: Add Ride
        addRideButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddRideActivity.class);
            startActivity(intent);
        });

        // Fetch profile info asynchronously
        new RetrieveProfile(result -> {
            nameTextView.setText("Name: " + result.name);
            emailTextView.setText("Email: " + result.email);
            pointsTextView.setText("Points: " + result.points);
        }).fetchProfile();
    }
}
