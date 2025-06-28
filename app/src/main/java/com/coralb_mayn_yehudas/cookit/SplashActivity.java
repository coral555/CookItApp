package com.coralb_mayn_yehudas.cookit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.app.AlarmManager;
import android.app.PendingIntent;
import java.util.Calendar;
import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatDelegate;

import androidx.appcompat.app.AppCompatActivity;

/**
 * SplashActivity displays a splash screen for a few seconds on app launch.
 * It applies theme settings, sets up the daily recipe notification,
 * and then routes the user to either SettingsActivity (on first launch)
 * or LoginActivity (on subsequent launches).
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode setting based on stored preference
        boolean isDarkMode = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize notification channel and daily alarm
        NotificationHelper.createChannel(this);
        scheduleDailyNotification();

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            boolean settingsDone = prefs.getBoolean("settings_done", false);

            Intent intent;
            if (!settingsDone) { // if first launch → show settings screen
                intent = new Intent(SplashActivity.this, SettingsActivity.class);
            } else { // otherwise → go to login screen
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();  // close splash activity so user can't return to it

        }, SPLASH_DISPLAY_LENGTH);
    }

    /**
     * Schedules a daily notification at a specific time (11:00 am).
     * The notification suggests a recipe based on user data.
     */
    @SuppressLint("ScheduleExactAlarm")
    private void scheduleDailyNotification() {
        Intent intent = new Intent(this, RecipeAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 0);

        long triggerAt = calendar.getTimeInMillis();
        if (System.currentTimeMillis() > triggerAt) {
            triggerAt += AlarmManager.INTERVAL_DAY; // Schedule for next day if time has passed
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
    }

}
