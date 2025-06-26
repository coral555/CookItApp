package com.coralb_mayn_yehudas.cookit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;

/**
 * Activity for capturing a photo using CameraX API.
 * Shows camera preview, captures image, and returns image URI to the calling activity.
 */
public class CameraActivity extends AppCompatActivity {
    private PreviewView previewView; // UI element that displays the live camera feed
    private ImageCapture imageCapture; // Responsible for taking pictures
    public static final String EXTRA_IMAGE_URI = "capturedImageUri"; // Key for returning the image URI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        Button captureBtn = findViewById(R.id.captureBtn);

        // Check camera permission in runtime
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.CAMERA },
                    1001
            );
        } else {
            startCamera(); // Start the camera if there's a permission
        }

        captureBtn.setOnClickListener(v -> takePhoto()); // Set the "Capture" button click listener to take a photo
    }

    /**
     * Initializes the camera and sets up preview and capture use cases.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get(); // Get the camera provider once it's ready

                // Create preview use case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build(); // Create image capture use case

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA; // Select the back camera

                cameraProvider.unbindAll(); // Unbind all use cases before rebinding

                // Bind preview and image capture to lifecycle
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );
            } catch (Exception e) {
                // Log failure during camera setup
                Log.e("CameraActivity", "Failed to start camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Captures a photo and returns the URI back to the calling activity.
     */
    private void takePhoto() {
        if (imageCapture == null) return;

        // Create a file to save the photo
        File photoFile = new File(
                getExternalCacheDir(),
                "IMG_" + System.currentTimeMillis() + ".jpg"
        );

        // Configure the output file
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Take picture with callback for success and failure
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_IMAGE_URI, savedUri.toString());
                        setResult(RESULT_OK, resultIntent);
                        finish(); // Close this activity
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraActivity", "Image capture failed", exception); // Log the error and show a message
                        Toast.makeText(
                                CameraActivity.this,
                                "Capture failed: " + exception.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    /**
     * Handles the result of permission request for camera access.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera(); // Start camera after permission is granted
        } else { // Show message and exit if permission is denied
            Toast.makeText(
                    this,
                    "Camera permission is required",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
        }
    }
}
