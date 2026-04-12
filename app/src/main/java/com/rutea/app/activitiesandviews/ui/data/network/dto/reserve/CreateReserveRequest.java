package com.rutea.app.activitiesandviews.ui.data.network.dto.reserve;

import com.google.gson.annotations.SerializedName;

public class CreateReserveRequest {
    @SerializedName("number_of_people")
    private final Integer numberOfPeople;
    private final String state;
    private final DisponibilityRef disponibility;

    public CreateReserveRequest(Integer numberOfPeople, String state, Long disponibilityId) {
        this.numberOfPeople = numberOfPeople;
        this.state = state;
        this.disponibility = new DisponibilityRef(disponibilityId);
    }

    public static class DisponibilityRef {
        @SerializedName("id_disponibility")
        private final Long idDisponibility;

        public DisponibilityRef(Long idDisponibility) {
            this.idDisponibility = idDisponibility;
        }
    }
}
