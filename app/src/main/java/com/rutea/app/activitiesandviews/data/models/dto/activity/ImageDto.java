package com.rutea.app.activitiesandviews.data.models.dto.activity;

import com.google.gson.annotations.SerializedName;

public class ImageDto {
    @SerializedName("id_image")
    private Long idImage;
    private String name;

    public Long getIdImage() {
        return idImage;
    }

    public String getName() {
        return name;
    }
}
