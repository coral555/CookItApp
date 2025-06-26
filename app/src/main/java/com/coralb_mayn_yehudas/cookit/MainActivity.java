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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import android.annotation.SuppressLint;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private int userId;
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
    private List<Recipe> allRecipes; // מכיל את כל המתכונים

    private RecipeAdapter adapter;

    private ImageView currentImagePreview;
    private View currentDialogView;
    private int currentFilterIndex = -1; // אין סינון ברירת מחדל


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
                                Button deleteImageButton = currentDialogView.findViewById(R.id.deleteImageButton);
                                deleteImageButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
            );


    private final ActivityResultLauncher<Intent> galleryCustomLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selectedImage = result.getData().getData();
                            if (selectedImage != null && currentImagePreview != null) {
                                selectedImageUri = selectedImage;
                                currentImagePreview.setImageURI(selectedImage);
                                currentImagePreview.setVisibility(View.VISIBLE);
                                Button deleteImageButton = currentDialogView.findViewById(R.id.deleteImageButton);
                                deleteImageButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
            );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getInt("user_id", -1);

        if (userId == -1) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        LocaleHelper.applySavedLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NotificationHelper.createChannel(this);
        scheduleDailyNotification();

        db = new DataBaseHelper(this);

        drawerLayout        = findViewById(R.id.drawerLayout);
        navigationView      = findViewById(R.id.navigationView);
        menuIcon            = findViewById(R.id.menuIcon);
        searchInput         = findViewById(R.id.searchInput);
        categoryFilter      = findViewById(R.id.categoryFilter);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        addRecipeButton     = findViewById(R.id.addRecipeButton);

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(this::onNavItem);

        // --- רשימה מלאה של כל המתכונים ---
        allRecipes = db.getAllRecipes(userId);
        recipes = new ArrayList<>(allRecipes); // תצוגה נוכחית שמושפעת מסינון

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
                new Thread(() -> {
                    if (db.deleteRecipe(r.getId())) {
                        // טוען מחדש את כל המתכונים מה־DB
                        List<Recipe> updatedList = db.getAllRecipes(userId);

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();

                            allRecipes.clear();
                            allRecipes.addAll(updatedList);

                            // אם יש סינון פעיל – מריץ אותו שוב (ללא פתיחת דיאלוג)
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
                r.setFavorite(!r.isFavorite());

                new Thread(() -> {
                    db.updateRecipe(r);

                    runOnUiThread(() -> {
                        if (currentFilterIndex != -1) {
                            runFilterAgain();
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }).start();
            }

        });

        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipesRecyclerView.setAdapter(adapter);

        addRecipeButton.setOnClickListener(v -> showAddOptionsDialog());

        LinearLayout filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(v -> showFilterDialog());

        // 💡 חיפוש דינמי עם תהליכון
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

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
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showFilterDialog() {
        String[] options = {
                getString(R.string.filter_name_asc),
                getString(R.string.filter_name_desc),
                getString(R.string.filter_category_asc),
                getString(R.string.filter_category_desc),
                getString(R.string.filter_time_asc),
                getString(R.string.filter_time_desc),
                getString(R.string.filter_favorites)
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.filter_title))
                .setItems(options, (dialog, which) -> {
                    currentFilterIndex = which;  // 💡 שמירת סינון נבחר

                    allRecipes = db.getAllRecipes(userId); // 💡 עדכון מה-DB
                    recipes.clear();
                    recipes.addAll(allRecipes);

                    switch (which) {
                        case 0: recipes.sort(Comparator.comparing(Recipe::getName)); break;
                        case 1: recipes.sort((a, b) -> b.getName().compareTo(a.getName())); break;
                        case 2: recipes.sort(Comparator.comparing(Recipe::getCategory)); break;
                        case 3: recipes.sort((a, b) -> b.getCategory().compareTo(a.getCategory())); break;
                        case 4:
                            recipes.sort(Comparator.comparingInt(r -> {
                                try { return Integer.parseInt(r.getTime()); }
                                catch (NumberFormatException e) { return Integer.MAX_VALUE; }
                            }));
                            break;
                        case 5:
                            recipes.sort((a, b) -> {
                                try { return Integer.parseInt(b.getTime()) - Integer.parseInt(a.getTime()); }
                                catch (NumberFormatException e) { return 0; }
                            });
                            break;
                        case 6:
                            recipes.clear();
                            recipes.addAll(db.getFavoriteRecipes(userId));
                            break;
                    }

                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void runFilterAgain() {
        if (currentFilterIndex == -1) return;

        allRecipes = db.getAllRecipes(userId);
        recipes.clear();

        if (currentFilterIndex == 6) {
            recipes.addAll(db.getFavoriteRecipes(userId));
        } else {
            recipes.addAll(allRecipes);
        }

        switch (currentFilterIndex) {
            case 0: recipes.sort(Comparator.comparing(Recipe::getName)); break;
            case 1: recipes.sort((a, b) -> b.getName().compareTo(a.getName())); break;
            case 2: recipes.sort(Comparator.comparing(Recipe::getCategory)); break;
            case 3: recipes.sort((a, b) -> b.getCategory().compareTo(a.getCategory())); break;
            case 4:
                recipes.sort(Comparator.comparingInt(r -> {
                    try { return Integer.parseInt(r.getTime()); }
                    catch (NumberFormatException e) { return Integer.MAX_VALUE; }
                }));
                break;
            case 5:
                recipes.sort((a, b) -> {
                    try { return Integer.parseInt(b.getTime()) - Integer.parseInt(a.getTime()); }
                    catch (NumberFormatException e) { return 0; }
                });
                break;
        }

        adapter.notifyDataSetChanged();
    }

    private boolean onNavItem(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_about) {
            showAboutDialog();
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_delete_all) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.delete_all_recipes)
                    .setMessage(R.string.delete_all_confirmation)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.delete_all_recipes)
                                .setMessage(R.string.delete_all_final_warning)
                                .setPositiveButton(android.R.string.ok, (dialog2, which2) -> {
                                    db.deleteAllUserData(userId);
                                    recipes.clear();
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(this, R.string.deleted_all_user_data, Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton(android.R.string.cancel, null)
                                .show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else if (id == R.id.nav_exit) {
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_recipe, null);
        currentDialogView = dialogView;

        EditText nameInput        = dialogView.findViewById(R.id.recipeName);
        Spinner categorySpinner   = dialogView.findViewById(R.id.recipeCategory);
        EditText ingredientsInput = dialogView.findViewById(R.id.recipeIngredients);
        EditText stepsInput       = dialogView.findViewById(R.id.recipeSteps);
        EditText timeInput        = dialogView.findViewById(R.id.recipeTime);
        ImageView imagePreview    = dialogView.findViewById(R.id.recipeImage);
        Button pickImageButton    = dialogView.findViewById(R.id.pickImageButton);
        Button deleteImageButton  = dialogView.findViewById(R.id.deleteImageButton);
        Button selectFromGalleryButton = dialogView.findViewById(R.id.selectFromGalleryButton);
        currentImagePreview = imagePreview;

        ArrayList<String> cats = db.getAllCategories(userId);
        cats.add(getString(R.string.add_category_option));
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cats);
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

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

        selectFromGalleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GalleryActivity.class);
            galleryCustomLauncher.launch(intent);
        });

        deleteImageButton.setOnClickListener(v -> {
            // Clear selected image and reset to default icon
            selectedImageUri = null;
            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
            imagePreview.setVisibility(View.VISIBLE);
            deleteImageButton.setVisibility(View.GONE);
        });

        String title = existing == null
                ? getString(R.string.dialog_add_recipe)
                : getString(R.string.dialog_edit_recipe);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                String name  = nameInput.getText().toString().trim();
                String cat   = categorySpinner.getSelectedItem().toString();
                String ingr  = ingredientsInput.getText().toString().trim();
                String steps = stepsInput.getText().toString().trim();
                String time  = timeInput.getText().toString().trim();
                String uri   = selectedImageUri != null ? selectedImageUri.toString() : null;

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

                new Thread(() -> {
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

                    // תהליך רקע – גישה ל־SQLite
                    new Thread(() -> {
                        boolean inserted = false;

                        if (!db.categoryExists(name, userId)) {
                            db.insertCategory(name, userId);
                            inserted = true;
                        }

                        ArrayList<String> updatedCats = db.getAllCategories(userId);
                        updatedCats.add(getString(R.string.add_category_option));

                        boolean finalInserted = inserted;
                        runOnUiThread(() -> {
                            if (finalInserted) {
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
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (spinner != null && adapter != null && cats != null) {
                        // Reset selection to "בחר קטגוריה" (index 0)
                        spinner.setSelection(0);
                    }
                })
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

    @SuppressLint("ScheduleExactAlarm")
    private void scheduleDailyNotification() {
        Intent intent = new Intent(this, RecipeAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // notification hour
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 0);

        long triggerAt = calendar.getTimeInMillis();
        if (System.currentTimeMillis() > triggerAt) {
            triggerAt += AlarmManager.INTERVAL_DAY;
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
    }

}
