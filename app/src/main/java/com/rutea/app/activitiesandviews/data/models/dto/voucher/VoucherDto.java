package com.rutea.app.activitiesandviews.data.models.dto.voucher;

import com.google.gson.annotations.SerializedName;

public class VoucherDto {
    @SerializedName("reserveId")
    private Long reserveId;
    @SerializedName("voucherCode")
    private String voucherCode;
    @SerializedName("activityTitle")
    private String activityTitle;
    @SerializedName("reservationDate")
    private String reservationDate;
    @SerializedName("meetingPoint")
    private String meetingPoint;
    @SerializedName("guideName")
    private String guideName;
    @SerializedName("numberOfPeople")
    private Integer numberOfPeople;
    private String state;
    private boolean checkedIn;
    @SerializedName("checkedInAt")
    private String checkedInAt;

    public Long getReserveId() { return reserveId; }
    public String getVoucherCode() { return voucherCode; }
    public String getActivityTitle() { return activityTitle; }
    public String getReservationDate() { return reservationDate; }
    public String getMeetingPoint() { return meetingPoint; }
    public String getGuideName() { return guideName; }
    public Integer getNumberOfPeople() { return numberOfPeople; }
    public String getState() { return state; }
    public boolean isCheckedIn() { return checkedIn; }
    public String getCheckedInAt() { return checkedInAt; }
}
