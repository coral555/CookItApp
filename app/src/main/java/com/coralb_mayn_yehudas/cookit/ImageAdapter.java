package com.coralb_mayn_yehudas.cookit;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * ImageAdapter is a custom adapter for displaying image URIs in a GridView.
 * uses ImageView for each item and loads the image directly from its URI.
 */
public class ImageAdapter extends BaseAdapter {

    private final Context context; // The context in which the adapter is used
    private final ArrayList<Uri> imageUris; // List of image URIs to be displayed

    // constructor
    public ImageAdapter(Context context, ArrayList<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
    }

    // Returns the total number of items (images) in the adapter
    @Override
    public int getCount() {
        return imageUris.size();
    }

    // Returns the image URI at the specified position
    @Override
    public Object getItem(int position) {
        return imageUris.get(position);
    }

    // Returns the item's ID (in this case, its position)
    @Override
    public long getItemId(int position) {
        return position;
    }

    // Returns a view (ImageView) for each item in the grid
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        // If there's no reusable view, create a new one
        if (convertView == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(300, 300));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {  // Reuse the existing ImageView for better performance
            imageView = (ImageView) convertView;
        }
        // Set the image URI for the current position
        imageView.setImageURI(imageUris.get(position));
        return imageView;
    }
}
