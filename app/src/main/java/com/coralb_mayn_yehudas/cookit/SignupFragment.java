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

/**
 * SignupFragment handles user registration for the CookIt app.
 * It collects username, email, and password input from the user,
 * validates the input, and stores the new user in the local SQLite database.
 */
public class SignupFragment extends Fragment {

    private EditText usernameInput, emailInput, passwordInput; // Input fields for signup
    private Button signupButton; // Button to submit the form
    private DataBaseHelper db; // Reference to local database

    public SignupFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the signup fragment layout
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // connect UI components
        usernameInput = view.findViewById(R.id.signupUsernameEditText);
        emailInput = view.findViewById(R.id.signupEmailEditText);
        passwordInput = view.findViewById(R.id.signupPasswordEditText);
        signupButton = view.findViewById(R.id.signupButton);

        db = new DataBaseHelper(requireContext());

        // Handle signup button click
        signupButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) { // Validate input
                Toast.makeText(getContext(), getString(R.string.signup_error_empty), Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), getString(R.string.signup_error_email), Toast.LENGTH_SHORT).show();
            } else {  // Attempt to insert new user into database
                boolean success = db.insertUser(username, email, password);
                if (success) {
                    Toast.makeText(getContext(), getString(R.string.signup_success), Toast.LENGTH_SHORT).show();
                    usernameInput.setText("");
                    emailInput.setText("");
                    passwordInput.setText("");
                } else {
                    Toast.makeText(getContext(), getString(R.string.signup_error_exists), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
