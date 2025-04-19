package edu.uga.cs.ridesharingapp;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RetrieveRideRequestsTask extends AsyncTask<Void, List<Ride>> {
    private final RideDataCallback callback;

    public interface RideDataCallback {
        void onRidesRetrieved(List<Ride> rides);
    }

    public RetrieveRideRequestsTask(RideDataCallback callback) {
        this.callback = callback;
    }

    @Override
    protected List<Ride> doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(List<Ride> result) {}

    public void fetchData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rideRequests");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Ride> rides = new ArrayList<>();
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    rides.add(ride);
                }
                callback.onRidesRetrieved(rides);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onRidesRetrieved(new ArrayList<>());
            }
        });
    }
}
