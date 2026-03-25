package com.example.xplorernow.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Disponibilidad {
    @SerializedName("id_disponibilidad")
    private int id;
    @SerializedName("id_actividad")
    private int idActividad;
    @SerializedName("id_guia")
    private int idGuia;
    @SerializedName("fecha_hora")
    private String fechaHora; // Store as String for simplicity with JSON, can be parsed later
    private double precio;
    @SerializedName("cupo_total")
    private int cupoTotal;
    @SerializedName("cupo_disponible")
    private int cupoDisponible;

    public Disponibilidad(int id, int idActividad, int idGuia, String fechaHora, double precio, int cupoTotal, int cupoDisponible) {
        this.id = id;
        this.idActividad = idActividad;
        this.idGuia = idGuia;
        this.fechaHora = fechaHora;
        this.precio = precio;
        this.cupoTotal = cupoTotal;
        this.cupoDisponible = cupoDisponible;
    }

    public int getId() { return id; }
    public int getIdActividad() { return idActividad; }
    public int getIdGuia() { return idGuia; }
    public String getFechaHora() { return fechaHora; }
    public double getPrecio() { return precio; }
    public int getCupoTotal() { return cupoTotal; }
    public int getCupoDisponible() { return cupoDisponible; }
}
