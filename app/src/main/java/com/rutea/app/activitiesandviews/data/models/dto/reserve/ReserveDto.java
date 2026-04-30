package com.rutea.app.activitiesandviews.data.models.dto.reserve;

import com.google.gson.annotations.SerializedName;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ImageDto;

import java.util.List;

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

    private String reservationDate;

    private Long activityId;
    private List<ImageDto> activityImages; // <-- agregar esto

    public List<ImageDto> getActivityImages() {
        return activityImages;
    }
    public void setActivityImages(List<ImageDto> activityImages) {
        this.activityImages = activityImages;
    }
    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {
        this.reservationDate = reservationDate;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

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

    public void setIdReserve(Long idReserve) { this.idReserve = idReserve; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
    public void setNumberOfPeople(Integer numberOfPeople) { this.numberOfPeople = numberOfPeople; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public void setState(String state) { this.state = state; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }
}
