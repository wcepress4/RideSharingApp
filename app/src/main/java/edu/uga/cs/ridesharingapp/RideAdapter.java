package edu.uga.cs.ridesharingapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rideList;
    private OnAcceptClickListener onAcceptClickListener;

    public interface OnAcceptClickListener {
        void onAcceptClick(Ride ride);
    }

    public RideAdapter(List<Ride> rideList, OnAcceptClickListener listener) {
        this.rideList = rideList;
        this.onAcceptClickListener = listener;
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
        holder.rideDate.setText("Date: " + ride.getDate());
        holder.rideTime.setText("Time: " + ride.getTime());
        holder.fromLocation.setText("From: " + ride.getFromLocation());
        holder.toLocation.setText("To: " + ride.getToLocation());
        holder.riderIdTextView.setText("Rider ID: " + ride.getRiderId());
        holder.driverIdTextView.setText("Driver ID: " + ride.getDriverId());

        holder.acceptButton.setOnClickListener(v -> onAcceptClickListener.onAcceptClick(ride));
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView rideDate, rideTime, fromLocation, toLocation, riderIdTextView, driverIdTextView;
        Button acceptButton;

        public RideViewHolder(View itemView) {
            super(itemView);
            rideDate = itemView.findViewById(R.id.rideDate);
            rideTime = itemView.findViewById(R.id.rideTime);
            fromLocation = itemView.findViewById(R.id.fromLocation);
            toLocation = itemView.findViewById(R.id.toLocation);
            riderIdTextView = itemView.findViewById(R.id.riderIdTextView);
            driverIdTextView = itemView.findViewById(R.id.driverIdTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
        }
    }
}
