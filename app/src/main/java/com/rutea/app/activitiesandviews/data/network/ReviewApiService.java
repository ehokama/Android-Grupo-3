package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.review.CreateReviewRequest;
import com.rutea.app.activitiesandviews.data.models.dto.review.ReviewDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ReviewApiService {
    @GET("api/reviews/my")
    Call<List<ReviewDto>> getMyReviews();

    @POST("api/reviews")
    Call<ReviewDto> createReview(@Body CreateReviewRequest request);
}
