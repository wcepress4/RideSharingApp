package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UnacceptedRidesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> unacceptedRides = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
    private FirebaseUser currentUser;
    private ImageView backArrow;
    private TabLayout tabLayout;
    private boolean showingOffers = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unaccepted_rides);

        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(v -> navigateToProfileActivity());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = findViewById(R.id.recyclerViewUnaccepted);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the RideAdapter with the correct tab info (Ride Offers tab)
        rideAdapter = new RideAdapter(unacceptedRides, ride -> {}, currentUser.getUid(), true, false);
        recyclerView.setAdapter(rideAdapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.getTabAt(0).select();  // Default to Ride Offers tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Set whether we're showing offers or requests based on the tab selected
                showingOffers = tab.getPosition() == 0;
                // Update the adapter to reflect the correct tab
                updateRideAdapter();
                fetchUnacceptedRides();  // Fetch rides again based on the selected tab
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        fetchUnacceptedRides();  // Initial fetch when activity is first created
    }

    private void navigateToProfileActivity() {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }

    private void fetchUnacceptedRides() {
        unacceptedRides.clear();
        DatabaseReference databaseRef = showingOffers
                ? FirebaseDatabase.getInstance().getReference("rideOffers")
                : FirebaseDatabase.getInstance().getReference("rideRequests");

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null && !ride.getAccepted() && isUserRide(ride) && isPastDate(ride)) {
                        unacceptedRides.add(ride);
                    }
                }
                // Notify the adapter that the data has changed
                rideAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) { }
        });
    }

    private boolean isPastDate(Ride ride) {
        try {
            Date rideDate = dateFormat.parse(ride.getDate() + " " + ride.getTime());
            return rideDate != null && rideDate.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isUserRide(Ride ride) {
        return ride.getUserId() != null && ride.getUserId().equals(currentUser.getUid());
    }

    private void updateRideAdapter() {
        // Reinitialize the adapter with the correct tab value to reflect the changes
        rideAdapter = new RideAdapter(unacceptedRides, ride -> {}, currentUser.getUid(), showingOffers, false);
        recyclerView.setAdapter(rideAdapter);
    }
}
