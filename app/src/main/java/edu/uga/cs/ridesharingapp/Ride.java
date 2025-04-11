package edu.uga.cs.ridesharingapp;

public class Ride {
    private String date;
    private String time;
    private String fromLocation;
    private String toLocation;
    private String riderName;
    private String userId;

    public Ride() {
        // Default constructor required for Firebase
    }

    public Ride(String date, String time, String fromLocation, String toLocation, String riderName, String userId) {
        this.date = date;
        this.time = time;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.riderName = riderName;
        this.userId = userId;
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

    public String getRiderName() {
        return riderName;
    }

    public String getUserId() {
        return userId;
    }
}
