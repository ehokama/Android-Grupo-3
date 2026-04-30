package com.rutea.app.activitiesandviews.data.models.dto.review;

import com.google.gson.annotations.SerializedName;

public class ReviewDto {
    @SerializedName("id_review")
    private Long idReview;
    private Integer rating;
    private Integer guideRating;
    private String comment;
    private String travellerName;
    private Long travellerId;
    private Long activityId;
    private Long guideId;
    private String guideName;

    public Long getIdReview() {
        return idReview;
    }

    public Integer getRating() {
        return rating;
    }

    public Integer getGuideRating() {
        return guideRating;
    }

    public String getComment() {
        return comment;
    }

    public String getTravellerName() {
        return travellerName;
    }

    public Long getTravellerId() {
        return travellerId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public Long getGuideId() {
        return guideId;
    }

    public String getGuideName() {
        return guideName;
    }
}
