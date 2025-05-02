// MyRidesActivity.java
package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class MyRidesActivity extends AppCompatActivity {

    private LinearLayout cardContainer;
    private String currentUserId;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);

        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> finish());

        cardContainer = findViewById(R.id.cardContainer);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchUserRides();
    }

    private void fetchUserRides() {
        DatabaseReference rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        DatabaseReference rideOffersRef = FirebaseDatabase.getInstance().getReference("rideOffers");

        // Find the TextView for "No Current Rides" message
        TextView noRidesMessage = findViewById(R.id.noCurrentRidesMessage);

        // Hide the message initially
        noRidesMessage.setVisibility(View.GONE);

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean hasRides = false;

                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null && ride.getAccepted() && (!ride.getDriverCompleted() || !ride.getRiderCompleted())) {
                        if (isUserInvolved(ride)) {
                            addRideCard(rideSnapshot.getKey(), ride, snapshot.getRef().getKey());
                            hasRides = true;  // Mark that a ride has been added
                        }
                    }
                }

                // If no rides were added, show the "No Current Rides" message
                if (!hasRides) {
                    noRidesMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        };

        // Add listener to both rideRequests and rideOffers references
        rideRequestsRef.addListenerForSingleValueEvent(listener);
        rideOffersRef.addListenerForSingleValueEvent(listener);
    }

    private boolean isUserInvolved(Ride ride) {
        return currentUserId.equals(ride.getDriverId()) || currentUserId.equals(ride.getRiderId());
    }

    private void addRideCard(String rideId, Ride ride, String rideType) {
        String oppositeUserId = currentUserId.equals(ride.getDriverId()) ? ride.getRiderId() : ride.getDriverId();

        if (oppositeUserId == null || oppositeUserId.trim().isEmpty()) {
            return;
        }

        View cardView = getLayoutInflater().inflate(R.layout.current_ride_item, cardContainer, false);

        TextView whenDetails = cardView.findViewById(R.id.whenDetails);
        TextView whereDetails = cardView.findViewById(R.id.whereDetails);
        TextView withDetails = cardView.findViewById(R.id.withDetails);
        TextView acceptedDetails = cardView.findViewById(R.id.acceptedDetails);
        Button confirmRideButton = cardView.findViewById(R.id.confirmRideButton);

        String dateTime = ride.getDate() + " · " + ride.getTime();
        whenDetails.setText(dateTime);

        String location = ride.getFromLocation() + " ➔ " + ride.getToLocation();
        whereDetails.setText(location);

        // Get full name of opposite user
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(oppositeUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                    String role = currentUserId.equals(ride.getDriverId()) ? "Rider" : "Driver";
                    withDetails.setText(role + ": " + fullName.trim());
                } else {
                    withDetails.setText("User: Unknown");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                withDetails.setText("User: Error");
            }
        });

        int completedCount = (ride.getDriverCompleted() ? 1 : 0) + (ride.getRiderCompleted() ? 1 : 0);
        acceptedDetails.setText(completedCount == 2 ? "Ride Complete (2/2)" : "Waiting for Confirmation (" + completedCount + "/2)");

        boolean isDriver = currentUserId.equals(ride.getDriverId());
        boolean currentCompleted = isDriver ? ride.getDriverCompleted() : ride.getRiderCompleted();

        if (completedCount == 2) {
            confirmRideButton.setVisibility(View.GONE);
        } else if (currentCompleted) {
            confirmRideButton.setEnabled(false);
            confirmRideButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            confirmRideButton.setText(isDriver ? "Waiting for Rider" : "Waiting for Driver");
        } else {
            confirmRideButton.setOnClickListener(v -> {
                DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference(rideType).child(rideId);

                // Update current user's completion status
                if (isDriver) {
                    rideRef.child("driverCompleted").setValue(true);
                } else {
                    rideRef.child("riderCompleted").setValue(true);
                }

                // Disable the button and update UI
                confirmRideButton.setEnabled(false);
                confirmRideButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                confirmRideButton.setText(isDriver ? "Waiting for Rider" : "Waiting for Driver");
                acceptedDetails.setText("Waiting for Confirmation (" + (completedCount + 1) + "/2)");

                // ✅ Check if both have completed, and update points if so
                rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Ride updatedRide = snapshot.getValue(Ride.class);
                        if (updatedRide != null &&
                                Boolean.TRUE.equals(updatedRide.getDriverCompleted()) &&
                                Boolean.TRUE.equals(updatedRide.getRiderCompleted())) {

                            // Prevent duplicate point updates (optional: add a `pointsUpdated` flag to DB if needed)

                            // ✅ Run UpdatePointsTask
                            new UpdatePointsTask(MyRidesActivity.this,
                                    updatedRide.getRiderId(),
                                    updatedRide.getDriverId()).execute();

                            // Optional: remove card from layout
                            cardContainer.removeView(cardView);

                            // ✅ Navigate to RidesHistoryActivity
                            Intent intent = new Intent(MyRidesActivity.this, RidesHistoryActivity.class);
                            startActivity(intent);
                            finish(); // Close current activity to prevent coming back with back button
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            });
        }

        cardContainer.addView(cardView);
    }
}
