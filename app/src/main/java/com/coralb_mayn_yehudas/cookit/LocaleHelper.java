package com.coralb_mayn_yehudas.cookit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    public static void applySavedLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String lang = prefs.getString("language", "en");
        setLocale(context, lang);
    }

    public static void setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        config.setLayoutDirection(locale); // תמיכה בכיוון טקסט RTL בעברית
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
