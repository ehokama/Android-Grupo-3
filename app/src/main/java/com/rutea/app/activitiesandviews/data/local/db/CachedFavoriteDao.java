package com.rutea.app.activitiesandviews.data.local.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CachedFavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CachedFavorite favorite);

    @Query("DELETE FROM cached_favorites WHERE activityId = :activityId AND userEmail = :email")
    void delete(long activityId, String email);

    @Query("SELECT * FROM cached_favorites WHERE userEmail = :email ORDER BY title ASC")
    List<CachedFavorite> getByEmail(String email);

    @Query("SELECT COUNT(*) FROM cached_favorites WHERE activityId = :activityId AND userEmail = :email")
    int isFavorite(long activityId, String email);

    @Query("UPDATE cached_favorites SET hasNewUpdate = :flag WHERE activityId = :activityId AND userEmail = :email")
    void setHasNewUpdate(long activityId, String email, boolean flag);

    @Query("UPDATE cached_favorites SET price = :price WHERE activityId = :activityId AND userEmail = :email")
    void updatePrice(long activityId, String email, double price);
}
