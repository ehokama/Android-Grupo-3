package com.rutea.app.activitiesandviews.data.local.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_reserves")
public class CachedReserve {

    @PrimaryKey
    @NonNull
    public Long idReserve;

    public String userEmail;
    public String activityTitle;
    public String creationDate;
    public Integer numberOfPeople;
    public Double totalPrice;
    public String state;

    public String reservationDate;

    public Long activityId;

    public CachedReserve(@NonNull Long idReserve, String userEmail, String activityTitle, Long activityId,
                         String reservationDate, Integer numberOfPeople, Double totalPrice, String state) {
        this.idReserve = idReserve;
        this.userEmail = userEmail;
        this.activityTitle = activityTitle;
        this.activityId = activityId;
        this.reservationDate = reservationDate;
        this.numberOfPeople = numberOfPeople;
        this.totalPrice = totalPrice;
        this.state = state;
    }
}
