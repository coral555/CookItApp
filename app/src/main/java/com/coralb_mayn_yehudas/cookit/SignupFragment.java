package com.coralb_mayn_yehudas.cookit;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SignupFragment extends Fragment {

    private EditText firstNameInput, lastNameInput, usernameInput, emailInput, passwordInput;
    private Button signupButton;
    private DataBaseHelper db;

    public SignupFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // התחברות לרכיבי UI
        firstNameInput = view.findViewById(R.id.signupFirstNameEditText);
        lastNameInput = view.findViewById(R.id.signupLastNameEditText);
        usernameInput = view.findViewById(R.id.signupUsernameEditText);
        emailInput = view.findViewById(R.id.signupEmailEditText);
        passwordInput = view.findViewById(R.id.signupPasswordEditText);
        signupButton = view.findViewById(R.id.signupButton);

        db = new DataBaseHelper(requireContext());

        signupButton.setOnClickListener(v -> {
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!firstName.matches("^[a-zA-Z]{2,}$")) {
                Toast.makeText(getContext(), "First name must be at least 2 letters and only in English", Toast.LENGTH_SHORT).show();
            } else if (!lastName.matches("^[a-zA-Z]{2,}$")) {
                Toast.makeText(getContext(), "Last name must be at least 2 letters and only in English", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                boolean success = db.insertUser(firstName, lastName, username, email, password);
                if (success) {
                    Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    firstNameInput.setText("");
                    lastNameInput.setText("");
                    usernameInput.setText("");
                    emailInput.setText("");
                    passwordInput.setText("");
                } else {
                    Toast.makeText(getContext(), "Username already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
