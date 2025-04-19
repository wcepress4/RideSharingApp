package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, pointsTextView;
    private Button backButton, logoutButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        backButton = findViewById(R.id.backButton);
        logoutButton = findViewById(R.id.logoutButton); // NEW BUTTON

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
    }
}
