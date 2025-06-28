package com.coralb_mayn_yehudas.cookit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

/**
 * LocaleHelper - utility class that handles language localization in the app.
 * It allows saving, retrieving, and applying a selected language using SharedPreferences.
 */
public class LocaleHelper {

    /**
     * Applies the saved language preference (default: "en") to the app context.
     * Should be called early in the activity lifecycle (before setContentView).
     */
    public static void applySavedLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String lang = prefs.getString("language", "en");
        setLocale(context, lang);
    }

    /**
     * Retrieves the current language setting from SharedPreferences.
     * return Language code ("en", "he")
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getString("language", "en");
    }

    /**
     * Updates the app's locale and text direction based on the given language code.
     * get context The current context
     * get languageCode The ISO language code to apply ("en", "he")
     */
    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale); // Ensures correct RTL layout for languages like Hebrew
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
