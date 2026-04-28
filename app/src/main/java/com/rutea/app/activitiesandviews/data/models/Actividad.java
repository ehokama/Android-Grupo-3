package com.rutea.app.activitiesandviews.data.models;

import com.google.gson.annotations.SerializedName;

public class Actividad {
    @SerializedName("id_actividad")
    private int id;
    @SerializedName("id_destino")
    private int idDestino;
    private String titulo;
    private String descripcion;
    private String tipo; // Tour, Free Tour, Excursión, etc.
    private String imagenUrl;
    private String duracion;
    private String queIncluye;
    private String puntoEncuentro;
    private String idioma;
    private String politicaCancelacion;

    public Actividad(int id, int idDestino, String titulo, String descripcion, String tipo) {
        this.id = id;
        this.idDestino = idDestino;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
    }

    // Getters
    public int getId() { return id; }
    public int getIdDestino() { return idDestino; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getTipo() { return tipo; }
    public String getImagenUrl() { return imagenUrl; }
    public String getDuracion() { return duracion; }
    public String getQueIncluye() { return queIncluye; }
    public String getPuntoEncuentro() { return puntoEncuentro; }
    public String getIdioma() { return idioma; }
    public String getPoliticaCancelacion() { return politicaCancelacion; }
}
