package com.rutea.app.models;

import com.google.gson.annotations.SerializedName;

public class Guia {
    @SerializedName("id_guia")
    private int id;
    @SerializedName("nombre_completo")
    private String nombreCompleto;
    private String telefono;
    private String email;

    public Guia(int id, String nombreCompleto, String telefono, String email) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.email = email;
    }

    public int getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
}
