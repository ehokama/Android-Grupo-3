package com.rutea.app.api;

import com.rutea.app.models.Actividad;
import com.rutea.app.models.Destino;
import com.rutea.app.models.Disponibilidad;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("actividades")
    Call<List<Actividad>> getActividades(
            @Query("destino") Integer idDestino,
            @Query("tipo") String tipo,
            @Query("precio_min") Double precioMin,
            @Query("precio_max") Double precioMax,
            @Query("fecha") String fecha
    );

    @GET("actividades/{id}")
    Call<Actividad> getActividadDetalle(@Path("id") int idActividad);

    @GET("actividades/{id}/disponibilidad")
    Call<List<Disponibilidad>> getDisponibilidad(@Path("id") int idActividad);

    @GET("destinos")
    Call<List<Destino>> getDestinos();
}
