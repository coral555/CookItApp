package com.coralb_mayn_yehudas.cookit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private DataBaseHelper db;

    public LoginFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        usernameInput = view.findViewById(R.id.loginUsernameEditText);
        passwordInput = view.findViewById(R.id.loginPasswordEditText);
        loginButton = view.findViewById(R.id.loginButton);

        db = new DataBaseHelper(requireContext());

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean isValid = db.checkUser(username, password);
                if (isValid) {
                    Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                    // כאן אפשר להוסיף Intent ל־MainActivity
                } else {
                    Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
