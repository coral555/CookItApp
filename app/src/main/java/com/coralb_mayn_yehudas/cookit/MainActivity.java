package com.coralb_mayn_yehudas.cookit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import java.util.Comparator;

import android.text.Editable;
import android.text.TextWatcher;
import android.app.AlarmManager;
import android.app.PendingIntent;

import java.util.Calendar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;

/**
 * This is the main screen of the app. It handles:
 * - Displaying a list of the user's recipes
 * - Adding, editing, and deleting recipes and categories
 * - Image handling via camera or gallery
 * - Daily notifications
 * - Dynamic search and filtering
 * - Navigation drawer with About, Settings, and Logout
 */
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    // UI components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private EditText searchInput;
    private Spinner categoryFilter;
    private RecyclerView recipesRecyclerView;
    private FloatingActionButton addRecipeButton;

    // Database
    private DataBaseHelper db;

    // User data
    private int userId;

    // State
    private List<Recipe> recipes;  // Filtered view
    private List<Recipe> allRecipes;  // All user recipes
    private RecipeAdapter adapter;

    private Uri selectedImageUri;
    private ImageView currentImagePreview;
    private View currentDialogView;
    private int currentFilterIndex = -1; // No filter applied

    /**
     * Handles result from CameraActivity (custom camera)
     * Launches the custom CameraActivity to capture an image.
     * Once the user captures a photo, this launcher receives the result.
     * If a valid image URI is returned, it is displayed in the preview,
     * and the "Delete Image" button is shown to allow clearing the image.
     */
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String uriString = result.getData()
                                    .getStringExtra(CameraActivity.EXTRA_IMAGE_URI);
                            if (uriString != null && currentImagePreview != null) {
                                // Parse and show image
                                selectedImageUri = Uri.parse(uriString);
                                currentImagePreview.setImageURI(selectedImageUri);
                                currentImagePreview.setVisibility(View.VISIBLE);
                                // Show delete button
                                Button deleteImageButton = currentDialogView.findViewById(R.id.deleteImageButton);
                                deleteImageButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
            );

    /**
     * Handles result from GalleryActivity (custom gallery picker)
     * Launches a custom GalleryActivity to allow the user to select an image from their gallery.
     * Once the user selects an image, this launcher receives the result.
     * If a valid image URI is returned, it is displayed in the preview,
     * and the "Delete Image" button is made visible for user convenience.
     */
    private final ActivityResultLauncher<Intent> galleryCustomLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selectedImage = result.getData().getData();
                            if (selectedImage != null && currentImagePreview != null) {
                                // Display selected image
                                selectedImageUri = selectedImage;
                                currentImagePreview.setImageURI(selectedImage);
                                currentImagePreview.setVisibility(View.VISIBLE);
                                // Show delete button
                                Button deleteImageButton = currentDialogView.findViewById(R.id.deleteImageButton);
                                deleteImageButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Check if user is logged in
        userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getInt("user_id", -1);
        if (userId == -1) {  // If not logged in, redirect to LoginActivity and clear the activity stack
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        // Load language preference and layout
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup notification channel and schedule daily notification
        NotificationHelper.createChannel(this);
        scheduleDailyNotification();

        db = new DataBaseHelper(this); // Initialize database

        // connect UI components
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuIcon = findViewById(R.id.menuIcon);
        searchInput = findViewById(R.id.searchInput);
        categoryFilter = findViewById(R.id.categoryFilter);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        addRecipeButton = findViewById(R.id.addRecipeButton);

        // Setup navigation menu
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(this::onNavItem);

        // Load user recipes
        allRecipes = db.getAllRecipes(userId);
        recipes = new ArrayList<>(allRecipes); // current display according to sorting

        // Setup RecyclerView adapter
        adapter = new RecipeAdapter(this, recipes, new RecipeAdapter.Listener() {
            @Override
            public void onView(Recipe r) {  // Show a dialog with full recipe details
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_view_recipe, null);

                ((TextView) view.findViewById(R.id.tvCategory)).setText(r.getCategory());
                ((TextView) view.findViewById(R.id.tvTime)).setText(r.getTime());
                ((TextView) view.findViewById(R.id.tvIngredients)).setText(r.getIngredients());
                ((TextView) view.findViewById(R.id.tvSteps)).setText(r.getSteps());

                ImageView img = view.findViewById(R.id.tvImage);
                if (r.getImageUri() != null) {
                    img.setImageURI(Uri.parse(r.getImageUri()));
                    img.setVisibility(View.VISIBLE);
                }

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(r.getName())
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }


            @Override
            public void onEdit(Recipe r) { // Open the edit dialog pre-filled with recipe details
                showAddRecipeDialog(r);
            }

            @Override
            public void onDelete(Recipe r) {
                // Delete recipe from DB on background thread
                new Thread(() -> {
                    if (db.deleteRecipe(r.getId())) {
                        // reload all recipes from db - fetch updated list
                        List<Recipe> updatedList = db.getAllRecipes(userId);

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();

                            allRecipes.clear();
                            allRecipes.addAll(updatedList);

                            // Reapply current filter or show updated list
                            if (currentFilterIndex != -1) {
                                runFilterAgain();
                            } else {
                                recipes.clear();
                                recipes.addAll(updatedList);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }).start();
            }


            @Override
            public void onFavoriteToggle(Recipe r) {
                r.setFavorite(!r.isFavorite());  // Toggle the favorite status of a recipe

                new Thread(() -> {
                    db.updateRecipe(r);

                    runOnUiThread(() -> {
                        // Refresh filtered or full list
                        if (currentFilterIndex != -1) {
                            runFilterAgain();
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }).start();
            }

        });
        // Final RecyclerView setup
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipesRecyclerView.setAdapter(adapter);
        // add recipe or category
        addRecipeButton.setOnClickListener(v -> showAddOptionsDialog());

        // Filter button opens filter dialog
        LinearLayout filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(v -> showFilterDialog());

        // Dynamic search as user types
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();

                new Thread(() -> {
                    List<Recipe> filtered;

                    if (query.isEmpty()) {
                        filtered = new ArrayList<>(allRecipes);
                    } else {
                        filtered = new ArrayList<>();
                        for (Recipe r : allRecipes) {
                            if (r.getName().toLowerCase().startsWith(query)) {
                                filtered.add(r);
                            }
                        }
                    }

                    runOnUiThread(() -> {
                        recipes.clear();
                        recipes.addAll(filtered);
                        adapter.notifyDataSetChanged();
                    });
                }).start();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Show the filter dialog with options for sorting and favorites.
     */
    private void showFilterDialog() {
        // Define filter/sort options (localized strings)
        String[] options = {
                getString(R.string.filter_name_asc), // Sort by name (A-Z)
                getString(R.string.filter_name_desc), // Sort by name (Z-A)
                getString(R.string.filter_category_asc), // Sort by category (A-Z)
                getString(R.string.filter_category_desc), // Sort by category (Z-A)
                getString(R.string.filter_time_asc), // Sort by time (shortest to longest)
                getString(R.string.filter_time_desc), // Sort by time (longest to shortest)
                getString(R.string.filter_favorites) // Show only favorite recipes
        };
        // Build and show an alert dialog for filter selection
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.filter_title))
                .setItems(options, (dialog, which) -> {
                    currentFilterIndex = which; // Save the selected filter index

                    // Reload all recipes from database before applying new filter
                    allRecipes = db.getAllRecipes(userId);
                    recipes.clear();
                    recipes.addAll(allRecipes);

                    switch (which) { // Apply the selected sorting/filtering logic
                        case 0: // Sort by name (A-Z)
                            recipes.sort(Comparator.comparing(Recipe::getName));
                            break;
                        case 1: // Sort by name (Z-A)
                            recipes.sort((a, b) -> b.getName().compareTo(a.getName()));
                            break;
                        case 2: // Sort by category (A-Z)
                            recipes.sort(Comparator.comparing(Recipe::getCategory));
                            break;
                        case 3:  // Sort by category (Z-A)
                            recipes.sort((a, b) -> b.getCategory().compareTo(a.getCategory()));
                            break;
                        case 4: // Sort by time (ascending) - converts time string to int
                            recipes.sort(Comparator.comparingInt(r -> {
                                try {
                                    return Integer.parseInt(r.getTime());
                                } catch (NumberFormatException e) {
                                    return Integer.MAX_VALUE; // Invalid time = push to bottom
                                }
                            }));
                            break;
                        case 5: // Sort by time (descending)
                            recipes.sort((a, b) -> {
                                try {
                                    return Integer.parseInt(b.getTime()) - Integer.parseInt(a.getTime());
                                } catch (NumberFormatException e) {
                                    return 0; // Skip invalid time
                                }
                            });
                            break;
                        case 6: // Show only favorite recipes
                            recipes.clear();
                            recipes.addAll(db.getFavoriteRecipes(userId));
                            break;
                    }
                    adapter.notifyDataSetChanged();  // Notify the adapter to update the UI
                })
                .setNegativeButton(android.R.string.cancel, null) // Cancel button closes dialog
                .show();
    }

    /**
     * Applies the last used filter again (after update/delete).
     * This is used to refresh the recipe list after updates (e.g., recipe deletion or favorite toggle),
     * without requiring the user to reopen the filter dialog.
     */
    private void runFilterAgain() {
        if (currentFilterIndex == -1) return;  // Exit if no filter was previously applied

        // Reload all recipes from the database
        allRecipes = db.getAllRecipes(userId);
        recipes.clear();

        if (currentFilterIndex == 6) { // If the selected filter is "Favorites", load only favorite recipes
            recipes.addAll(db.getFavoriteRecipes(userId));
        } else {
            recipes.addAll(allRecipes);
        }

        // Apply sorting or filtering based on the last selected filter index
        switch (currentFilterIndex) {
            case 0: // Sort by name (A-Z)
                recipes.sort(Comparator.comparing(Recipe::getName));
                break;
            case 1: // Sort by name (Z-A)
                recipes.sort((a, b) -> b.getName().compareTo(a.getName()));
                break;
            case 2: // Sort by category (A-Z)
                recipes.sort(Comparator.comparing(Recipe::getCategory));
                break;
            case 3: // Sort by category (Z-A)
                recipes.sort((a, b) -> b.getCategory().compareTo(a.getCategory()));
                break;
            case 4: // Sort by preparation time (ascending)
                recipes.sort(Comparator.comparingInt(r -> {
                    try {
                        return Integer.parseInt(r.getTime());
                    } catch (NumberFormatException e) {
                        return Integer.MAX_VALUE;  // Push invalid times to the end
                    }
                }));
                break;
            case 5: // Sort by preparation time (descending)
                recipes.sort((a, b) -> {
                    try {
                        return Integer.parseInt(b.getTime()) - Integer.parseInt(a.getTime());
                    } catch (NumberFormatException e) {
                        return 0; // Leave order unchanged if time is invalid
                    }
                });
                break;
        }
        adapter.notifyDataSetChanged(); // notify the RecyclerView adapter to refresh the UI
    }

    /**
     * Navigation drawer handler.
     * Handles navigation menu item selections from the NavigationDrawer.
     * Executes the corresponding action based on the selected menu item ID.
     */
    private boolean onNavItem(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_about) { // Show the About dialog with app and system info
            showAboutDialog();
        } else if (id == R.id.nav_settings) { // Open the Settings screen
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_delete_all) {
            // Ask for confirmation before deleting all user data (recipes and categories)
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_all_recipes)
                    .setMessage(R.string.delete_all_confirmation)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // Show a second final warning before deletion
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.delete_all_recipes)
                                .setMessage(R.string.delete_all_final_warning)
                                .setPositiveButton(android.R.string.ok, (dialog2, which2) -> {
                                    db.deleteAllUserData(userId); // Delete data from DB
                                    recipes.clear();
                                    adapter.notifyDataSetChanged(); // Refresh UI
                                    Toast.makeText(this, R.string.deleted_all_user_data, Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else if (id == R.id.nav_exit) {
            // Confirm logout, clear stored user ID, and redirect to login screen
            new AlertDialog.Builder(this)
                    .setTitle(R.string.logout_title)
                    .setMessage(R.string.logout_confirm)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                .edit()
                                .remove("user_id")
                                .apply();

                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return true;
        } else { // unrecognized menu item
            return false;
        }
        // Close the navigation drawer after handling the action
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Shows the About dialog with package and device info.
     */
    private void showAboutDialog() {
        String packageName = getPackageName(); // App's package identifier
        String osVersion = android.os.Build.VERSION.RELEASE; // Android version
        int apiLevel = android.os.Build.VERSION.SDK_INT;  // API level

        // Compose the dialog message using string resources and system info
        String msg = getString(R.string.app_name) + "\n\n" +
                getString(R.string.about_package_label) + ": " + packageName + "\n\n" +
                getString(R.string.about_os_version) + ": " + osVersion + " (API " + apiLevel + ")\n\n" +
                getString(R.string.about_submitted_by) + ": " + getString(R.string.about_names) + "\n\n" +
                getString(R.string.about_submission_date) + ": 29.06.2025";

        // Show alert dialog with the info
        new AlertDialog.Builder(this)
                .setTitle(R.string.about)
                .setMessage(msg)
                .setPositiveButton(R.string.dialog_cancel, null)
                .show();
    }

    /**
     * Displays a dialog with two options:
     * 1. Add a new recipe
     * 2. Add a new category
     * Based on user selection, it either opens the recipe creation dialog
     * or the category creation dialog.
     */
    private void showAddOptionsDialog() {
        String[] options = { // Options for the dialog (localized strings)
                getString(R.string.dialog_add_recipe), // add recipe
                getString(R.string.add_category_title)  // add category
        };
        // Create and display a simple options dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_choose_action) // choose action
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddRecipeDialog(null);  // Open dialog to add a new recipe
                    } else {  // Open dialog to add a new category
                        showAddCategoryDialog(null, null, null);
                    }
                })
                .show();
    }

    /**
     * Displays a dialog to create or edit a recipe.
     * If an existing recipe is passed, the dialog is pre-filled with its data
     * for editing. Otherwise, it's used for creating a new recipe.
     *
     * The dialog allows the user to input:
     * - Recipe name
     * - Category (with dynamic addition option)
     * - Ingredients
     * - Steps
     * - Time (in minutes)
     * - Optional image (from camera or gallery)
     * Validations performed for empty fields, time value, and duplicate recipes.
     */
    private void showAddRecipeDialog(@Nullable Recipe existing) {
        // Inflate dialog layout and assign to currentDialogView for later access (e.g., image)
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_recipe, null);
        currentDialogView = dialogView;

        // Get references to dialog UI elements
        EditText nameInput = dialogView.findViewById(R.id.recipeName);
        Spinner categorySpinner = dialogView.findViewById(R.id.recipeCategory);
        EditText ingredientsInput = dialogView.findViewById(R.id.recipeIngredients);
        EditText stepsInput = dialogView.findViewById(R.id.recipeSteps);
        EditText timeInput = dialogView.findViewById(R.id.recipeTime);
        ImageView imagePreview = dialogView.findViewById(R.id.recipeImage);
        Button pickImageButton = dialogView.findViewById(R.id.pickImageButton);
        Button deleteImageButton = dialogView.findViewById(R.id.deleteImageButton);
        Button selectFromGalleryButton = dialogView.findViewById(R.id.selectFromGalleryButton);
        currentImagePreview = imagePreview;

        // Load user categories and append "Add Category" option
        ArrayList<String> cats = db.getAllCategories(userId);
        cats.add(getString(R.string.add_category_option));
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cats);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(catAdapter);

        // If editing existing recipe – prefill fields
        if (existing != null) {
            nameInput.setText(existing.getName());
            ingredientsInput.setText(existing.getIngredients());
            stepsInput.setText(existing.getSteps());
            timeInput.setText(existing.getTime());

            int pos = cats.indexOf(existing.getCategory());
            if (pos >= 0) categorySpinner.setSelection(pos);

            if (existing.getImageUri() != null) {
                // Load existing image
                selectedImageUri = Uri.parse(existing.getImageUri());
                imagePreview.setImageURI(selectedImageUri);
                imagePreview.setVisibility(View.VISIBLE);
                deleteImageButton.setVisibility(View.VISIBLE);
            } else {
                // Show default icon if no image exists
                imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
                imagePreview.setVisibility(View.VISIBLE);
                deleteImageButton.setVisibility(View.GONE);
            }
        } else {
            // For new recipe: reset everything
            selectedImageUri = null;
            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
            imagePreview.setVisibility(View.VISIBLE);
            deleteImageButton.setVisibility(View.GONE);
        }
        // If user selects "Add Category" from spinner
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (cats.get(position).equals(getString(R.string.add_category_option))) {
                    showAddCategoryDialog(categorySpinner, catAdapter, cats);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Handle camera image selection
        pickImageButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            } else {
                cameraLauncher.launch(new Intent(this, CameraActivity.class));
            }
        });

        // Handle gallery image selection
        selectFromGalleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GalleryActivity.class);
            galleryCustomLauncher.launch(intent);
        });

        // Delete selected image
        deleteImageButton.setOnClickListener(v -> {
            // Clear selected image and reset to default icon
            selectedImageUri = null;
            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
            imagePreview.setVisibility(View.VISIBLE);
            deleteImageButton.setVisibility(View.GONE);
        });

        // Determine dialog title based on mode
        String title = existing == null
                ? getString(R.string.dialog_add_recipe)
                : getString(R.string.dialog_edit_recipe);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        // Handle save logic
        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                String cat = categorySpinner.getSelectedItem().toString();
                String ingr = ingredientsInput.getText().toString().trim();
                String steps = stepsInput.getText().toString().trim();
                String time = timeInput.getText().toString().trim();
                String uri = selectedImageUri != null ? selectedImageUri.toString() : null;

                // Basic input validation
                if (name.isEmpty() || cat.isEmpty() || ingr.isEmpty() || steps.isEmpty() || time.isEmpty()) {
                    Toast.makeText(this, R.string.dialog_fill_fields, Toast.LENGTH_SHORT).show();
                    return;
                }

                int timeValue;
                try {
                    timeValue = Integer.parseInt(time);
                    if (timeValue <= 0) {
                        Toast.makeText(this, getString(R.string.error_time_positive), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, getString(R.string.error_time_invalid), Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(() -> {  // Insert or update recipe in DB
                    if (existing == null) {
                        // Insertion: check for duplicate
                        if (db.recipeExists(name, cat, userId)) {
                            runOnUiThread(() -> Toast.makeText(this, R.string.duplicate_recipe_error, Toast.LENGTH_SHORT).show());
                            return;
                        }
                        db.insertRecipe(name, cat, ingr, steps, time, uri, userId);
                    } else {
                        boolean nameChanged = !existing.getName().equals(name);
                        boolean categoryChanged = !existing.getCategory().equals(cat);
                        if ((nameChanged || categoryChanged) && db.recipeExists(name, cat, userId)) {
                            runOnUiThread(() -> Toast.makeText(this, R.string.duplicate_recipe_error, Toast.LENGTH_SHORT).show());
                            return;
                        }

                        // Update recipe
                        existing.setName(name);
                        existing.setCategory(cat);
                        existing.setIngredients(ingr);
                        existing.setSteps(steps);
                        existing.setTime(time);
                        existing.setImageUri(uri);
                        db.updateRecipe(existing);
                    }

                    // Refresh recipe list
                    List<Recipe> updatedList = db.getAllRecipes(userId);
                    runOnUiThread(() -> {
                        allRecipes.clear();
                        allRecipes.addAll(updatedList);
                        recipes.clear();
                        recipes.addAll(updatedList);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    });
                }).start();
            });
        });

        dialog.show();
    }

    /**
     * Displays a dialog to add a new recipe category.
     * - If the category doesn't already exist, it will be inserted into the database.
     * - If provided, updates the given Spinner and its adapter with the new category list.
     * - Ensures user can't leave the category spinner on "Add Category" option without entering a real category.
     */
    private void showAddCategoryDialog(@Nullable Spinner spinner, @Nullable ArrayAdapter<String> adapter, @Nullable ArrayList<String> cats) {
        EditText input = new EditText(this);
        input.setHint(R.string.enter_category_name);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_category_title)
                .setView(input)
                .setPositiveButton(R.string.add, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            addButton.setOnClickListener(v -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(this, R.string.empty_category, Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(() -> {
                    // Insert only if category does not exist
                    boolean inserted = false;
                    if (!db.categoryExists(name, userId)) {
                        db.insertCategory(name, userId);
                        inserted = true;
                    }
                    // Refresh category list
                    ArrayList<String> updatedCats = db.getAllCategories(userId);
                    updatedCats.add(getString(R.string.add_category_option));

                    boolean finalInserted = inserted;
                    runOnUiThread(() -> {
                        // Show feedback
                        if (finalInserted) {
                            Toast.makeText(this, R.string.category_added_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.category_exists, Toast.LENGTH_SHORT).show();
                        }
                        // Update UI spinner if passed
                        if (spinner != null && adapter != null && cats != null) {
                            cats.clear();
                            cats.addAll(updatedCats);
                            adapter.notifyDataSetChanged();
                            int pos = cats.indexOf(name);
                            if (pos >= 0) spinner.setSelection(pos);
                        }

                        dialog.dismiss();
                    });
                }).start();
            });

            cancelButton.setOnClickListener(v -> {
                // If no spinner involved — just dismiss
                if (spinner == null) {
                    dialog.dismiss(); // User clicked "Add Category" directly — allow cancel
                } else { // Prevent leaving "Add Category" selected if no real categories
                    if (cats == null || cats.size() <= 1) {
                        Toast.makeText(this, getString(R.string.must_add_category_first), Toast.LENGTH_SHORT).show();
                    } else {
                        spinner.setSelection(0);
                        dialog.dismiss();
                    }
                }
            });
        });

        dialog.show();
    }

    /**
     * Requests camera permission if needed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Launch camera if permission was granted
            cameraLauncher.launch(new Intent(this, CameraActivity.class));
        }
    }

    /**
     * Schedule a daily notification recommending a recipe.
     */
    private void scheduleDailyNotification() {
        Intent intent = new Intent(this, RecipeAlarmReceiver.class);

        // Wrap the intent in a PendingIntent with immutable flag (for API 23+ safety)
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Get the system AlarmManager service
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Set the desired time for the notification
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // notification hour
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed today, schedule for the next day
        long triggerAt = calendar.getTimeInMillis();
        if (System.currentTimeMillis() > triggerAt) {
            triggerAt += AlarmManager.INTERVAL_DAY;
        }

        // Schedule the exact alarm (even if the device is idle)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
    }
}