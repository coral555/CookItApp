package com.coralb_mayn_yehudas.cookit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView menuIcon;
    private EditText searchInput;
    private Spinner categoryFilter;
    private RecyclerView recipesRecyclerView;
    private FloatingActionButton addRecipeButton;
    private DataBaseHelper db;
    private Uri selectedImageUri;

    private Spinner categorySpinnerInDialog;
    private ArrayAdapter<String> categoryAdapterInDialog;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        menuIcon = findViewById(R.id.menuIcon);
        searchInput = findViewById(R.id.searchInput);
        categoryFilter = findViewById(R.id.categoryFilter);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        addRecipeButton = findViewById(R.id.addRecipeButton);
        db = new DataBaseHelper(this);

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_about) {
                showAboutDialog();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            } else if (id == R.id.nav_exit) {
                finishAffinity();
                return true;
            }
            return false;
        });

        addRecipeButton.setOnClickListener(v -> showAddOptionsDialog());
    }

    private void showAboutDialog() {
        String appName = getString(R.string.app_name);
        String packageName = getPackageName();
        String androidVersion = android.os.Build.VERSION.RELEASE;
        int apiLevel = android.os.Build.VERSION.SDK_INT;

        String message = appName + "\n\n" +
                "Package: " + packageName + "\n" +
                "OS Version: " + androidVersion + " (API " + apiLevel + ")\n\n" +
                "Submitted by: Coral Bahofrker, May Nigri & Yehuda Shmulevitz\n\n" +
                "Submission Date: 29.06.2025";

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.about))
                .setMessage(message)
                .setPositiveButton(getString(R.string.dialog_cancel), null)
                .show();
    }

    private void showAddOptionsDialog() {
        String[] options = {
                getString(R.string.create_recipe),
                getString(R.string.create_category)
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_choose_action))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showAddRecipeDialog();
                    else showAddCategoryDialog();
                })
                .show();
    }

    private void showAddCategoryDialog() {
        final EditText input = new EditText(this);
        input.setHint(getString(R.string.enter_category_name));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_category_title))
                .setView(input)
                .setPositiveButton(getString(R.string.add), (dialog, which) -> {
                    String category = input.getText().toString().trim();
                    if (category.isEmpty()) {
                        Toast.makeText(this, getString(R.string.empty_category), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // תהליכון - כדי שה-UI לא ייתקע
                    new Thread(() -> {
                        if (db.categoryExists(category)) {
                            runOnUiThread(() ->
                                    Toast.makeText(this, getString(R.string.category_exists), Toast.LENGTH_SHORT).show());
                        } else {
                            db.insertCategory(category);
                            runOnUiThread(() -> {
                                Toast.makeText(this, getString(R.string.category_added_success), Toast.LENGTH_SHORT).show();

                                // עדכון ספינר אם פתוח
                                if (categorySpinnerInDialog != null && categoryAdapterInDialog != null) {
                                    int lastIndex = categoryAdapterInDialog.getCount() - 1;
                                    categoryAdapterInDialog.insert(category, lastIndex);
                                    categoryAdapterInDialog.notifyDataSetChanged();
                                    categorySpinnerInDialog.setSelection(lastIndex);
                                }
                            });
                        }
                    }).start();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showAddRecipeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_recipe, null);
        EditText nameInput = dialogView.findViewById(R.id.recipeName);
        Spinner categorySpinner = dialogView.findViewById(R.id.recipeCategory);
        EditText ingredientsInput = dialogView.findViewById(R.id.recipeIngredients);
        EditText stepsInput = dialogView.findViewById(R.id.recipeSteps);
        EditText timeInput = dialogView.findViewById(R.id.recipeTime);
        ImageView imagePreview = dialogView.findViewById(R.id.recipeImage);
        Button pickImageButton = dialogView.findViewById(R.id.pickImageButton);

        ArrayList<String> categories = db.getAllCategories();
        categories.add(getString(R.string.add_category_option));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // שמירה לצורך עדכון לאחר הוספה
        categorySpinnerInDialog = categorySpinner;
        categoryAdapterInDialog = adapter;

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (categories.get(position).equals(getString(R.string.add_category_option))) {
                    showAddCategoryDialog();
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        pickImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_add_recipe))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.dialog_save), (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String category = categorySpinner.getSelectedItem().toString();
                    String ingredients = ingredientsInput.getText().toString().trim();
                    String steps = stepsInput.getText().toString().trim();
                    String time = timeInput.getText().toString().trim();

                    if (name.isEmpty() || category.isEmpty()) {
                        Toast.makeText(this, getString(R.string.dialog_fill_fields), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.insertRecipe(name, category, ingredients, steps, time,
                            selectedImageUri != null ? selectedImageUri.toString() : null);

                    Toast.makeText(this, getString(R.string.dialog_recipe_saved), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show();
    }
}
