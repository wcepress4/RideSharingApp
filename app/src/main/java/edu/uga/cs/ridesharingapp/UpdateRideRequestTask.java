package edu.uga.cs.ridesharingapp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateRideRequestTask {

    private String rideKey;

    public UpdateRideRequestTask(String rideKey) {
        this.rideKey = rideKey;
    }

    public void execute(Ride ride) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("rideRequests")
                .child(rideKey);
        databaseRef.setValue(ride);
    }
}
