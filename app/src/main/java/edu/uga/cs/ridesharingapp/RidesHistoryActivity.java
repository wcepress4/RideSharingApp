package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RidesHistoryActivity extends AppCompatActivity {

    private LinearLayout cardContainer;
    private String currentUserId;
    private ImageView backArrow;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rides_history);

        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> navigateToProfileActivity());

        cardContainer = findViewById(R.id.cardContainer);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fetchRideHistory();
    }

    private void fetchRideHistory() {
        DatabaseReference rideRequestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        DatabaseReference rideOffersRef = FirebaseDatabase.getInstance().getReference("rideOffers");

        rideRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null && ride.getAccepted() && ride.getDriverCompleted() && ride.getRiderCompleted() && isUserInvolved(ride)) {
                        addRideCard(ride, "request");
                    }
                }
                rideOffersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                            Ride ride = rideSnapshot.getValue(Ride.class);
                            if (ride != null && ride.getAccepted() && ride.getDriverCompleted() && ride.getRiderCompleted() && isUserInvolved(ride)) {
                                addRideCard(ride, "offer");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private boolean isUserInvolved(Ride ride) {
        return currentUserId.equals(ride.getUserId()) ||
                currentUserId.equals(ride.getDriverId()) ||
                currentUserId.equals(ride.getRiderId());
    }

    private void addRideCard(Ride ride, String rideType) {
        View cardView = getLayoutInflater().inflate(R.layout.history_ride_item, cardContainer, false);

        TextView whenDetails = cardView.findViewById(R.id.whenDetails);
        TextView whereDetails = cardView.findViewById(R.id.whereDetails);
        TextView withDetails = cardView.findViewById(R.id.withDetails);
        TextView rideTypeDetails = cardView.findViewById(R.id.rideTypeDetails);

        // Set WHEN
        String dateTime = ride.getDate() + " · " + ride.getTime();
        whenDetails.setText(dateTime);

        // Set WHERE
        String location = ride.getFromLocation() + " ➔ " + ride.getToLocation();
        whereDetails.setText(location);

        // Set WITH (Name instead of userId)
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        String oppositeUserId = (currentUserId.equals(ride.getDriverId())) ? ride.getRiderId() : ride.getDriverId();

        userRef.child(oppositeUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    withDetails.setText(firstName + " " + lastName);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });

        // Set Ride Type
        rideTypeDetails.setText(rideType.equals("request") ? "Request" : "Offer");

        // Add to container
        cardContainer.addView(cardView);
    }

    private void navigateToProfileActivity() {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }
}
