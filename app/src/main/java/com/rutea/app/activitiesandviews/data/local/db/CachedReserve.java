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
    public String guideName;
    public String country;
    public String city;
    public Integer duration;
    public String meetingPoint;
    public boolean canRate;
    public boolean alreadyRated;
    public Integer myActivityRating;
    public Integer myGuideRating;
    public String myComment;

    public Long activityId;

    public CachedReserve(@NonNull Long idReserve, String userEmail, String activityTitle, Long activityId,
                         String reservationDate, String creationDate, Integer numberOfPeople, Double totalPrice,
                         String state, String guideName, String country, String city, Integer duration,
                         String meetingPoint, boolean canRate, boolean alreadyRated, Integer myActivityRating,
                         Integer myGuideRating, String myComment) {
        this.idReserve = idReserve;
        this.userEmail = userEmail;
        this.activityTitle = activityTitle;
        this.activityId = activityId;
        this.reservationDate = reservationDate;
        this.creationDate = creationDate;
        this.numberOfPeople = numberOfPeople;
        this.totalPrice = totalPrice;
        this.state = state;
        this.guideName = guideName;
        this.country = country;
        this.city = city;
        this.duration = duration;
        this.meetingPoint = meetingPoint;
        this.canRate = canRate;
        this.alreadyRated = alreadyRated;
        this.myActivityRating = myActivityRating;
        this.myGuideRating = myGuideRating;
        this.myComment = myComment;
    }
}
