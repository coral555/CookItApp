package com.coralb_mayn_yehudas.cookit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Random;

public class RecipeAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DataBaseHelper db = new DataBaseHelper(context);
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) return;

        // for favorites recipe
        List<Recipe> favorites = db.getFavoriteRecipes(userId);
        Recipe selected = null;

        if (!favorites.isEmpty()) {
            selected = favorites.get(new Random().nextInt(favorites.size()));
        } else {
            List<Recipe> all = db.getAllRecipes(userId);
            if (!all.isEmpty()) {
                selected = all.get(new Random().nextInt(all.size()));
            }
        }

        if (selected != null) {
            String title = context.getString(R.string.notification_title_with_recipe, selected.getName());
            String text = context.getString(R.string.notification_text_with_recipe);
            NotificationHelper.showNotification(context, title, text);
        } else {
            String title = context.getString(R.string.notification_title_no_recipes);
            String text = context.getString(R.string.notification_text_no_recipes);
            NotificationHelper.showNotification(context, title, text);
        }
    }
}
