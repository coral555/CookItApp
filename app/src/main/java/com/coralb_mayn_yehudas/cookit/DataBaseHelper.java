package com.coralb_mayn_yehudas.cookit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CookIt.db";
    private static final int DATABASE_VERSION = 2;

    // טבלת משתמשים
    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_FIRST_NAME = "first_name";
    private static final String COL_LAST_NAME = "last_name";
    private static final String COL_USERNAME = "username";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";

    // טבלת קטגוריות
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COL_CATEGORY_NAME = "name";

    // טבלת מתכונים
    private static final String TABLE_RECIPES = "recipes";
    private static final String COL_RECIPE_NAME = "name";
    private static final String COL_RECIPE_CATEGORY = "category";
    private static final String COL_RECIPE_INGREDIENTS = "ingredients";
    private static final String COL_RECIPE_STEPS = "steps";
    private static final String COL_RECIPE_TIME = "time";
    private static final String COL_RECIPE_IMAGE = "imageUri";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // יצירת טבלת משתמשים
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FIRST_NAME + " TEXT, " +
                COL_LAST_NAME + " TEXT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_EMAIL + " TEXT, " +
                COL_PASSWORD + " TEXT)");

        // יצירת טבלת קטגוריות
        db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CATEGORY_NAME + " TEXT UNIQUE)");

        // יצירת טבלת מתכונים
        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT, " +
                COL_RECIPE_CATEGORY + " TEXT, " +
                COL_RECIPE_INGREDIENTS + " TEXT, " +
                COL_RECIPE_STEPS + " TEXT, " +
                COL_RECIPE_TIME + " TEXT, " +
                COL_RECIPE_IMAGE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    // ---------- פונקציונליות משתמשים ----------
    public boolean insertUser(String firstName, String lastName, String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FIRST_NAME, firstName);
        values.put(COL_LAST_NAME, lastName);
        values.put(COL_USERNAME, username);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE username=? AND password=?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // ---------- פונקציונליות קטגוריות ----------
    public boolean insertCategory(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CATEGORY_NAME, name);
        long result = db.insert(TABLE_CATEGORIES, null, values);
        return result != -1;
    }

    public boolean categoryExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORIES + " WHERE name = ?", new String[]{name});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public ArrayList<String> getAllCategories() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_CATEGORY_NAME + " FROM " + TABLE_CATEGORIES, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0));
        }
        cursor.close();
        return list;
    }

    // ---------- פונקציונליות מתכונים ----------
    public boolean insertRecipe(String name, String category, String ingredients, String steps, String time, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RECIPE_NAME, name);
        values.put(COL_RECIPE_CATEGORY, category);
        values.put(COL_RECIPE_INGREDIENTS, ingredients);
        values.put(COL_RECIPE_STEPS, steps);
        values.put(COL_RECIPE_TIME, time);
        values.put(COL_RECIPE_IMAGE, imageUri);
        long result = db.insert(TABLE_RECIPES, null, values);
        return result != -1;
    }
}
