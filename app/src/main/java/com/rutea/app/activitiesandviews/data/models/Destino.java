package com.rutea.app.activitiesandviews.data.models;

import com.google.gson.annotations.SerializedName;

public class Destino {
    @SerializedName("id_destino")
    private int id;
    private String nombre;
    private String pais;

    public Destino(int id, String nombre, String pais) {
        this.id = id;
        this.nombre = nombre;
        this.pais = pais;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getPais() { return pais; }
}
