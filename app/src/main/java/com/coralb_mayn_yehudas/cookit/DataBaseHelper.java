package com.coralb_mayn_yehudas.cookit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * DataBaseHelper class manages local SQLite database interactions for the CookIt app.
 * It includes tables for users, categories, and recipes, all linked by user_id for multi-user support.
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "CookIt.db";
    private static final int    DATABASE_VERSION = 4;

    // --- users table ---
    private static final String TABLE_USERS      = "users";
    private static final String COL_ID           = "id";
    private static final String COL_USERNAME     = "username";
    private static final String COL_EMAIL        = "email";
    private static final String COL_PASSWORD     = "password";

    // --- categories table ---
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COL_CAT_ID       = "id";
    private static final String COL_CAT_NAME     = "name";
    private static final String COL_CAT_USER_ID  = "user_id";

    // --- recipes table ---
    private static final String TABLE_RECIPES     = "recipes";
    private static final String COL_RECIPE_ID     = "id";
    private static final String COL_RECIPE_NAME   = "name";
    private static final String COL_RECIPE_CATEGORY    = "category";
    private static final String COL_RECIPE_INGREDIENTS = "ingredients";
    private static final String COL_RECIPE_STEPS       = "steps";
    private static final String COL_RECIPE_TIME        = "time";
    private static final String COL_RECIPE_IMAGE       = "imageUri";
    private static final String COL_RECIPE_FAVORITE    = "favorite";  // 0 not favorite or 1 favorite
    private static final String COL_RECIPE_USER_ID     = "user_id";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is first created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create users table
        db.execSQL(
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_USERNAME   + " TEXT UNIQUE, " +
                        COL_EMAIL      + " TEXT, " +
                        COL_PASSWORD   + " TEXT" +
                        ")"
        );

        // create categories table
        db.execSQL(
                "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                        COL_CAT_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_CAT_NAME     + " TEXT, " +
                        COL_CAT_USER_ID  + " INTEGER" +
                        ")"
        );

        // create recipes table
        db.execSQL(
                "CREATE TABLE " + TABLE_RECIPES + " (" +
                        COL_RECIPE_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_RECIPE_NAME       + " TEXT, " +
                        COL_RECIPE_CATEGORY   + " TEXT, " +
                        COL_RECIPE_INGREDIENTS+ " TEXT, " +
                        COL_RECIPE_STEPS      + " TEXT, " +
                        COL_RECIPE_TIME       + " TEXT, " +
                        COL_RECIPE_IMAGE      + " TEXT, " +
                        COL_RECIPE_FAVORITE   + " INTEGER DEFAULT 0, " +
                        COL_RECIPE_USER_ID    + " INTEGER" +
                        ")"
        );
    }

    /**
     * Called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // ---------- users ----------
    public boolean insertUser(String username, String email, String password) {
        // Opens the database for writing (inserts, updates, deletes)
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_EMAIL,    email);
        cv.put(COL_PASSWORD, password);
        long id = db.insert(TABLE_USERS, null, cv);
        return id != -1;
    }

    public boolean checkUser(String username, String password) {
        // Opens the database for reading (select queries only)
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS + " WHERE " +
                        COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{ username, password }
        );
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public int getUserId(String username) {
        // Opens the database for reading (select queries only)
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS + " WHERE username=?",
                new String[]{ username }
        );
        int id = -1;
        if (c.moveToFirst()) {
            id = c.getInt(0);
        }
        c.close();
        return id;
    }

    // ---------- categories ----------
    public boolean insertCategory(String name, int userId) {
        // Opens the database for writing (inserts, updates, deletes)
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name);
        cv.put(COL_CAT_USER_ID, userId);
        long id = db.insert(TABLE_CATEGORIES, null, cv);
        return id != -1;
    }

    public boolean categoryExists(String name, int userId) {
        // Opens the database for reading (select queries only)
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + TABLE_CATEGORIES + " WHERE name=? AND user_id=?",
                new String[]{ name, String.valueOf(userId) }
        );
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public ArrayList<String> getAllCategories(int userId) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(); // Opens the database for reading (select queries only)
        Cursor c = db.rawQuery(
                "SELECT name FROM " + TABLE_CATEGORIES + " WHERE user_id=?",
                new String[]{ String.valueOf(userId) }
        );
        while (c.moveToNext()) {
            list.add(c.getString(0));
        }
        c.close();
        return list;
    }

    // ---------- recipes ----------
    public boolean insertRecipe(String name, String category, String ingredients, String steps, String time, String imageUri, int userId) {
        SQLiteDatabase db = getWritableDatabase(); // Opens the database for writing (inserts, updates, deletes)
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME,        name);
        cv.put(COL_RECIPE_CATEGORY,    category);
        cv.put(COL_RECIPE_INGREDIENTS, ingredients);
        cv.put(COL_RECIPE_STEPS,       steps);
        cv.put(COL_RECIPE_TIME,        time);
        cv.put(COL_RECIPE_IMAGE,       imageUri);
        cv.put(COL_RECIPE_FAVORITE, 0);
        cv.put(COL_RECIPE_USER_ID,     userId);
        long id = db.insert(TABLE_RECIPES, null, cv);
        return id != -1;
    }

    public List<Recipe> getAllRecipes(int userId) {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(); // Opens the database for reading (select queries only)
        Cursor c = db.rawQuery(
                "SELECT " +
                        COL_RECIPE_ID + ", " +
                        COL_RECIPE_NAME + ", " +
                        COL_RECIPE_CATEGORY + ", " +
                        COL_RECIPE_INGREDIENTS + ", " +
                        COL_RECIPE_STEPS + ", " +
                        COL_RECIPE_TIME + ", " +
                        COL_RECIPE_IMAGE + ", " +
                        COL_RECIPE_FAVORITE + ", " +
                        COL_RECIPE_USER_ID +
                        " FROM " + TABLE_RECIPES +
                        " WHERE user_id=?",
                new String[]{ String.valueOf(userId) }
        );
        while (c.moveToNext()) {
            Recipe r = new Recipe(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6),
                    c.getInt(7) == 1,
                    c.getInt(8)
            );
            list.add(r);
        }
        c.close();
        return list;
    }

    public boolean updateRecipe(Recipe r) {
        SQLiteDatabase db = getWritableDatabase(); // Opens the database for writing (inserts, updates, deletes)
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME,        r.getName());
        cv.put(COL_RECIPE_CATEGORY,    r.getCategory());
        cv.put(COL_RECIPE_INGREDIENTS, r.getIngredients());
        cv.put(COL_RECIPE_STEPS,       r.getSteps());
        cv.put(COL_RECIPE_TIME,        r.getTime());
        cv.put(COL_RECIPE_IMAGE,       r.getImageUri());
        cv.put(COL_RECIPE_FAVORITE,    r.isFavorite() ? 1 : 0);
        cv.put(COL_RECIPE_USER_ID,     r.getUserId());
        int rows = db.update(
                TABLE_RECIPES,
                cv,
                COL_RECIPE_ID + "=?",
                new String[]{ String.valueOf(r.getId()) }
        );
        return rows > 0;
    }

    public boolean deleteRecipe(int recipeId) {
        SQLiteDatabase db = getWritableDatabase(); // Opens the database for writing (inserts, updates, deletes)
        int rows = db.delete(
                TABLE_RECIPES,
                COL_RECIPE_ID + "=?",
                new String[]{ String.valueOf(recipeId) }
        );
        return rows > 0;
    }

    public void deleteAllUserData(int userId) {
        SQLiteDatabase db = this.getWritableDatabase(); // Opens the database for writing (inserts, updates, deletes)
        db.beginTransaction();
        try {
            db.delete("recipes", "user_id = ?", new String[]{String.valueOf(userId)});
            db.delete("categories", "user_id = ?", new String[]{String.valueOf(userId)});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public boolean recipeExists(String name, String category, int userId) {
        SQLiteDatabase db = this.getReadableDatabase(); // Opens the database for reading (select queries only)
        Cursor cursor = db.rawQuery(
                "SELECT id FROM recipes WHERE name=? AND category=? AND user_id=?",
                new String[]{ name, category, String.valueOf(userId) }
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private Recipe createRecipeFromCursor(Cursor c) {
        return new Recipe(
                c.getInt(c.getColumnIndexOrThrow(COL_RECIPE_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_RECIPE_NAME)),
                c.getString(c.getColumnIndexOrThrow(COL_RECIPE_CATEGORY)),
                c.getString(c.getColumnIndexOrThrow(COL_RECIPE_INGREDIENTS)),
                c.getString(c.getColumnIndexOrThrow(COL_RECIPE_STEPS)),
                c.getString(c.getColumnIndexOrThrow(COL_RECIPE_TIME)),
                c.getString(c.getColumnIndexOrThrow(COL_RECIPE_IMAGE)),
                c.getInt(c.getColumnIndexOrThrow(COL_RECIPE_FAVORITE)) == 1,
                c.getInt(c.getColumnIndexOrThrow(COL_RECIPE_USER_ID))
        );
    }

    public List<Recipe> getFavoriteRecipes(int userId) {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase(); // Opens the database for reading (select queries only)
        // Executes a raw SQL query to select all favorite recipes for the given user
        Cursor cursor = db.rawQuery("SELECT * FROM recipes WHERE user_id=? AND favorite=1", new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) { // Iterates over the result set and converts each row into a Recipe object
            list.add(createRecipeFromCursor(cursor));
        }
        cursor.close(); // close the cursor to avoid memory leaks
        return list;
    }

}
