package com.rutea.app.activitiesandviews.data.models.dto.activity;

import com.rutea.app.activitiesandviews.data.models.dto.disponibility.DisponibilityDto;

import java.util.List;

public class ActivityDto {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private Double rating;
    private Integer duration;
    private String category;
    private String inclusions;
    private String meetingPoint;
    private String language;
    private String cancellationPolicy;
    private String ubicationName;
    private Long ubicationId;
    private List<ImageDto> images;
    private List<DisponibilityDto> disponibilities;

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public Double getRating() {
        return rating;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getCategory() {
        return category;
    }

    public String getInclusions() {
        return inclusions;
    }

    public String getMeetingPoint() {
        return meetingPoint;
    }

    public String getLanguage() {
        return language;
    }

    public String getCancellationPolicy() {
        return cancellationPolicy;
    }

    public String getUbicationName() {
        return ubicationName;
    }

    public Long getUbicationId() {
        return ubicationId;
    }

    public List<ImageDto> getImages() {
        return images;
    }

    public List<DisponibilityDto> getDisponibilities() {
        return disponibilities;
    }
}
