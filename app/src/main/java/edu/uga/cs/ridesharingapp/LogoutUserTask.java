package edu.uga.cs.ridesharingapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogoutUserTask {

    public interface LogoutCallback {
        void onLogoutComplete();
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Context context;
    private final LogoutCallback callback;

    public LogoutUserTask(Context context, LogoutCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void execute() {
        executor.execute(() -> {
            // Background work: Firebase sign out
            FirebaseAuth.getInstance().signOut();

            // Return to main thread
            handler.post(callback::onLogoutComplete);
        });
    }
}
