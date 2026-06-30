package com.rutea.app.activitiesandviews.data.models.dto.voucher;

public class CheckInResponse {
    private boolean success;
    private String message;
    private Long reserveId;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Long getReserveId() { return reserveId; }
}
