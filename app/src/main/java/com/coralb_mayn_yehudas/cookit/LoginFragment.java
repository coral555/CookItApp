package com.coralb_mayn_yehudas.cookit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.login_error_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isValid = db.checkUser(username, password);
            if (isValid) {
                Toast.makeText(getContext(), getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                // 🟢 שליפת מזהה המשתמש ושמירה בהעדפות
                int userId = db.getUserId(username);
                SharedPreferences.Editor editor = requireContext()
                        .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        .edit();
                editor.putInt("user_id", userId);
                editor.apply();

                SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                boolean settingsDone = prefs.getBoolean("settings_done", false);

                Intent intent = new Intent(requireContext(),
                        settingsDone ? MainActivity.class : SettingsActivity.class);
                startActivity(intent);
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), getString(R.string.login_error_invalid), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
