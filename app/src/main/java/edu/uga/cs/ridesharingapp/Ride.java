package edu.uga.cs.ridesharingapp;

import java.io.Serializable;

public class Ride implements Serializable {
    private String date;
    private String time;
    private String fromLocation;
    private String toLocation;
    private String riderId;  // ID of the rider
    private String driverId; // ID of the driver
    private String userId;   // ID of the user who posted the ride
    private boolean accepted;
    private boolean riderCompleted;
    private boolean driverCompleted;
    private String rideKey;  // Firebase database key

    public Ride() {
        // Default constructor required for Firebase
    }

    public Ride(String date, String time, String fromLocation, String toLocation,
                String userId, String driverId, String riderId,
                boolean accepted, boolean riderCompleted, boolean driverCompleted) {
        this.date = date;
        this.time = time;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.userId = userId;
        this.driverId = driverId;
        this.riderId = riderId;
        this.accepted = accepted;
        this.riderCompleted = riderCompleted;
        this.driverCompleted = driverCompleted;
    }

    // Getters
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getFromLocation() { return fromLocation; }
    public String getToLocation() { return toLocation; }
    public String getRiderId() { return riderId; }
    public String getDriverId() { return driverId; }
    public String getUserId() { return userId; }
    public boolean getAccepted() { return accepted; }
    public boolean getRiderCompleted() { return riderCompleted; }
    public boolean getDriverCompleted() { return driverCompleted; }
    public String getRideKey() { return rideKey; }

    // Setters
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }
    public void setRiderId(String riderId) { this.riderId = riderId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
    public void setRiderCompleted(boolean riderCompleted) { this.riderCompleted = riderCompleted; }
    public void setDriverCompleted(boolean driverCompleted) { this.driverCompleted = driverCompleted; }
    public void setRideKey(String rideKey) { this.rideKey = rideKey; }
}
