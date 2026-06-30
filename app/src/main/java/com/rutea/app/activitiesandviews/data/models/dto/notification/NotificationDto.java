package com.rutea.app.activitiesandviews.data.models.dto.notification;

public class NotificationDto {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Long reserveId;

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Long getReserveId() { return reserveId; }
}
