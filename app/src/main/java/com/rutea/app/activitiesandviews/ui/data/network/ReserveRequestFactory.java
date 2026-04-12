package com.rutea.app.activitiesandviews.ui.data.network;

import com.rutea.app.activitiesandviews.ui.data.network.dto.reserve.CreateReserveRequest;

public final class ReserveRequestFactory {
    private ReserveRequestFactory() {
    }

    public static CreateReserveRequest create(long disponibilityId, int people) {
        return new CreateReserveRequest(people, "CONFIRMED", disponibilityId);
    }
}
