package edu.uga.cs.ridesharingapp;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;

public class RetrieveUserDetailsTask extends AsyncTask<String, String> {

    private WeakReference<RideAdapter.RideViewHolder> holderRef;

    public RetrieveUserDetailsTask(RideAdapter.RideViewHolder holder) {
        this.holderRef = new WeakReference<>(holder);
    }

    @Override
    protected String doInBackground(String... params) {
        final String[] userDetails = new String[1];
        String userId = params[0];

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                Long points = snapshot.child("points").getValue(Long.class);

                if (firstName != null && lastName != null) {
                    userDetails[0] = firstName + " " + lastName;
                } else {
                    userDetails[0] = "Unknown";
                }

                // Even if we don't *use* points now, it's good to fetch them
                if (points == null) {
                    points = 0L; // Default points if not found
                }

                onPostExecute(userDetails[0]);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                userDetails[0] = "Unknown";
                onPostExecute(userDetails[0]);
            }
        });

        return null; // Always null here because actual result comes in onDataChange
    }

    @Override
    protected void onPostExecute(String result) {
        RideAdapter.RideViewHolder holder = holderRef.get();
        if (holder != null) {
            holder.withDetails.setText(result);
        }
    }
}
