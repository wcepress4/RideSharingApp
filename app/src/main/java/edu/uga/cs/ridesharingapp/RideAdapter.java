package edu.uga.cs.ridesharingapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private OnAcceptClickListener onAcceptClickListener;
    private String currentUserId;
    private boolean isOfferTab;

    public interface OnAcceptClickListener {
        void onAcceptClick(Ride ride);
    }

    public RideAdapter(List<Ride> rideList, OnAcceptClickListener listener, String currentUserId, boolean isOfferTab) {
        this.rideList = rideList;
        this.onAcceptClickListener = listener;
        this.currentUserId = currentUserId;
        this.isOfferTab = isOfferTab;
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

        // Bind new fields according to new XML
        holder.whenDetails.setText(ride.getDate() + " · " + ride.getTime());
        holder.whereDetails.setText(ride.getFromLocation() + " ➔ " + ride.getToLocation());
        holder.withDetails.setText(ride.getRiderId()); // Could replace with rider name if you have it

        // Conditional visibility based on tab and user role
        boolean showSettings = isOfferTab
                ? currentUserId.equals(ride.getDriverId())
                : currentUserId.equals(ride.getRiderId());
        holder.settingsButton.setVisibility(showSettings ? View.VISIBLE : View.GONE);

        holder.acceptButton.setOnClickListener(v -> onAcceptClickListener.onAcceptClick(ride));

        holder.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EditRideActivity.class);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView whenDetails, whereDetails, withDetails;
        Button acceptButton;
        ImageButton settingsButton;

        public RideViewHolder(View itemView) {
            super(itemView);
            whenDetails = itemView.findViewById(R.id.whenDetails);
            whereDetails = itemView.findViewById(R.id.whereDetails);
            withDetails = itemView.findViewById(R.id.withDetails);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            settingsButton = itemView.findViewById(R.id.settingsButton);
        }
    }
}
