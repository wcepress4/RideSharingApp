package edu.uga.cs.ridesharingapp;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class RetrieveFilteredRidesTask extends AsyncTask<Void, List<Ride>> {

    public interface RideDataCallback {
        void onRidesRetrieved(List<Ride> rides);
    }

    public enum FilterType {
        ALL_AVAILABLE,
        USER_CREATED,
        ACCEPTED_BY_USER,
        COMPLETED_BY_USER
    }

    private final RideDataCallback callback;
    private final FilterType filterType;
    private final String currentUserId;
    private final boolean isOffer;  // true = rideOffers, false = rideRequests

    public RetrieveFilteredRidesTask(RideDataCallback callback, FilterType filterType, String currentUserId, boolean isOffer) {
        this.callback = callback;
        this.filterType = filterType;
        this.currentUserId = currentUserId;
        this.isOffer = isOffer;
    }

    @Override
    protected List<Ride> doInBackground(Void... voids) {
        return null; // Firebase is async, no use here
    }

    @Override
    protected void onPostExecute(List<Ride> rides) {
        // Firebase handles this separately
    }

    public void fetchData() {
        String path = isOffer ? "rideOffers" : "rideRequests";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Ride> filteredRides = new ArrayList<>();
                long now = System.currentTimeMillis();

                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride == null) continue;

                    boolean include = false;

                    switch (filterType) {
                        case ALL_AVAILABLE:
                            if (!ride.getAccepted() && !ride.getCompleted() && !isExpired(ride)) {
                                include = true;
                            }
                            break;
                        case USER_CREATED:
                            String ownerId = isOffer ? ride.getDriverId() : ride.getRiderId();
                            if (currentUserId.equals(ownerId) && !ride.getAccepted()) {
                                include = true;
                            }
                            break;
                        case ACCEPTED_BY_USER:
                            boolean involved = currentUserId.equals(ride.getDriverId()) || currentUserId.equals(ride.getRiderId());
                            if (involved && ride.getAccepted() && !ride.getCompleted() && !isExpired(ride)) {
                                include = true;
                            }
                            break;
                        case COMPLETED_BY_USER:
                            boolean wasInvolved = currentUserId.equals(ride.getDriverId()) || currentUserId.equals(ride.getRiderId());
                            if (wasInvolved && ride.getCompleted()) {
                                include = true;
                            }
                            break;
                    }

                    if (include) {
                        filteredRides.add(ride);
                    }
                }

                callback.onRidesRetrieved(filteredRides);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onRidesRetrieved(new ArrayList<>());
            }
        });
    }

    private boolean isExpired(Ride ride) {
        try {
            String dateTimeString = ride.getDate() + " " + ride.getTime(); // e.g., "2025-04-17 14:30"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date pickup = sdf.parse(dateTimeString);
            return pickup.getTime() < System.currentTimeMillis();
        } catch (Exception e) {
            return false;
        }
    }
}
