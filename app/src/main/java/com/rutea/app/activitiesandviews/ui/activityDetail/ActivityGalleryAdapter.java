package com.rutea.app.activitiesandviews.ui.activityDetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rutea.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Carrusel de fotos del detalle; cada URL apunta al endpoint raw del backend
 * (misma convención que el hero y el home).
 */
public class ActivityGalleryAdapter extends RecyclerView.Adapter<ActivityGalleryAdapter.PhotoVH> {

    private final List<String> imageUrls = new ArrayList<>();

    public void setUrls(List<String> urls) {
        imageUrls.clear();
        if (urls != null) {
            imageUrls.addAll(urls);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_gallery_photo, parent, false);
        return new PhotoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoVH holder, int position) {
        holder.bind(imageUrls.get(position));
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static final class PhotoVH extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        PhotoVH(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivGalleryPhoto);
        }

        void bind(String url) {
            Glide.with(imageView)
                    .load(url)
                    .placeholder(R.drawable.bg_hero_landscape)
                    .error(R.drawable.bg_hero_landscape)
                    .centerCrop()
                    .into(imageView);
        }
    }
}
