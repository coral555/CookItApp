package com.coralb_mayn_yehudas.cookit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

/**
 * GalleryActivity displays images from the device's gallery in a grid.
 * Users can select an image, and the selected URI is returned to the calling activity.
 */
public class GalleryActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 123; // request code used to identify the storage permission request result
    private GridView gridView; // gridView used to display images from the device's gallery
    private ImageAdapter adapter; // custom adapter to populate the GridView with image
    private ArrayList<Uri> imageUris; // list of URIs representing the images from the gallery

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // initialization
        setContentView(R.layout.activity_gallery); // sets the user interface layout for this activity

        gridView = findViewById(R.id.gridView); // Finds and assigns the GridView from the layout to display the gallery images

        // request permission if not exists
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            loadImagesFromGallery();
        }
    }

    /**
     * Loads image URIs from the device's external gallery storage.
     * Populates a GridView using ImageAdapter.
     * When an image is clicked, its URI is returned to the calling activity.
     */
    private void loadImagesFromGallery() {
        imageUris = new ArrayList<>();
        // defines the columns to retrieve from the media store –  only the image ID
        String[] projection = { MediaStore.Images.Media._ID };
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; // Specifies the URI to access external images from the device's media storage

        // Query the gallery for image IDs, ordered by date (newest first)
        Cursor cursor = getContentResolver().query(
                imagesUri, projection, null, null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );

        if (cursor != null) { // Check if the query returned a valid Cursor
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);  // Get the index of the _ID column from the cursor
            while (cursor.moveToNext()) {  // Loop through each row in the cursor
                long id = cursor.getLong(idColumn); // Retrieve the image ID for the current row
                Uri contentUri = Uri.withAppendedPath(imagesUri, String.valueOf(id)); // Construct the full URI
                imageUris.add(contentUri); // Add the image URI to the list
            }
            cursor.close(); // Close the cursor to free up resources
        }
        // If no images were found in the gallery, show a toast message
        if (imageUris.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_images_found), Toast.LENGTH_SHORT).show();
        }

        // Set up adapter and GridView
        adapter = new ImageAdapter(this, imageUris);
        gridView.setAdapter(adapter);

        // Handle image click – return the URI to the calling activity
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Uri selectedUri = imageUris.get(position);
            Intent resultIntent = new Intent();
            resultIntent.setData(selectedUri);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    /**
     * Handles the result of the permission request.
     * If granted, loads the gallery images; otherwise shows an error and exits.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check if the permission request was for reading external storage
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            // If permission was granted, proceed to load images
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImagesFromGallery();
            } else {
                // Permission denied – show error message and close activity
                Toast.makeText(this, getString(R.string.gallery_permission_denied), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
