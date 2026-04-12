package com.rutea.app.activitiesandviews.data.models.dto.disponibility;

import com.google.gson.annotations.SerializedName;

public class DisponibilityDto {
    @SerializedName("id_disponibility")
    private Long idDisponibility;
    private String hour;
    private Double precio;
    @SerializedName("total_quota")
    private Integer totalQuota;
    @SerializedName("disponible_quota")
    private Integer disponibleQuota;
    private Long activityId;
    private Long guideId;

    public Long getIdDisponibility() {
        return idDisponibility;
    }

    public String getHour() {
        return hour;
    }

    public Double getPrecio() {
        return precio;
    }

    public Integer getTotalQuota() {
        return totalQuota;
    }

    public Integer getDisponibleQuota() {
        return disponibleQuota;
    }

    public Long getActivityId() {
        return activityId;
    }

    public Long getGuideId() {
        return guideId;
    }
}
