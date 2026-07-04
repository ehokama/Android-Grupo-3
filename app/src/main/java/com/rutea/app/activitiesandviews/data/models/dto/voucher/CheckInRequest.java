package com.rutea.app.activitiesandviews.data.models.dto.voucher;

public class CheckInRequest {
    private final long reserveId;
    private final boolean confirmed;

    public CheckInRequest(long reserveId, boolean confirmed) {
        this.reserveId = reserveId;
        this.confirmed = confirmed;
    }

    public long getReserveId() {
        return reserveId;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
