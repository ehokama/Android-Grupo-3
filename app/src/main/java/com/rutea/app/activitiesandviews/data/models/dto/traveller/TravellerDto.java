package com.rutea.app.activitiesandviews.data.models.dto.traveller;

import java.util.List;
import com.google.gson.annotations.SerializedName;


public class TravellerDto {
    @SerializedName("id_traveller")
    private Long id_traveller;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("preferences")
    private List<String> preferences;

    public TravellerDto(String name, String email, String phone, List<String> preferences) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.preferences = preferences;
    }

    public Long getId_traveller() {
        return id_traveller;
    }

    public void setId_traveller(Long id_traveller) {
        this.id_traveller = id_traveller;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }
}
