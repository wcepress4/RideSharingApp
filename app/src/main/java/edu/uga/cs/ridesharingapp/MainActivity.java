package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private String currentTab = "Ride Offers";  // Default tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getText().toString();
                loadRideDetails();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        ImageView homeButton = findViewById(R.id.homeButton);
        ImageView addRideButton = findViewById(R.id.addRideButton);
        ImageView profileButton = findViewById(R.id.profileButton);

        homeButton.setOnClickListener(v -> recyclerView.scrollToPosition(0));

        addRideButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddRideActivity.class)));

        profileButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        loadRideDetails(); // Initial load
    }

    private void loadRideDetails() {
        if (currentTab.equals("Ride Offers")) {
            RetrieveRideOffersTask offersTask = new RetrieveRideOffersTask(new RetrieveRideOffersTask.RideDataCallback() {
                @Override
                public void onRidesRetrieved(List<Ride> rides) {
                    displayRides(rides);
                }
            });
            offersTask.fetchData();
        } else {
            RetrieveRideRequestsTask requestsTask = new RetrieveRideRequestsTask(new RetrieveRideRequestsTask.RideDataCallback() {
                @Override
                public void onRidesRetrieved(List<Ride> rides) {
                    displayRides(rides);
                }
            });
            requestsTask.fetchData();
        }
    }

    private void displayRides(List<Ride> rideList) {
        rideAdapter = new RideAdapter(rideList, new RideAdapter.OnAcceptClickListener() {
            @Override
            public void onAcceptClick(Ride ride) {
                Toast.makeText(MainActivity.this, "Accepted ride with Rider ID: " + ride.getRiderId(), Toast.LENGTH_SHORT).show();

                // TODO: Add Firebase logic to mark ride as accepted, e.g.:
                // ride.setAccepted(true);
                // ride.setDriverId(currentUser.getUid()); // if current user is driver
                // Update in Firebase
            }
        });
        recyclerView.setAdapter(rideAdapter);
    }
}
