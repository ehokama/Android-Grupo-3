package com.rutea.app.activitiesandviews.data.models.dto.review;

public class CreateReviewRequest {
    private final Integer rating;
    private final Integer guideRating;
    private final String comment;
    private final ActivityRef activity;

    public CreateReviewRequest(Integer rating, Integer guideRating, String comment, Long activityId) {
        this.rating = rating;
        this.guideRating = guideRating;
        this.comment = comment;
        this.activity = new ActivityRef(activityId);
    }

    public static class ActivityRef {
        private final Long id;

        public ActivityRef(Long id) {
            this.id = id;
        }
    }
}
