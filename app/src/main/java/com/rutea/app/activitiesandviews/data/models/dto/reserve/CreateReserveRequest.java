package com.rutea.app.activitiesandviews.data.models.dto.reserve;

import com.google.gson.annotations.SerializedName;

public class CreateReserveRequest {
    @SerializedName("number_of_people")
    private final Integer numberOfPeople;
    private final String state;
    private final DisponibilityRef disponibility;
    private final String discountMode;
    private final Integer discountPercent;
    @SerializedName("applied_news_id")
    private final Long appliedNewsId;

    public CreateReserveRequest(Integer numberOfPeople, String state, Long disponibilityId) {
        this.numberOfPeople = numberOfPeople;
        this.state = state;
        this.disponibility = new DisponibilityRef(disponibilityId);
        this.discountMode = null;
        this.discountPercent = null;
        this.appliedNewsId = null;
    }

    public CreateReserveRequest(Integer numberOfPeople, String state, Long disponibilityId,
                                String discountMode, Integer discountPercent, Long appliedNewsId) {
        this.numberOfPeople = numberOfPeople;
        this.state = state;
        this.disponibility = new DisponibilityRef(disponibilityId);
        this.discountMode = discountMode;
        this.discountPercent = discountPercent;
        this.appliedNewsId = appliedNewsId;
    }

    public static class DisponibilityRef {
        @SerializedName("id_disponibility")
        private final Long idDisponibility;

        public DisponibilityRef(Long idDisponibility) {
            this.idDisponibility = idDisponibility;
        }
    }
}
