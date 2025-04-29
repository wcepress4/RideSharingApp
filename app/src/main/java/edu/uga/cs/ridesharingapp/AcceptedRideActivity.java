package edu.uga.cs.ridesharingapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class AcceptedRideActivity extends AppCompatActivity implements RideAdapter.OnAcceptClickListener {

    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> acceptedRidesList = new ArrayList<>();
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_ride);

        // Correctly get the logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = currentUser.getUid();

        recyclerView = findViewById(R.id.recyclerViewAcceptedRides);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // true for 'accepted rides' page
        rideAdapter = new RideAdapter(acceptedRidesList, this, currentUserId, false, true);
        recyclerView.setAdapter(rideAdapter);

        loadAcceptedRides();
    }

    private void loadAcceptedRides() {
        // Use RetrieveFilteredRidesTask with ALL rides
        RetrieveFilteredRidesTask task = new RetrieveFilteredRidesTask(
                rideList -> {
                    acceptedRidesList.clear();
                    for (Ride ride : rideList) {
                        // Only accepted rides not completed
                        if (ride.getAccepted() && !(ride.getDriverCompleted() && ride.getRiderCompleted())) {
                            // Only show if user is part of the ride
                            if (ride.getDriverId().equals(currentUserId) || ride.getRiderId().equals(currentUserId)) {
                                acceptedRidesList.add(ride);
                            }
                        }
                    }
                    rideAdapter.notifyDataSetChanged();
                },
                RetrieveFilteredRidesTask.FilterType.ACCEPTED,
                currentUserId,
                false // false: not filtering by offer/request tab anymore
        );
        task.fetchData();
    }

    @Override
    public void onAcceptClick(Ride ride) {
        // Completing the ride
        if (ride.getDriverId().equals(currentUserId)) {
            ride.setDriverCompleted(true);
        } else if (ride.getRiderId().equals(currentUserId)) {
            ride.setRiderCompleted(true);
        }

        // Save the update
        if (isOffer(ride)) {
            new UpdateRideOfferTask(ride.getRideKey()).execute(ride);
        } else {
            new UpdateRideRequestTask(ride.getRideKey()).execute(ride);
        }

        acceptedRidesList.remove(ride);
        rideAdapter.notifyDataSetChanged();
    }

    // Helper function
    private boolean isOffer(Ride ride) {
        return ride.getDriverId() != null && !ride.getDriverId().isEmpty();
    }
}
