package edu.uga.cs.ridesharingapp;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUserTask extends AsyncTask<String, Boolean> {

    private Context context;
    private FirebaseAuth mAuth;

    public RegisterUserTask(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String email = params[0];
        String password = params[1];

        final boolean[] result = {false};

        // Firebase operations need to run synchronously within the async wrapper
        final Object lock = new Object();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Give initial points (100)
                            FirebaseDatabase.getInstance().getReference("points")
                                    .child(user.getUid()).setValue(100);
                            result[0] = true;
                        }
                    }
                    synchronized (lock) {
                        lock.notify();
                    }
                });

        try {
            synchronized (lock) {
                lock.wait(); // Wait for Firebase task to complete
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, MainActivity.class));
        } else {
            Toast.makeText(context, "Registration failed!", Toast.LENGTH_LONG).show();
        }
    }
}
