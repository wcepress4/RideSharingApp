package edu.uga.cs.ridesharingapp;

public class Ride {
    private String date;
    private String time;
    private String fromLocation;
    private String toLocation;
    private String riderId;
    private String driverId;
    private boolean accepted;
    private boolean completed;

    public Ride() {
        // Default constructor required for Firebase
    }

    public Ride(String date, String time, String fromLocation, String toLocation, String riderId, String driverId, boolean accepted, boolean completed) {
        this.date = date;
        this.time = time;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.riderId = riderId;
        this.driverId = driverId;
        this.accepted = accepted;
        this.completed = completed;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public String getRiderId() {
        return riderId;
    }

    public String getDriverId() {
        return driverId;
    }

    public boolean getAccepted() { return accepted; }

    public boolean getCompleted() { return completed; }
}
