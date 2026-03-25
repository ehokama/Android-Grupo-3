package com.example.xplorernow.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xplorernow.databinding.ItemActividadBinding;
import com.example.xplorernow.models.Actividad;

import java.util.ArrayList;
import java.util.List;

public class ActividadAdapter extends RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder> {

    private List<Actividad> actividades = new ArrayList<>();
    private final OnActividadClickListener listener;

    public interface OnActividadClickListener {
        void onActividadClick(Actividad actividad);
    }

    public ActividadAdapter(OnActividadClickListener listener) {
        this.listener = listener;
    }

    public void setActividades(List<Actividad> actividades) {
        this.actividades = actividades;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActividadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemActividadBinding binding = ItemActividadBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ActividadViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ActividadViewHolder holder, int position) {
        holder.bind(actividades.get(position));
    }

    @Override
    public int getItemCount() {
        return actividades.size();
    }

    class ActividadViewHolder extends RecyclerView.ViewHolder {
        private final ItemActividadBinding binding;

        public ActividadViewHolder(@NonNull ItemActividadBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Actividad actividad) {
            binding.tvActividadTitulo.setText(actividad.getTitulo());
            binding.tvActividadTipo.setText(actividad.getTipo());
            binding.tvActividadDuracion.setText(actividad.getDuracion());
            
            // For now, setting placeholder values for fields that might come from related models
            // In a real scenario, you'd fetch these from the API response objects
            binding.tvActividadDestino.setText("Destino ID: " + actividad.getIdDestino());
            binding.tvActividadPrecio.setText("$ ---"); // Price usually comes from Disponibilidad
            binding.tvActividadCupos.setText("Consultar cupos");

            Glide.with(binding.ivActividadImagen.getContext())
                    .load(actividad.getImagenUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivActividadImagen);

            binding.getRoot().setOnClickListener(v -> listener.onActividadClick(actividad));
        }
    }
}
