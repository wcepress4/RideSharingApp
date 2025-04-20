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

        String email = user.getEmail();
        String uid = user.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                Long points = snapshot.child("points").getValue(Long.class);

                String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                long userPoints = points != null ? points : 0;

                ProfileResult result = new ProfileResult(fullName.trim(), email, userPoints);
                onPostExecute(result); // callback
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RetrieveProfile", "Failed to fetch profile", error.toException());
                onPostExecute(new ProfileResult("N/A", email, 0));
            }
        });
    }
}
