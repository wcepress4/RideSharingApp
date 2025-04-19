package edu.uga.cs.ridesharingapp;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class RetrieveProfile extends AsyncTask<Void, RetrieveProfile.ProfileResult> {

    public interface OnProfileRetrievedListener {
        void onProfileRetrieved(ProfileResult result);
    }

    private OnProfileRetrievedListener listener;

    public RetrieveProfile(OnProfileRetrievedListener listener) {
        this.listener = listener;
    }

    public static class ProfileResult {
        public String name;
        public String email;
        public long points;

        public ProfileResult(String name, String email, long points) {
            this.name = name;
            this.email = email;
            this.points = points;
        }
    }

    @Override
    protected ProfileResult doInBackground(Void... voids) {
        // This should not be used for Firebase retrieval directly, since Firebase is async by nature.
        // We'll leave this empty and trigger the result manually once Firebase returns the data.
        return null;
    }

    @Override
    protected void onPostExecute(ProfileResult result) {
        if (listener != null && result != null) {
            listener.onProfileRetrieved(result);
        }
    }

    public void fetchProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            onPostExecute(new ProfileResult("N/A", "N/A", 0));
            return;
        }

        String name = user.getDisplayName() != null ? user.getDisplayName() : "N/A";
        String email = user.getEmail();
        String uid = user.getUid();

        DatabaseReference pointsRef = FirebaseDatabase.getInstance()
                .getReference("points")
                .child(uid);

        pointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long points = 0;
                if (snapshot.exists()) {
                    points = snapshot.getValue(Long.class);
                }
                ProfileResult result = new ProfileResult(name, email, points);
                onPostExecute(result); // manually trigger callback
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RetrieveProfile", "Failed to fetch profile data", error.toException());
                onPostExecute(new ProfileResult(name, email, 0)); // fallback on error
            }
        });
    }
}
