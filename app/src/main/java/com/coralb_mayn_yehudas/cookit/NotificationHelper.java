package com.coralb_mayn_yehudas.cookit;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

/**
 * Notification helper manages the creation and display of daily recipe reminder notifications.
 * It creates a notification channel and builds notifications using NotificationCompat.
 */
public class NotificationHelper {

    private static final String CHANNEL_ID = "recipe_reminder_channel"; // unique ID for the channel
    private static final String CHANNEL_NAME = "Recipe Reminder";  // channel name shown to the user
    private static final int NOTIFICATION_ID = 1001; // Notification ID for updates

    /**
     * Creates a notification channel for recipe reminders.
     * Must be called once before posting notifications
     * get context Application context used to access the NotificationManager
     */
    public static void createChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Daily recipe suggestions");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    /**
     * Displays a notification to the user with a daily recipe suggestion.
     * tapping the notification opens the Main Activity.
     * get context Application context, title of the notification, body text of the notification
     */
    @SuppressLint("MissingPermission")
    public static void showNotification(Context context, String title, String text) {
        // Intent to open MainActivity when the user taps the notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with a custom icon if desired
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);  // Dismiss notification when clicked

        // Display the notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }
}