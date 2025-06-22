package com.coralb_mayn_yehudas.cookit;

public class Recipe {

    private int     id;
    private String  name;
    private String  category;
    private String  ingredients;
    private String  steps;
    private String  time;
    private String  imageUri;
    private boolean favorite;

    public Recipe(int id,
                  String name,
                  String category,
                  String ingredients,
                  String steps,
                  String time,
                  String imageUri,
                  boolean favorite) {
        this.id          = id;
        this.name        = name;
        this.category    = category;
        this.ingredients = ingredients;
        this.steps       = steps;
        this.time        = time;
        this.imageUri    = imageUri;
        this.favorite    = favorite;
    }

    // --- getters ---

    public int     getId()          { return id; }
    public String  getName()        { return name; }
    public String  getCategory()    { return category; }
    public String  getIngredients() { return ingredients; }
    public String  getSteps()       { return steps; }
    public String  getTime()        { return time; }
    public String  getImageUri()    { return imageUri; }
    public boolean isFavorite()     { return favorite; }

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
}
