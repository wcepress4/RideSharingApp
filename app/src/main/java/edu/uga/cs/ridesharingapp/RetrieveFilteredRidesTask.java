package edu.uga.cs.ridesharingapp;

import android.util.Log;

import com.google.firebase.database.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RetrieveFilteredRidesTask extends AsyncTask<Void, List<Ride>> {

    public interface RideDataCallback {
        void onRidesRetrieved(List<Ride> rides);
    }

    public enum FilterType {
        ALL_AVAILABLE,
        USER_CREATED,
        ACCEPTED,
        COMPLETED
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
        return null;
    }

    @Override
    protected void onPostExecute(List<Ride> rides) {
        // Not used
    }

    public void fetchData() {
        String path = isOffer ? "rideOffers" : "rideRequests";
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(path);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Ride> filteredRides = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                SimpleDateFormat customDateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());

                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride == null) continue;

                    // Set the Firebase key to the ride object
                    ride.setRideKey(rideSnapshot.getKey());

                    boolean include = false;
                    boolean userInvolved = currentUserId.equals(ride.getDriverId()) || currentUserId.equals(ride.getRiderId());
                    boolean isFullyCompleted = ride.getDriverCompleted() && ride.getRiderCompleted();

                    // Always exclude expired rides
                    if (isExpired(ride, customDateFormat)) {
                        continue;  // Skip expired rides entirely
                    }

                    switch (filterType) {
                        case ALL_AVAILABLE:
                            if (!ride.getAccepted() && !isFullyCompleted) {
                                include = true;
                            }
                            break;
                        case USER_CREATED:
                            String ownerId = isOffer ? ride.getDriverId() : ride.getRiderId();
                            if (currentUserId.equals(ownerId) && !ride.getAccepted() && !isFullyCompleted) {
                                include = true;
                            }
                            break;
                        case ACCEPTED:
                            if (userInvolved && ride.getAccepted() && !isFullyCompleted) {
                                include = true;
                            }
                            break;
                        case COMPLETED:
                            if (userInvolved && isFullyCompleted) {
                                include = true;
                            }
                            break;
                    }

                    if (include) {
                        filteredRides.add(ride);
                    }
                }

                // Sort by soonest date and time (ascending order)
                Collections.sort(filteredRides, (r1, r2) -> {
                    try {
                        String dateTime1 = r1.getDate() + " " + r1.getTime();
                        String dateTime2 = r2.getDate() + " " + r2.getTime();

                        Date d1 = sdf.parse(dateTime1);
                        Date d2 = sdf.parse(dateTime2);

                        // Log dates for debugging purposes
                        Log.d("DateComparison", "Comparing " + dateTime1 + " with " + dateTime2);

                        return d1.compareTo(d2); // If d1 is before d2, it will return a negative value (ascending order)
                    } catch (Exception e) {
                        // If parsing fails, fall back to comparing based on the custom format
                        try {
                            Date d1 = customDateFormat.parse(r1.getDate() + " " + r1.getTime());
                            Date d2 = customDateFormat.parse(r2.getDate() + " " + r2.getTime());
                            return d1.compareTo(d2);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return 0; // If parsing fails again, consider them equal
                        }
                    }
                });

                // Pass the sorted list to the callback
                callback.onRidesRetrieved(filteredRides);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onRidesRetrieved(new ArrayList<>());
            }
        });
    }

    private boolean isExpired(Ride ride, SimpleDateFormat customDateFormat) {
        try {
            String dateTimeString = ride.getDate() + " " + ride.getTime();
            Date pickup = null;

            // Try parsing with the custom format first
            try {
                pickup = customDateFormat.parse(dateTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // If parsing with custom format fails, fall back to the default format
            if (pickup == null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                pickup = sdf.parse(dateTimeString);
            }

            if (pickup == null) {
                Log.e("DateError", "Failed to parse date: " + dateTimeString);
                return false; // Default to not expired if parsing fails
            }

            // Get the current time up to minute precision
            Calendar now = Calendar.getInstance();
            now.set(Calendar.SECOND, 0);
            now.set(Calendar.MILLISECOND, 0);

            Log.d("DateCheck", "Comparing ride time " + pickup + " with current time " + now.getTime());

            return pickup.before(now.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
