package edu.uga.cs.ridesharingapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LogoutUserTask extends AsyncTask<Void, Boolean> {

    private Context context;
    private Activity activity;

    public LogoutUserTask(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            FirebaseAuth.getInstance().signOut();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            Toast.makeText(context, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(context, LoginActivity.class));
            activity.finish();
        } else {
            Toast.makeText(context, "Logout failed. Try again.", Toast.LENGTH_LONG).show();
        }
    }
}
