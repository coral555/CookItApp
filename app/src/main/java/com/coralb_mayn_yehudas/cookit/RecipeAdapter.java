package com.coralb_mayn_yehudas.cookit;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Recipe adapter is a RecyclerView adapter that displays a list of Recipe items.
 * It binds each recipe to a card layout and allows interaction through view/edit/delete/favorite buttons.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {

    /**
     * Listener interface to handle user actions on each recipe item.
     */
    public interface Listener {
        void onView(Recipe r);  // Triggered when the user wants to view details

        void onEdit(Recipe r); // Triggered when editing a recipe

        void onDelete(Recipe r); // Triggered when deleting a recipe

        void onFavoriteToggle(Recipe r); // Triggered when toggling the favorite status
    }

    private List<Recipe> data; // The list of recipes to display
    private final Listener listener; // Listener for item actions
    private final Context ctx; // Context for inflating views and accessing resources

    //Constructor
    public RecipeAdapter(Context ctx, List<Recipe> data, Listener l) {
        this.ctx = ctx;
        this.data = data;
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single recipe item
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_recipe, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Recipe r = data.get(i);
        // Set text fields
        h.tvName.setText(r.getName());
        h.tvCategory.setText(r.getCategory());
        h.tvTime.setText(r.getTime());

        // Load image if available
        if (r.getImageUri() != null) {
            h.img.setVisibility(View.VISIBLE);
            h.img.setImageURI(Uri.parse(r.getImageUri()));
        } else {
            h.img.setVisibility(View.GONE);
        }

        // Set favorite icon state
        h.btnFavorite.setImageResource(
                r.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline
        );
        // Set click listeners for buttons
        h.btnFavorite.setOnClickListener(v -> listener.onFavoriteToggle(r));
        h.btnView.setOnClickListener(v -> listener.onView(r));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(r));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(List<Recipe> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder (VH) class holds references to all views for a single item.
     */
    public static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        ImageButton btnFavorite;
        TextView tvName, tvCategory, tvTime;
        Button btnView, btnEdit, btnDelete;

        VH(@NonNull View v) {
            super(v);
            img = v.findViewById(R.id.imgRecipe);
            btnFavorite = v.findViewById(R.id.btnFavorite);
            tvName = v.findViewById(R.id.tvName);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvTime = v.findViewById(R.id.tvTime);
            btnView = v.findViewById(R.id.btnView);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
