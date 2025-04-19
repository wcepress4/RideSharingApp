package edu.uga.cs.ridesharingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        EditText emailInput = view.findViewById(R.id.emailInput);
        EditText passwordInput = view.findViewById(R.id.passwordInput);
        EditText retypePasswordInput = view.findViewById(R.id.retypePasswordInput);
        Button registerButton = view.findViewById(R.id.registerButton);
        TextView switchToLogin = view.findViewById(R.id.switchToLogin);

        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String retypePassword = retypePasswordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || retypePassword.isEmpty()) {
                Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(retypePassword)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Execute async registration task
            new RegisterUserTask(getContext()).execute(email, password);
        });

        switchToLogin.setOnClickListener(v -> {
            ((LoginActivity) getActivity()).switchToLogin();
        });

        return view;
    }
}
