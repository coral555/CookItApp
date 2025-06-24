package com.coralb_mayn_yehudas.cookit;

import android.app.IntentService;
import android.content.Intent;

public class RecipeUpdateService extends IntentService {

    public static final String ACTION_SAVE_RECIPE = "com.coralb_mayn_yehudas.cookit.SAVE_RECIPE";
    public static final String EXTRA_RECIPE = "recipe";

    public RecipeUpdateService() {
        super("RecipeUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_SAVE_RECIPE.equals(intent.getAction())) {
            Recipe r = (Recipe) intent.getSerializableExtra(EXTRA_RECIPE);
            DataBaseHelper db = new DataBaseHelper(this);

            if (r.getId() == -1) {
                db.insertRecipe(r.getName(), r.getCategory(), r.getIngredients(), r.getSteps(), r.getTime(), r.getImageUri(), r.getUserId());
            } else {
                db.updateRecipe(r);
            }
        }
    }
}