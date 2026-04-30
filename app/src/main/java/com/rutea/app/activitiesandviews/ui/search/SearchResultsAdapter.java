package com.rutea.app.activitiesandviews.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.VH> {

    private final List<ActivityDto> items;
    private final Consumer<ActivityDto> onItemClick;

    public SearchResultsAdapter(List<ActivityDto> items, Consumer<ActivityDto> onItemClick) {
        this.items = items;
        this.onItemClick = onItemClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(items.get(pos)); }

    @Override
    public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvCategory, tvDestino, tvDuration, tvPrice;

        VH(@NonNull View v) {
            super(v);
            ivImage    = v.findViewById(R.id.ivResultImage);
            tvTitle    = v.findViewById(R.id.tvResultTitle);
            tvCategory = v.findViewById(R.id.tvResultCategory);
            tvDestino  = v.findViewById(R.id.tvResultDestino);
            tvDuration = v.findViewById(R.id.tvResultDuration);
            tvPrice    = v.findViewById(R.id.tvResultPrice);
        }

        void bind(ActivityDto a) {
            tvTitle.setText(a.getTitle() != null ? a.getTitle() : "Actividad");
            tvCategory.setText(a.getCategory() != null ? a.getCategory() : "");
            tvDestino.setText(a.getUbicationName() != null ? "📍 " + a.getUbicationName() : "📍 —");
            tvDuration.setText(a.getDuration() != null ? "🕐 " + a.getDuration() + " hs" : "🕐 —");
            tvPrice.setText(a.getPrice() != null
                    ? String.format(Locale.getDefault(), "$%.0f", a.getPrice()) : "Gratis");

            if (a.getImages() != null && !a.getImages().isEmpty()) {
                Long imgId = a.getImages().get(0).getIdImage();
                Glide.with(ivImage.getContext())
                        .load("http://10.0.2.2:8080/api/images/" + imgId + "/raw")
                        .placeholder(R.drawable.bg_hero_landscape)
                        .error(R.drawable.bg_hero_landscape)
                        .centerCrop()
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.bg_hero_landscape);
            }

            itemView.setOnClickListener(v -> { if (onItemClick != null) onItemClick.accept(a); });
        }
    }
}