package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private OnAcceptClickListener onAcceptClickListener;
    private String currentUserId;
    private boolean isOfferTab;
    private boolean isAcceptedRidePage;

    public interface OnAcceptClickListener {
        void onAcceptClick(Ride ride);
    }

    public RideAdapter(List<Ride> rideList, OnAcceptClickListener listener, String currentUserId, boolean isOfferTab, boolean isAcceptedRidePage) {
        this.rideList = rideList;
        this.onAcceptClickListener = listener;
        this.currentUserId = currentUserId;
        this.isOfferTab = isOfferTab;
        this.isAcceptedRidePage = isAcceptedRidePage;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_item, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);

        holder.whenDetails.setText(ride.getDate() + " · " + ride.getTime());
        holder.whereDetails.setText(ride.getFromLocation() + " ➔ " + ride.getToLocation());

        if (isOfferTab) {
            String driverId = ride.getDriverId();
            if (driverId != null && !driverId.isEmpty()) {
                new RetrieveUserDetailsTask(holder).execute(driverId);
            } else {
                holder.withDetails.setText("Unknown Driver");
            }
        } else {
            String riderId = ride.getRiderId();
            if (riderId != null && !riderId.isEmpty()) {
                new RetrieveUserDetailsTask(holder).execute(riderId);
            } else {
                holder.withDetails.setText("Unknown Rider");
            }
        }

        if (isAcceptedRidePage) {
            holder.settingsButton.setVisibility(View.GONE);
            holder.addEditButton.setText("Complete Ride");
            holder.addEditButton.setOnClickListener(v -> {
                onAcceptClickListener.onAcceptClick(ride);
            });
        } else {
            boolean isUserCreated = isOfferTab
                    ? currentUserId.equals(ride.getDriverId())
                    : currentUserId.equals(ride.getRiderId());

            if (isUserCreated) {
                holder.settingsButton.setVisibility(View.VISIBLE);
                holder.addEditButton.setText("Edit Ride");

                holder.addEditButton.setOnClickListener(v -> {
                    Intent intent = new Intent(holder.itemView.getContext(), EditRideActivity.class);
                    intent.putExtra("ride", ride);
                    holder.itemView.getContext().startActivity(intent);
                });

                holder.settingsButton.setOnClickListener(v -> {
                    if (isOfferTab) {
                        new DeleteRideOfferTask(ride.getRideKey()).execute(ride);
                        Toast.makeText(holder.itemView.getContext(), "Ride deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        new DeleteRideRequestTask(ride.getRideKey()).execute(ride);
                        Toast.makeText(holder.itemView.getContext(), "Ride deleted", Toast.LENGTH_SHORT).show();
                    }
                    rideList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, rideList.size());
                });
            } else {
                holder.settingsButton.setVisibility(View.GONE);
                holder.addEditButton.setText("Accept Ride");

                holder.addEditButton.setOnClickListener(v -> {
                    Log.d("RideAdapter", "THIS USER ACCEPTED -> " + currentUserId);

                    DatabaseReference dbRef;
                    if (isOfferTab) {
                        dbRef = FirebaseDatabase.getInstance().getReference("rideOffers").child(ride.getRideKey());
                        dbRef.child("riderId").setValue(currentUserId);
                    } else {
                        dbRef = FirebaseDatabase.getInstance().getReference("rideRequests").child(ride.getRideKey());
                        dbRef.child("driverId").setValue(currentUserId);
                    }

                    dbRef.child("accepted").setValue(true);
                    ride.setAccepted(true);

                    if (isOfferTab) {
                        ride.setRiderId(currentUserId);
                    } else {
                        ride.setDriverId(currentUserId);
                    }

                    onAcceptClickListener.onAcceptClick(ride);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView whenDetails, whereDetails, withDetails;
        Button addEditButton;
        ImageButton settingsButton;

        public RideViewHolder(View itemView) {
            super(itemView);
            whenDetails = itemView.findViewById(R.id.whenDetails);
            whereDetails = itemView.findViewById(R.id.whereDetails);
            withDetails = itemView.findViewById(R.id.withDetails);
            addEditButton = itemView.findViewById(R.id.addEditButton);
            settingsButton = itemView.findViewById(R.id.settingsButton);
        }
    }
}
