package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.reserve.CreateReserveRequest;

public final class ReserveRequestFactory {
    private ReserveRequestFactory() {
    }

    public static CreateReserveRequest create(long disponibilityId, int people) {
        return new CreateReserveRequest(people, "CONFIRMED", disponibilityId);
    }
}
