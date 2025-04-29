package edu.uga.cs.ridesharingapp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateRideOfferTask {

    private String rideKey;

    public UpdateRideOfferTask(String rideKey) {
        this.rideKey = rideKey;
    }

    public void execute(Ride ride) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("rideOffers")
                .child(rideKey);
        databaseRef.setValue(ride);
    }
}
