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

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {

    public interface Listener {
        void onView(Recipe r);
        void onEdit(Recipe r);
        void onDelete(Recipe r);
        void onFavoriteToggle(Recipe r);
    }

    private List<Recipe> data;
    private final Listener listener;
    private final Context ctx;

    public RecipeAdapter(Context ctx, List<Recipe> data, Listener l) {
        this.ctx = ctx;
        this.data = data;
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_recipe, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Recipe r = data.get(i);
        h.tvName.setText(r.getName());
        h.tvCategory.setText(r.getCategory());
        h.tvTime.setText(r.getTime());

        if (r.getImageUri() != null) {
            h.img.setVisibility(View.VISIBLE);
            h.img.setImageURI(Uri.parse(r.getImageUri()));
        } else {
            h.img.setVisibility(View.GONE);
        }

        h.btnFavorite.setImageResource(
                r.isFavorite() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline
        );

        h.btnFavorite.setOnClickListener(v -> listener.onFavoriteToggle(r));
        h.btnView.setOnClickListener(v -> listener.onView(r));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(r));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(r));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // 👇 מתודה חדשה לסינון/ריענון הנתונים
    public void updateData(List<Recipe> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
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
