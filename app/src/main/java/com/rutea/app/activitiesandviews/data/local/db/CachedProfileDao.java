package com.rutea.app.activitiesandviews.data.local.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface CachedProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProfile(CachedProfile profile);

    @Query("SELECT * FROM cached_profile WHERE email = :email LIMIT 1")
    CachedProfile getByEmail(String email);

    @Query("DELETE FROM cached_profile WHERE email = :email")
    void deleteByEmail(String email);

}
