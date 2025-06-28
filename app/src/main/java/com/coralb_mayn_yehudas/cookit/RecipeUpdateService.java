package com.coralb_mayn_yehudas.cookit;

import android.app.IntentService;
import android.content.Intent;

/**
 * RecipeUpdateService is a background service that handles saving or updating Recipe objects.
 * It receives an intent with a serialized Recipe and either inserts it into the database or updates it.
 */
public class RecipeUpdateService extends IntentService {

    public static final String ACTION_SAVE_RECIPE = "com.coralb_mayn_yehudas.cookit.SAVE_RECIPE";
    public static final String EXTRA_RECIPE = "recipe";

    /**
     * Default constructor specifying the name of the worker thread.
     */
    public RecipeUpdateService() {
        super("RecipeUpdateService");
    }

    /**
     * Handles incoming intents. If the intent action matches SAVE_RECIPE,
     * the provided Recipe is either inserted or updated in the database.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_SAVE_RECIPE.equals(intent.getAction())) {
            // retrieve the recipe from the intent
            Recipe r = (Recipe) intent.getSerializableExtra(EXTRA_RECIPE);
            DataBaseHelper db = new DataBaseHelper(this);

            // insert new recipe if ID is -1, otherwise update existing
            if (r.getId() == -1) {
                db.insertRecipe(r.getName(), r.getCategory(), r.getIngredients(), r.getSteps(), r.getTime(), r.getImageUri(), r.getUserId());
            } else {
                db.updateRecipe(r);
            }
        }
    }
}