package edu.uga.cs.ridesharingapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private String currentTab = "Offers";  // Default tab is "Offers"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            // Redirect to LoginActivity
            return;
        }

        // Initialize TabLayout and RecyclerView
        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setup tab layout listener to switch between "Offers" and "Requests"
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getText().toString();  // Get selected tab's text
                loadRideDetails();  // Load ride details based on selected tab
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Default loading of rides when the activity starts
        loadRideDetails();
    }

    private void loadRideDetails() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ridesRef;

        // Select the database reference based on the selected tab
        if (currentTab.equals("Offers")) {
            ridesRef = database.getReference("rideOffers");  // Fetch ride offers from Firebase
        } else {
            ridesRef = database.getReference("rideRequests");  // Fetch ride requests from Firebase
        }

        // Fetch data from Firebase
        ridesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Ride> rideList = new ArrayList<>();

                // Loop through each snapshot and add rides to the list
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    rideList.add(ride);
                }

                // Pass the list to the adapter and set it on the RecyclerView
                rideAdapter = new RideAdapter(rideList, new RideAdapter.OnAcceptClickListener() {
                    @Override
                    public void onAcceptClick(Ride ride) {
                        // Handle the "Accept" action here (e.g., updating Firebase, showing confirmation)
                        Toast.makeText(MainActivity.this, "Accepted ride: " + ride.getRiderName(), Toast.LENGTH_SHORT).show();
                    }
                });
                recyclerView.setAdapter(rideAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show error message if the data fetching is cancelled or failed
                Toast.makeText(MainActivity.this, "Failed to load ride details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
