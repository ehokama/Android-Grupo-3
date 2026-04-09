package com.rutea.app.activitiesandviews.ui.myActivities;

public class Actividad {
    public String titulo;
    public String destino;
    public String tipo;
    public String duracion;
    public String precio;
    public String cupos;
    public int imagen;

    public Actividad(String titulo, String destino, String tipo,
                     String duracion, String precio, String cupos, int imagen) {
        this.titulo = titulo;
        this.destino = destino;
        this.tipo = tipo;
        this.duracion = duracion;
        this.precio = precio;
        this.cupos = cupos;
        this.imagen = imagen;
    }
}