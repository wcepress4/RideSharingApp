package edu.uga.cs.ridesharingapp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StoreRideRequestTask extends AsyncTask<Ride, Void> {

    @Override
    protected Void doInBackground(Ride... rides) {
        Ride ride = rides[0];
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        dbRef.push().setValue(ride);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        // Optional: any post-processing
    }
}
