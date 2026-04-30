package com.rutea.app.activitiesandviews.ui.favorites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavorite;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    public interface Listener {
        void onUnfavorite(CachedFavorite favorite);
        void onReserve(CachedFavorite favorite);
        void onItemClick(CachedFavorite favorite);
    }

    private List<CachedFavorite> items = new ArrayList<>();
    private final Listener listener;

    public FavoriteAdapter(Listener listener) {
        this.listener = listener;
    }

    public void updateList(List<CachedFavorite> newItems) {
        this.items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CachedFavorite fav = items.get(position);

        holder.tvTitle.setText(fav.title != null ? fav.title : "Actividad");

        if (fav.price != null) {
            holder.tvPrice.setText("$ " + String.format("%.0f", fav.price));
        } else {
            holder.tvPrice.setText("Precio no disponible");
        }

        holder.tvCategory.setText(fav.category != null ? fav.category : "");

        if (fav.hasNewUpdate) {
            holder.tvNewBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvNewBadge.setVisibility(View.GONE);
        }

        if (fav.imageUrl != null && !fav.imageUrl.isEmpty()) {
            Glide.with(holder.ivImage.getContext())
                    .load(fav.imageUrl)
                    .placeholder(R.drawable.bg_hero_landscape)
                    .error(R.drawable.bg_hero_landscape)
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.bg_hero_landscape);
        }

        holder.btnUnfavorite.setOnClickListener(v -> listener.onUnfavorite(fav));
        holder.btnReserve.setOnClickListener(v -> listener.onReserve(fav));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(fav));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvPrice, tvCategory, tvNewBadge;
        ImageButton btnUnfavorite;
        MaterialButton btnReserve;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivFavImage);
            tvTitle = itemView.findViewById(R.id.tvFavTitle);
            tvPrice = itemView.findViewById(R.id.tvFavPrice);
            tvCategory = itemView.findViewById(R.id.tvFavCategory);
            tvNewBadge = itemView.findViewById(R.id.tvNewBadge);
            btnUnfavorite = itemView.findViewById(R.id.btnUnfavorite);
            btnReserve = itemView.findViewById(R.id.btnFavReserve);
        }
    }
}
