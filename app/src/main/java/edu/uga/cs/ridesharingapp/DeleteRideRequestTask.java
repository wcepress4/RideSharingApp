package edu.uga.cs.ridesharingapp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DeleteRideRequestTask extends AsyncTask<Ride, Void> {

    private String rideKey;

    // Constructor to pass the Firebase-generated key
    public DeleteRideRequestTask(String rideKey) {
        this.rideKey = rideKey;
    }

    @Override
    protected Void doInBackground(Ride... rides) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("rideRequests").child(rideKey);
        dbRef.removeValue();  // Delete the ride with the given key
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        // Optional: handle post-deletion actions, like showing a confirmation
    }
}
