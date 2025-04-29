package edu.uga.cs.ridesharingapp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateRideOfferTask extends AsyncTask<Ride, Boolean> {

    private String rideKey;

    public UpdateRideOfferTask(String rideKey) {
        this.rideKey = rideKey;
    }

    @Override
    protected Boolean doInBackground(Ride... rides) {
        if (rides.length == 0 || rides[0] == null) {
            return false;
        }

        Ride ride = rides[0];
        DatabaseReference databaseRef = FirebaseDatabase.getInstance()
                .getReference("rideOffers")
                .child(rideKey);

        databaseRef.setValue(ride);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        // Optional: you can show a toast or log if you want
        // No action needed unless you want user feedback
    }
}
