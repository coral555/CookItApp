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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private EditText searchInput;
    private Spinner categoryFilter;
    private RecyclerView recipesRecyclerView;
    private FloatingActionButton addRecipeButton;

    private DataBaseHelper db;
    private Uri selectedImageUri;

    private List<Recipe> recipes;
    private RecipeAdapter adapter;

    private ImageView currentImagePreview;

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String uriString = result.getData()
                                    .getStringExtra(CameraActivity.EXTRA_IMAGE_URI);
                            if (uriString != null && currentImagePreview != null) {
                                selectedImageUri = Uri.parse(uriString);
                                currentImagePreview.setImageURI(selectedImageUri);
                                currentImagePreview.setVisibility(View.VISIBLE);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout        = findViewById(R.id.drawerLayout);
        navigationView      = findViewById(R.id.navigationView);
        menuIcon            = findViewById(R.id.menuIcon);
        searchInput         = findViewById(R.id.searchInput);
        categoryFilter      = findViewById(R.id.categoryFilter);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        addRecipeButton     = findViewById(R.id.addRecipeButton);

        db = new DataBaseHelper(this);

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(this::onNavItem);

        recipes = db.getAllRecipes();
        adapter = new RecipeAdapter(this, recipes, new RecipeAdapter.Listener() {
            @Override
            public void onView(Recipe r) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(r.getName())
                        .setMessage(
                                getString(R.string.category) + ": " + r.getCategory() + "\n" +
                                        getString(R.string.time) + ": " + r.getTime() + "\n\n" +
                                        getString(R.string.ingredients) + ":\n" + r.getIngredients() + "\n\n" +
                                        getString(R.string.steps) + ":\n" + r.getSteps()
                        )
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }

            @Override
            public void onEdit(Recipe r) {
                showAddRecipeDialog(r);
            }

            @Override
            public void onDelete(Recipe r) {
                if (db.deleteRecipe(r.getId())) {
                    recipes.remove(r);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFavoriteToggle(Recipe r) {
                r.setFavorite(!r.isFavorite());
                db.updateRecipe(r);
                adapter.notifyDataSetChanged();
            }
        });

        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipesRecyclerView.setAdapter(adapter);

        addRecipeButton.setOnClickListener(v -> showAddOptionsDialog());
    }

    private boolean onNavItem(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_about) {
            showAboutDialog();
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_exit) {
            finishAffinity();
        } else {
            return false;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showAboutDialog() {
        String packageName = getPackageName();
        String osVersion = android.os.Build.VERSION.RELEASE;
        int apiLevel = android.os.Build.VERSION.SDK_INT;

        String msg = getString(R.string.app_name) + "\n\n" +
                getString(R.string.about_package_label) + ": " + packageName + "\n\n" +
                getString(R.string.about_os_version) + ": " + osVersion + " (API " + apiLevel + ")\n\n" +
                getString(R.string.about_submitted_by) + ": " + getString(R.string.about_names) + "\n\n" +
                getString(R.string.about_submission_date) + ": 29.06.2025";

        new AlertDialog.Builder(this)
                .setTitle(R.string.about)
                .setMessage(msg)
                .setPositiveButton(R.string.dialog_cancel, null)
                .show();
    }




    private void showAddOptionsDialog() {
        String[] options = {
                getString(R.string.dialog_add_recipe),
                getString(R.string.add_category_title)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_choose_action)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showAddRecipeDialog(null);
                    } else {
                        showAddCategoryDialog(null, null, null);
                    }
                })
                .show();
    }

    private void showAddRecipeDialog(@Nullable Recipe existing) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_recipe, null);

        EditText nameInput        = dialogView.findViewById(R.id.recipeName);
        Spinner categorySpinner   = dialogView.findViewById(R.id.recipeCategory);
        EditText ingredientsInput = dialogView.findViewById(R.id.recipeIngredients);
        EditText stepsInput       = dialogView.findViewById(R.id.recipeSteps);
        EditText timeInput        = dialogView.findViewById(R.id.recipeTime);
        ImageView imagePreview    = dialogView.findViewById(R.id.recipeImage);
        Button pickImageButton    = dialogView.findViewById(R.id.pickImageButton);

        // ניצור כפתור מחיקה דינמי
        Button deleteImageButton = new Button(this);
        deleteImageButton.setText(getString(R.string.delete_image));
        deleteImageButton.setVisibility(View.GONE); // מוסתר בהתחלה

        // מוסיפים אותו לדינמיקה אחרי pickImageButton
        LinearLayout layout = (LinearLayout) dialogView.findViewById(R.id.recipeDialogLayout);
        layout.addView(deleteImageButton);

        currentImagePreview = imagePreview;

        ArrayList<String> cats = db.getAllCategories();
        cats.add(getString(R.string.add_category_option));
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, cats);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(catAdapter);

        if (existing != null) {
            nameInput.setText(existing.getName());
            ingredientsInput.setText(existing.getIngredients());
            stepsInput.setText(existing.getSteps());
            timeInput.setText(existing.getTime());
            int pos = cats.indexOf(existing.getCategory());
            if (pos >= 0) categorySpinner.setSelection(pos);
            if (existing.getImageUri() != null) {
                selectedImageUri = Uri.parse(existing.getImageUri());
                imagePreview.setImageURI(selectedImageUri);
                imagePreview.setVisibility(View.VISIBLE);
                deleteImageButton.setVisibility(View.VISIBLE);
            }
        }

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (cats.get(position).equals(getString(R.string.add_category_option))) {
                    showAddCategoryDialog(categorySpinner, catAdapter, cats);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

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

        // לחיצה על מחיקת תמונה
        deleteImageButton.setOnClickListener(v -> {
            selectedImageUri = null;
            imagePreview.setImageDrawable(null);
            imagePreview.setVisibility(View.GONE);
            deleteImageButton.setVisibility(View.GONE);
        });

        String title = existing == null
                ? getString(R.string.dialog_add_recipe)
                : getString(R.string.dialog_edit_recipe);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_save, (dialog, which) -> {
                    String name  = nameInput.getText().toString().trim();
                    String cat   = categorySpinner.getSelectedItem().toString();
                    String ingr  = ingredientsInput.getText().toString().trim();
                    String steps = stepsInput.getText().toString().trim();
                    String time  = timeInput.getText().toString().trim();
                    String uri   = selectedImageUri != null ? selectedImageUri.toString() : null;

                    if (name.isEmpty() || cat.isEmpty()) {
                        Toast.makeText(this, R.string.dialog_fill_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (existing == null) {
                        db.insertRecipe(name, cat, ingr, steps, time, uri);
                    } else {
                        existing.setName(name);
                        existing.setCategory(cat);
                        existing.setIngredients(ingr);
                        existing.setSteps(steps);
                        existing.setTime(time);
                        existing.setImageUri(uri);
                        db.updateRecipe(existing);
                    }

                    recipes.clear();
                    recipes.addAll(db.getAllRecipes());
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showAddCategoryDialog(@Nullable Spinner spinner, @Nullable ArrayAdapter<String> adapter, @Nullable ArrayList<String> cats) {
        EditText input = new EditText(this);
        input.setHint(R.string.enter_category_name);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_category_title)
                .setView(input)
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.empty_category, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        AtomicBoolean inserted = new AtomicBoolean(false);
                        if (!db.categoryExists(name)) {
                            db.insertCategory(name);
                            inserted.set(true);
                        }

                        ArrayList<String> updatedCats = db.getAllCategories();
                        updatedCats.add(getString(R.string.add_category_option));

                        runOnUiThread(() -> {
                            if (inserted.get()) {
                                Toast.makeText(this, R.string.category_added_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, R.string.category_exists, Toast.LENGTH_SHORT).show();
                            }

                            if (spinner != null && adapter != null && cats != null) {
                                cats.clear();
                                cats.addAll(updatedCats);
                                adapter.notifyDataSetChanged();

                                int pos = cats.indexOf(name);
                                if (pos >= 0) {
                                    spinner.setSelection(pos);
                                }
                            }
                        });
                    }).start();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(new Intent(this, CameraActivity.class));
        }
    }
}
