package com.coralb_mayn_yehudas.cookit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat themeSwitch;
    private Spinner languageSpinner;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this); // החלת שפה לפני טעינת העיצוב
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        themeSwitch = findViewById(R.id.themeSwitch);
        languageSpinner = findViewById(R.id.languageSpinner);
        Button finishButton = findViewById(R.id.finishButton);

        // מצב כהה
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        themeSwitch.setChecked(darkMode);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // שפה נוכחית
        String savedLang = prefs.getString("language", "en");
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
        languageSpinner.setSelection(savedLang.equals("iw") ? 0 : 1);

        // שינוי מצב כהה
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES :
                    AppCompatDelegate.MODE_NIGHT_NO);
        });

        // שינוי שפה
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String lang = position == 0 ? "iw" : "en";
                String currentLang = prefs.getString("language", "en");

                if (!currentLang.equals(lang)) {
                    prefs.edit().putString("language", lang).apply();
                    LocaleHelper.setLocale(SettingsActivity.this, lang);
                    recreate(); // טוען את המסך מחדש עם שפה מעודכנת
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        finishButton.setOnClickListener(v -> {
            prefs.edit().putBoolean("settings_done", true).apply();
            startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
            finish();
        });
    }
}
