package com.rutea.app.activitiesandviews.ui.data.network.dto.reserve;

import com.google.gson.annotations.SerializedName;

public class ReserveDto {
    @SerializedName("id_reserve")
    private Long idReserve;
    @SerializedName("creation_date")
    private String creationDate;
    @SerializedName("number_of_people")
    private Integer numberOfPeople;
    private Double totalPrice;
    private String state;
    private String travellerName;
    private Long travellerId;
    private Long disponibilityId;
    private Double disponibilityPrice;
    private String activityTitle;

    public Long getIdReserve() {
        return idReserve;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public String getState() {
        return state;
    }

    public String getTravellerName() {
        return travellerName;
    }

    public Long getTravellerId() {
        return travellerId;
    }

    public Long getDisponibilityId() {
        return disponibilityId;
    }

    public Double getDisponibilityPrice() {
        return disponibilityPrice;
    }

    public String getActivityTitle() {
        return activityTitle;
    }
}
