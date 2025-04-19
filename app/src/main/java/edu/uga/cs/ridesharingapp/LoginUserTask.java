package edu.uga.cs.ridesharingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginUserTask extends AsyncTask<String, Boolean> {

    private Context context;
    private Activity activity;
    private FirebaseAuth mAuth;

    public LoginUserTask(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        this.mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String email = params[0];
        String password = params[1];

        final boolean[] success = {false};
        final Object lock = new Object();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    success[0] = task.isSuccessful();
                    synchronized (lock) {
                        lock.notify();
                    }
                });

        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return success[0];
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, MainActivity.class));
            activity.finish();
        } else {
            Toast.makeText(context, "Login failed. Check your credentials.", Toast.LENGTH_LONG).show();
        }
    }
}
