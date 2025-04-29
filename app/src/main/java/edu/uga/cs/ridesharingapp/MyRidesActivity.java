package edu.uga.cs.ridesharingapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyRidesActivity extends AppCompatActivity {

    private LinearLayout cardContainer;
    private String currentUserId;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rides);

        // Initialize views
        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> finish());  // Navigates back to the previous activity

        cardContainer = findViewById(R.id.cardContainer);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchUserRides();
    }

    private void fetchUserRides() {
        DatabaseReference rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        DatabaseReference rideOffersRef = FirebaseDatabase.getInstance().getReference("rideOffers");

        // Fetch ride requests where the current user is involved, accepted and incomplete
        rideRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null && ride.getAccepted() && !ride.getRiderCompleted() && !ride.getDriverCompleted() &&
                            isUserInvolved(ride)) {
                        addRideCard(ride);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });

        // Fetch ride offers where the current user is involved, accepted and incomplete
        rideOffersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null && ride.getAccepted() && !ride.getRiderCompleted() && !ride.getDriverCompleted() &&
                            isUserInvolved(ride)) {
                        addRideCard(ride);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    // Check if the current user is either the driver or the rider of the ride
    private boolean isUserInvolved(Ride ride) {
        return currentUserId.equals(ride.getDriverId()) || currentUserId.equals(ride.getRiderId());
    }

    // Add the ride card view for the ride to the layout
    private void addRideCard(Ride ride) {
        View cardView = getLayoutInflater().inflate(R.layout.current_ride_item, cardContainer, false);

        TextView whenDetails = cardView.findViewById(R.id.whenDetails);
        TextView whereDetails = cardView.findViewById(R.id.whereDetails);
        TextView withDetails = cardView.findViewById(R.id.withDetails);
        TextView acceptedDetails = cardView.findViewById(R.id.acceptedDetails);

        // Set WHEN
        String dateTime = ride.getDate() + " · " + ride.getTime();
        whenDetails.setText(dateTime);

        // Set WHERE
        String location = ride.getFromLocation() + " ➔ " + ride.getToLocation();
        whereDetails.setText(location);

        // Set WITH (driver or rider)
        String withText;
        if (currentUserId.equals(ride.getDriverId())) {
            withText = "Rider: " + ride.getRiderId();
        } else {
            withText = "Driver: " + ride.getDriverId();
        }
        withDetails.setText(withText);

        // Set ACCEPTED status
        acceptedDetails.setText(ride.getAccepted() ? "Accepted" : "Pending");

        // Add the card to the container
        cardContainer.addView(cardView);
    }
}
