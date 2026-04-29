package com.rutea.app.activitiesandviews.data.local.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CachedReserveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CachedReserve> reserves);

    @Query("SELECT * FROM cached_reserves WHERE userEmail = :email ORDER BY creationDate DESC")
    List<CachedReserve> getByEmail(String email);

    @Query("DELETE FROM cached_reserves WHERE userEmail = :email")
    void deleteByEmail(String email);
}
