package com.coralb_mayn_yehudas.cookit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "CookIt.db";
    private static final int    DATABASE_VERSION = 3;  // bump when schema changes

    // --- table: users ---
    private static final String TABLE_USERS      = "users";
    private static final String COL_ID           = "id";
    private static final String COL_FIRST_NAME   = "first_name";
    private static final String COL_LAST_NAME    = "last_name";
    private static final String COL_USERNAME     = "username";
    private static final String COL_EMAIL        = "email";
    private static final String COL_PASSWORD     = "password";

    // --- table: categories ---
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COL_CAT_ID       = "id";
    private static final String COL_CAT_NAME     = "name";

    // --- table: recipes ---
    private static final String TABLE_RECIPES     = "recipes";
    private static final String COL_RECIPE_ID     = "id";
    private static final String COL_RECIPE_NAME   = "name";
    private static final String COL_RECIPE_CATEGORY    = "category";
    private static final String COL_RECIPE_INGREDIENTS = "ingredients";
    private static final String COL_RECIPE_STEPS       = "steps";
    private static final String COL_RECIPE_TIME        = "time";
    private static final String COL_RECIPE_IMAGE       = "imageUri";
    private static final String COL_RECIPE_FAVORITE    = "favorite";  // 0 or 1

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // users
        db.execSQL(
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_FIRST_NAME + " TEXT, " +
                        COL_LAST_NAME  + " TEXT, " +
                        COL_USERNAME   + " TEXT UNIQUE, " +
                        COL_EMAIL      + " TEXT, " +
                        COL_PASSWORD   + " TEXT" +
                        ")"
        );

        // categories
        db.execSQL(
                "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                        COL_CAT_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_CAT_NAME + " TEXT UNIQUE" +
                        ")"
        );

        // recipes (with favorite flag)
        db.execSQL(
                "CREATE TABLE " + TABLE_RECIPES + " (" +
                        COL_RECIPE_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_RECIPE_NAME       + " TEXT, " +
                        COL_RECIPE_CATEGORY   + " TEXT, " +
                        COL_RECIPE_INGREDIENTS+ " TEXT, " +
                        COL_RECIPE_STEPS      + " TEXT, " +
                        COL_RECIPE_TIME       + " TEXT, " +
                        COL_RECIPE_IMAGE      + " TEXT, " +
                        COL_RECIPE_FAVORITE   + " INTEGER DEFAULT 0" +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // simple drop & recreate (data will be lost on upgrade)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // ---------- users ----------

    public boolean insertUser(String firstName, String lastName, String username, String email, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FIRST_NAME, firstName);
        cv.put(COL_LAST_NAME,  lastName);
        cv.put(COL_USERNAME,   username);
        cv.put(COL_EMAIL,      email);
        cv.put(COL_PASSWORD,   password);
        long id = db.insert(TABLE_USERS, null, cv);
        return id != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USERS + " WHERE " +
                        COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{ username, password }
        );
        boolean exists = (c.getCount() > 0);
        c.close();
        return exists;
    }

    // ---------- categories ----------

    public boolean insertCategory(String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name);
        long id = db.insert(TABLE_CATEGORIES, null, cv);
        return id != -1;
    }

    public boolean categoryExists(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT 1 FROM " + TABLE_CATEGORIES + " WHERE " + COL_CAT_NAME + "=?",
                new String[]{ name }
        );
        boolean exists = (c.getCount() > 0);
        c.close();
        return exists;
    }

    public ArrayList<String> getAllCategories() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + COL_CAT_NAME + " FROM " + TABLE_CATEGORIES,
                null
        );
        while (c.moveToNext()) {
            list.add(c.getString(0));
        }
        c.close();
        return list;
    }

    // ---------- recipes ----------

    public boolean insertRecipe(String name, String category, String ingredients, String steps, String time, String imageUri) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME,        name);
        cv.put(COL_RECIPE_CATEGORY,    category);
        cv.put(COL_RECIPE_INGREDIENTS, ingredients);
        cv.put(COL_RECIPE_STEPS,       steps);
        cv.put(COL_RECIPE_TIME,        time);
        cv.put(COL_RECIPE_IMAGE,       imageUri);
        // favorite defaults to 0
        long id = db.insert(TABLE_RECIPES, null, cv);
        return id != -1;
    }

    /** Fetch all recipes from the database */
    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " +
                        COL_RECIPE_ID + ", " +
                        COL_RECIPE_NAME + ", " +
                        COL_RECIPE_CATEGORY + ", " +
                        COL_RECIPE_INGREDIENTS + ", " +
                        COL_RECIPE_STEPS + ", " +
                        COL_RECIPE_TIME + ", " +
                        COL_RECIPE_IMAGE + ", " +
                        COL_RECIPE_FAVORITE +
                        " FROM " + TABLE_RECIPES,
                null
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
                    c.getInt(7) == 1
            );
            list.add(r);
        }
        c.close();
        return list;
    }

    /** Update an existing recipe */
    public boolean updateRecipe(Recipe r) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RECIPE_NAME,        r.getName());
        cv.put(COL_RECIPE_CATEGORY,    r.getCategory());
        cv.put(COL_RECIPE_INGREDIENTS, r.getIngredients());
        cv.put(COL_RECIPE_STEPS,       r.getSteps());
        cv.put(COL_RECIPE_TIME,        r.getTime());
        cv.put(COL_RECIPE_IMAGE,       r.getImageUri());
        cv.put(COL_RECIPE_FAVORITE,    r.isFavorite() ? 1 : 0);
        int rows = db.update(
                TABLE_RECIPES,
                cv,
                COL_RECIPE_ID + "=?",
                new String[]{ String.valueOf(r.getId()) }
        );
        return rows > 0;
    }

    /** Delete a recipe by its ID */
    public boolean deleteRecipe(int recipeId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(
                TABLE_RECIPES,
                COL_RECIPE_ID + "=?",
                new String[]{ String.valueOf(recipeId) }
        );
        return rows > 0;
    }
}
