package com.rutea.app.activitiesandviews.data.models.dto.voucher;

public class CheckInRequest {
    private final String qrPayload;

    public CheckInRequest(String qrPayload) {
        this.qrPayload = qrPayload;
    }

    public String getQrPayload() {
        return qrPayload;
    }
}
