package com.coralb_mayn_yehudas.cookit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Random;

/**
 * RecipeAlarmReceiver is triggered by a daily alarm (AlarmManager).
 * It checks the user's favorite or all recipes and sends a notification with a random suggestion.
 * If no recipes exist, a generic encouragement notification is shown instead.
 */
public class RecipeAlarmReceiver extends BroadcastReceiver {

    /**
     * Called when the alarm goes off. This method:
     * - Loads the user's favorite recipes (if any),
     * - Picks one at random,
     * - Builds a localized notification using NotificationHelper.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        DataBaseHelper db = new DataBaseHelper(context);
        // Get the logged-in user's ID
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) return; // No user logged in

        // Try to fetch favorite recipes first
        List<Recipe> favorites = db.getFavoriteRecipes(userId);
        Recipe selected = null;

        if (!favorites.isEmpty()) {
            selected = favorites.get(new Random().nextInt(favorites.size()));
        } else {
            // Fallback to any available recipe
            List<Recipe> all = db.getAllRecipes(userId);
            if (!all.isEmpty()) {
                selected = all.get(new Random().nextInt(all.size()));
            }
        }
        // Show a personalized notification if a recipe is found
        if (selected != null) {
            String title = context.getString(R.string.notification_title_with_recipe, selected.getName());
            String text = context.getString(R.string.notification_text_with_recipe);
            NotificationHelper.showNotification(context, title, text);
        } else { // Fallback: no recipes at all
            String title = context.getString(R.string.notification_title_no_recipes);
            String text = context.getString(R.string.notification_text_no_recipes);
            NotificationHelper.showNotification(context, title, text);
        }
    }
}
