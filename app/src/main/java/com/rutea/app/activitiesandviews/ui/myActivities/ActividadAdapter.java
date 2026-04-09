package com.rutea.app.activitiesandviews.ui.myActivities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rutea.app.R;
import java.util.List;
public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ViewHolder> {

    private List<Actividad> lista;

    public ActividadAdapter(List<Actividad> lista) {
        this.lista = lista;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView titulo, destino, tipo, duracion, precio, cupos;

        public ViewHolder(View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.iv_actividad_imagen);
            titulo = itemView.findViewById(R.id.tv_actividad_titulo);
            destino = itemView.findViewById(R.id.tv_actividad_destino);
            tipo = itemView.findViewById(R.id.tv_actividad_tipo);
            duracion = itemView.findViewById(R.id.tv_actividad_duracion);
            precio = itemView.findViewById(R.id.tv_actividad_precio);
            cupos = itemView.findViewById(R.id.tv_actividad_cupos);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_actividad, parent, false); // 👈 TU XML
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Actividad a = lista.get(position);

        holder.titulo.setText(a.titulo);
        holder.destino.setText(a.destino);
        holder.tipo.setText(a.tipo);
        holder.duracion.setText(a.duracion);
        holder.precio.setText(a.precio);
        holder.cupos.setText(a.cupos);
        holder.imagen.setImageResource(a.imagen);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}