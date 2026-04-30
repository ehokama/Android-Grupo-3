package com.rutea.app.activitiesandviews.di;

import android.content.Context;

import androidx.room.Room;

import com.rutea.app.activitiesandviews.data.local.db.AppDatabase;
import com.rutea.app.activitiesandviews.data.local.db.CachedProfileDao;
import com.rutea.app.activitiesandviews.data.local.db.CachedFavoriteDao;
import com.rutea.app.activitiesandviews.data.local.db.CachedReserveDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "rutea_cache_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public CachedProfileDao provideProfileDao(AppDatabase db) {
        return db.cachedProfileDao();
    }

    @Provides
    public CachedReserveDao provideReserveDao(AppDatabase db) {
        return db.cachedReserveDao();
    }

    @Provides
    public CachedFavoriteDao provideFavoriteDao(AppDatabase db) {
        return db.cachedFavoriteDao();
    }
}
