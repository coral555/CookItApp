package com.coralb_mayn_yehudas.cookit;

import java.io.Serializable;

/**
 * recipe is a model class representing a recipe entity in the app.
 * implements serializable to allow passing between activities or fragments.
 */
public class Recipe implements Serializable {
    private int id; // Unique ID of the recipe in the database
    private String name;  // Name/title of the recipe
    private String category; // Recipe category
    private String ingredients; // Ingredients list as a string
    private String steps; // Cooking/preparation steps
    private String time; // Preparation time
    private String imageUri; // URI to the recipe's image (optional)
    private boolean favorite; // Whether the recipe is marked as favorite
    private int userId; // ID of the user who owns this recipe

    /**
     * Constructs a new Recipe object with all fields initialized.
     */
    public Recipe(int id, String name, String category, String ingredients, String steps, String time, String imageUri, boolean favorite, int userId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.ingredients = ingredients;
        this.steps = steps;
        this.time = time;
        this.imageUri = imageUri;
        this.favorite = favorite;
        this.userId = userId;
    }

    // --- getters ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public String getTime() {
        return time;
    }

    public String getImageUri() {
        return imageUri;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public int getUserId() {
        return userId;
    }

    // --- setters ---

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
