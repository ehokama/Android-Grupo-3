package com.rutea.app.activitiesandviews.ui.news;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.news.NewsDto;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    public interface Listener {
        void onItemClick(NewsDto news);
    }

    private List<NewsDto> items = new ArrayList<>();
    private final Listener listener;

    public NewsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void updateList(List<NewsDto> newItems) {
        this.items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsDto news = items.get(position);

        holder.tvTitle.setText(news.getTitle() != null ? news.getTitle() : "");
        holder.tvDesc.setText(news.getDescription() != null ? news.getDescription() : "");

        String type = news.getType() != null ? news.getType() : "";
        holder.tvType.setText(type);
        holder.tvType.setBackgroundColor(typeColor(type));

        String imageUrl = resolveImageUrl(news);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.ivImage.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_hero_landscape)
                    .error(R.drawable.bg_hero_landscape)
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.bg_hero_landscape);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(news));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static String resolveImageUrl(NewsDto news) {
        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            return news.getImageUrl();
        }
        if (news.getImageId() != null) {
            return "http://10.0.2.2:8080/api/images/" + news.getImageId() + "/raw";
        }
        return null;
    }

    public static int typeColor(String type) {
        if (type == null) return 0xFF9E9E9E;
        switch (type.toUpperCase()) {
            case "OFERTA":  return 0xFFFF6F00;
            case "DESTINO": return 0xFF388E3C;
            default:        return 0xFF1976D2; // NOTICIA
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDesc, tvType;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivNewsImage);
            tvTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvDesc  = itemView.findViewById(R.id.tvNewsDesc);
            tvType  = itemView.findViewById(R.id.tvNewsType);
        }
    }
}
