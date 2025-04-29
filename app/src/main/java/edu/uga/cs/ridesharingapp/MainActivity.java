package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_RIDE_REQUEST_CODE = 1001;
    private static final int EDIT_RIDE_REQUEST_CODE = 1002;  // For handling edit rides

    private FirebaseAuth mAuth;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private String currentTab = "Ride Offers";
    private String currentUserId;

    private TextView userGreeting;
    private TextView userPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        currentUserId = currentUser.getUid();

        // Get views
        userGreeting = findViewById(R.id.userGreeting);
        userPoints = findViewById(R.id.userPoints);

        fetchUserInfo();

        tabLayout = findViewById(R.id.tabLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getText().toString();
                loadRideDetails();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        findViewById(R.id.homeButton).setOnClickListener(v -> recyclerView.scrollToPosition(0));
        findViewById(R.id.addRideButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddRideActivity.class);
            startActivityForResult(intent, ADD_RIDE_REQUEST_CODE);
        });
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        loadRideDetails();
    }

    private void fetchUserInfo() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                Long points = snapshot.child("points").getValue(Long.class);

                if (firstName != null && lastName != null) {
                    userGreeting.setText("Hello, " + firstName + " " + lastName);
                }
                if (points != null) {
                    userPoints.setText("Points: " + points);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ADD_RIDE_REQUEST_CODE || requestCode == EDIT_RIDE_REQUEST_CODE) {
                loadRideDetails(); // Reload the rides after either adding or editing a ride
            }
        }
    }

    private void loadRideDetails() {
        // Determine the filter type based on the tab
        RetrieveFilteredRidesTask.FilterType filterType = RetrieveFilteredRidesTask.FilterType.ALL_AVAILABLE;
        boolean isOffer = currentTab.equals("Ride Offers");

        RetrieveFilteredRidesTask task = new RetrieveFilteredRidesTask(
                rideList -> displayRides(rideList),
                filterType,
                currentUserId,
                isOffer
        );
        task.fetchData(); // Fetch updated data
    }

    private void displayRides(List<Ride> rideList) {
        boolean isOfferTab = currentTab.equals("Ride Offers");

        rideAdapter = new RideAdapter(
                rideList,
                ride -> {
                    if (!ride.getAccepted()) {
                        // Accept the ride: mark as accepted and update it in Firebase
                        ride.setAccepted(true);

                        if (isOfferTab) {
                            new UpdateRideOfferTask(ride.getRideKey()).execute(ride);
                        } else {
                            new UpdateRideRequestTask(ride.getRideKey()).execute(ride);
                        }

                        Toast.makeText(MainActivity.this, "Ride accepted!", Toast.LENGTH_SHORT).show();
                        loadRideDetails();
                    } else {
                        Toast.makeText(MainActivity.this, "Ride already accepted.", Toast.LENGTH_SHORT).show();
                    }
                },
                currentUserId,
                isOfferTab,
                false  // <<<< HERE: isAcceptedRidePage is FALSE in MainActivity
        );

        recyclerView.setAdapter(rideAdapter);
    }
}
