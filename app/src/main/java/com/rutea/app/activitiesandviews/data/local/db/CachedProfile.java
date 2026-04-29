package com.rutea.app.activitiesandviews.data.local.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_profile")
public class CachedProfile {

    @PrimaryKey
    @NonNull
    public String email;

    public String name;
    public String phone;
    public String preferences; // JSON string, e.g. "AVENTURA,EXCURSION"

    public CachedProfile(@NonNull String email, String name, String phone, String preferences) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.preferences = preferences;
    }
}
