package com.rutea.app.activitiesandviews.data.network;

import com.rutea.app.activitiesandviews.data.models.dto.reserve.CreateReserveRequest;

public final class ReserveRequestFactory {
    private ReserveRequestFactory() {
    }

    public static CreateReserveRequest create(long disponibilityId, int people) {
        return new CreateReserveRequest(people, "CONFIRMED", disponibilityId);
    }

    public static CreateReserveRequest createWithDiscount(long disponibilityId, int people,
                                                          String discountMode, int discountPercent,
                                                          Long appliedNewsId) {
        String mode = discountMode != null && !discountMode.trim().isEmpty() ? discountMode : null;
        Integer percent = discountPercent > 0 ? discountPercent : null;
        return new CreateReserveRequest(people, "CONFIRMED", disponibilityId, mode, percent, appliedNewsId);
    }
}
