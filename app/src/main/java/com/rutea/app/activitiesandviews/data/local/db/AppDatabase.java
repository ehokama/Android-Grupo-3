package com.rutea.app.activitiesandviews.data.local.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CachedProfile.class, CachedReserve.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CachedProfileDao cachedProfileDao();
    public abstract CachedReserveDao cachedReserveDao();
}
