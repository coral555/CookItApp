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

public class GalleryActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 123;

    private GridView gridView;
    private ImageAdapter adapter;
    private ArrayList<Uri> imageUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        gridView = findViewById(R.id.gridView);

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

    private void loadImagesFromGallery() {
        imageUris = new ArrayList<>();

        String[] projection = { MediaStore.Images.Media._ID };
        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = getContentResolver().query(
                imagesUri, projection, null, null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );

        if (cursor != null) {
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                Uri contentUri = Uri.withAppendedPath(imagesUri, String.valueOf(id));
                imageUris.add(contentUri);
            }
            cursor.close();
        }

        if (imageUris.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_images_found), Toast.LENGTH_SHORT).show();
        }

        adapter = new ImageAdapter(this, imageUris);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Uri selectedUri = imageUris.get(position);
            Intent resultIntent = new Intent();
            resultIntent.setData(selectedUri);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImagesFromGallery();
            } else {
                Toast.makeText(this, getString(R.string.gallery_permission_denied), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
