package edu.uga.cs.ridesharingapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Load LoginFragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.loginFragmentContainer, new LoginFragment())
                    .commit();
        }
    }

    public void switchToRegister() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.loginFragmentContainer, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }

    public void switchToLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.loginFragmentContainer, new LoginFragment())
                .addToBackStack(null)
                .commit();
    }
}
