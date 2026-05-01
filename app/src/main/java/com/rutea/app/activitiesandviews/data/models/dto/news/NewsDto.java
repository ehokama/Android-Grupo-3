package com.rutea.app.activitiesandviews.data.models.dto.news;

public class NewsDto {
    private Long id;
    private String title;
    private String description;
    private String content;
    private Long imageId;
    private String imageUrl;
    private String type; // "NOTICIA", "OFERTA", "DESTINO"
    private Long activityId;
    private String discount;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public Long getImageId() { return imageId; }
    public String getImageUrl() { return imageUrl; }
    public String getType() { return type; }
    public Long getActivityId() { return activityId; }
    public String getDiscount() { return discount; }
}
