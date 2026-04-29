package com.rutea.app.activitiesandviews.ui.history;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.reserve.ReserveDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ReserveAdapter extends RecyclerView.Adapter<ReserveAdapter.ViewHolder> {

    public interface OnReserveCancelListener {
        void onCancelClick(Long reserveId);
    }

    private List<ReserveDto> reserves;
    private OnReserveCancelListener cancelListener;

    public ReserveAdapter(List<ReserveDto> reserves, OnReserveCancelListener cancelListener) {
        this.reserves = reserves;
        this.cancelListener = cancelListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvState, tvDate, tvPeople, tvPrice;
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvReserveTitle);
            tvState = itemView.findViewById(R.id.tvReserveState);
            tvDate = itemView.findViewById(R.id.tvReserveDate);
            tvPeople = itemView.findViewById(R.id.tvReservePeople);
            tvPrice = itemView.findViewById(R.id.tvReservePrice);
            btnCancel = itemView.findViewById(R.id.btnCancelReserve);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserve, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReserveDto reserve = reserves.get(position);

        holder.tvTitle.setText(reserve.getActivityTitle() != null ? reserve.getActivityTitle() : "Reserva #" + reserve.getIdReserve());
        holder.tvDate.setText("Fecha: " + formatDateTime(reserve.getCreationDate()));
        holder.tvPeople.setText("Personas: " + (reserve.getNumberOfPeople() != null ? reserve.getNumberOfPeople() : 1));
        holder.tvPrice.setText(String.format(Locale.getDefault(), "$ %.2f", reserve.getTotalPrice() != null ? reserve.getTotalPrice() : 0.0));

        String state = reserve.getState() != null ? reserve.getState() : "UNKNOWN";
        holder.tvState.setText(state);

        if ("COMPLETED".equalsIgnoreCase(state)) {
            holder.tvState.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            holder.btnCancel.setVisibility(View.GONE);
        } else if ("CANCELLED".equalsIgnoreCase(state) || "CANCELED".equalsIgnoreCase(state)) {
            holder.tvState.setBackgroundColor(Color.parseColor("#F44336")); // Red
            holder.btnCancel.setVisibility(View.GONE);
        } else if ("CONFIRMED".equalsIgnoreCase(state)) {
            holder.tvState.setBackgroundColor(Color.parseColor("#2196F3")); // Blue
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            // PENDING or others
            holder.tvState.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
            holder.btnCancel.setVisibility(View.VISIBLE);
        }

        holder.btnCancel.setOnClickListener(v -> {
            if (cancelListener != null && reserve.getIdReserve() != null) {
                cancelListener.onCancelClick(reserve.getIdReserve());
            }
        });
    }

    @Override
    public int getItemCount() {
        return reserves.size();
    }

    private String formatDateTime(String rawDateTime) {
        if (rawDateTime == null || rawDateTime.isEmpty()) {
            return "Fecha no disponible";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(rawDateTime);
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault()));
        } catch (Exception e) {
            return rawDateTime.replace("T", " ");
        }
    }
}
