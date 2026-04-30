package com.rutea.app.activitiesandviews.data.local.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "cached_favorites", primaryKeys = {"activityId", "userEmail"})
public class CachedFavorite {
    @NonNull public long activityId;
    @NonNull public String userEmail;
    public String title;
    public Double price;
    public Double rating;
    public String category;
    public String ubicationName;
    public String imageUrl;
    public boolean hasNewUpdate;

    public CachedFavorite(long activityId, @NonNull String userEmail, String title,
                          Double price, Double rating, String category,
                          String ubicationName, String imageUrl) {
        this.activityId = activityId;
        this.userEmail = userEmail;
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.category = category;
        this.ubicationName = ubicationName;
        this.imageUrl = imageUrl;
        this.hasNewUpdate = false;
    }
}
