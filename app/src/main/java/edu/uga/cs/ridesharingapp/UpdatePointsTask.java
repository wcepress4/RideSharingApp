package edu.uga.cs.ridesharingapp;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdatePointsTask extends AsyncTask<Void, Boolean> {

    private Context context;
    private String riderId;
    private String driverId;
    private DatabaseReference usersRef;
    private boolean success = true;

    public UpdatePointsTask(Context context, String riderId, String driverId) {
        this.context = context;
        this.riderId = riderId;
        this.driverId = driverId;
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        final Object lock = new Object(); // for waiting inside background thread

        // Update Rider
        usersRef.child(riderId).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long currentPoints = snapshot.getValue(Long.class);
                if (currentPoints != null) {
                    usersRef.child(riderId).child("points").setValue(currentPoints - 50);
                } else {
                    success = false;
                }
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                success = false;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                success = false;
            }
        }

        // Update Driver
        usersRef.child(driverId).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long currentPoints = snapshot.getValue(Long.class);
                if (currentPoints != null) {
                    usersRef.child(driverId).child("points").setValue(currentPoints + 50);
                } else {
                    success = false;
                }
                synchronized (lock) {
                    lock.notify();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                success = false;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });

        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                success = false;
            }
        }

        return success;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Toast.makeText(context, "Points updated successfully.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to update points.", Toast.LENGTH_SHORT).show();
        }
    }
}
