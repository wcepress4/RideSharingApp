package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()); // Use single h for 12-hour
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

        rideAdapter = new RideAdapter(unacceptedRides, ride -> {
            Intent intent = new Intent(UnacceptedRidesActivity.this, EditRideActivity.class);
            intent.putExtra("ride", ride);
            startActivityForResult(intent, 1);
        }, currentUser.getUid(), true, false);
        recyclerView.setAdapter(rideAdapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.getTabAt(0).select();  // Default to Ride Offers tab

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingOffers = tab.getPosition() == 0;
                updateRideAdapter();
                fetchUnacceptedRides();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fetchUnacceptedRides();  // Initial fetch
    }

    private void navigateToProfileActivity() {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }

    private void fetchUnacceptedRides() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getUid();
        unacceptedRides.clear();

        if (showingOffers) {
            // Show only ride offers created by current user (driver)
            DatabaseReference offerRef = FirebaseDatabase.getInstance().getReference("rideOffers");
            offerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                        Ride ride = rideSnapshot.getValue(Ride.class);
                        if (ride != null && !ride.getAccepted() && currentUserId.equals(ride.getDriverId())) {
                            ride.setRideKey(rideSnapshot.getKey());
                            unacceptedRides.add(ride);
                        }
                    }
                    rideAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UnacceptedRidesActivity.this, "Failed to load ride offers.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Show only ride requests created by current user (rider)
            DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("rideRequests");
            requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                        Ride ride = rideSnapshot.getValue(Ride.class);
                        if (ride != null && !ride.getAccepted() && currentUserId.equals(ride.getRiderId())) {
                            ride.setRideKey(rideSnapshot.getKey());
                            unacceptedRides.add(ride);
                        }
                    }
                    rideAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UnacceptedRidesActivity.this, "Failed to load ride requests.", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        String uid = currentUser.getUid();
        return showingOffers
                ? uid.equals(ride.getDriverId())
                : uid.equals(ride.getRiderId());
    }

    private void updateRideAdapter() {
        rideAdapter = new RideAdapter(unacceptedRides, ride -> {
            Intent intent = new Intent(UnacceptedRidesActivity.this, EditRideActivity.class);
            intent.putExtra("ride", ride);
            startActivityForResult(intent, 1);
        }, currentUser.getUid(), showingOffers, false);
        recyclerView.setAdapter(rideAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            fetchUnacceptedRides();
        }
    }
}
