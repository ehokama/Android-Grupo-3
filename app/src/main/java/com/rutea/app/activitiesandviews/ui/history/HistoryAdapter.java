package com.rutea.app.activitiesandviews.ui.history;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.reserve.ReserveDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

    private final List<ReserveDto> items;
    private final Consumer<Long> onCancelClick;
    private final Consumer<ReserveDto> onRateClick;

    public HistoryAdapter(List<ReserveDto> items, Consumer<Long> onCancelClick, Consumer<ReserveDto> onRateClick) {
        this.items = items;
        this.onCancelClick = onCancelClick;
        this.onRateClick = onRateClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_reserve, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView ivActivityImage;
        TextView tvTitle, tvDate, tvPeople, tvPrice, tvReserveId, tvActivityDate;
        Chip chipState;
        Button btnCancel, btnRate;
        TextView tvReviewSummary;

        VH(@NonNull View v) {
            super(v);
            ivActivityImage = v.findViewById(R.id.ivActivityImage);
            tvTitle         = v.findViewById(R.id.tvActivityTitle);
            tvDate          = v.findViewById(R.id.tvReserveDate);
            tvActivityDate  = v.findViewById(R.id.tvActivityDate);
            tvPeople        = v.findViewById(R.id.tvPeople);
            tvPrice         = v.findViewById(R.id.tvTotalPrice);
            tvReserveId     = v.findViewById(R.id.tvReserveId);
            chipState       = v.findViewById(R.id.chipState);
            btnCancel       = v.findViewById(R.id.btnCancel);
            btnRate         = v.findViewById(R.id.btnRate);
            tvReviewSummary = v.findViewById(R.id.tvReviewSummary);
        }

        void bind(ReserveDto r) {
            tvTitle.setText(r.getActivityTitle() != null ? r.getActivityTitle() : "Actividad");
            tvReserveId.setText("Reserva #" + r.getIdReserve());
            tvDate.setText("Creada: " + formatDate(r.getCreationDate()));
            tvActivityDate.setText("Fecha actividad: " + formatDate(r.getReservationDate()));
            tvPeople.setText(r.getNumberOfPeople() + (r.getNumberOfPeople() == 1 ? " persona" : " personas"));
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", r.getTotalPrice() != null ? r.getTotalPrice() : 0.0));

            // ─── Estado chip ───
            bindStateChip(r.getState());

            // ─── Botón cancelar: solo si PENDING o CONFIRMED ───
            String state = r.getState() != null ? r.getState() : "";
            boolean cancellable = state.equals("PENDING") || state.equals("CONFIRMED");
            btnCancel.setVisibility(cancellable ? View.VISIBLE : View.GONE);
            btnCancel.setOnClickListener(v -> {
                if (onCancelClick != null) onCancelClick.accept(r.getIdReserve());
            });

            boolean showRateButton = r.isCanRate() && !r.isAlreadyRated();
            btnRate.setVisibility(showRateButton ? View.VISIBLE : View.GONE);
            btnRate.setOnClickListener(v -> {
                if (onRateClick != null) onRateClick.accept(r);
            });

            if (r.isAlreadyRated() && r.getMyActivityRating() != null && r.getMyGuideRating() != null) {
                String summary = "Tu calificación: Actividad " + r.getMyActivityRating() + "/5 • Guía " + r.getMyGuideRating() + "/5";
                if (r.getMyComment() != null && !r.getMyComment().trim().isEmpty()) {
                    summary += "\n\"" + r.getMyComment().trim() + "\"";
                }
                tvReviewSummary.setText(summary);
                tvReviewSummary.setVisibility(View.VISIBLE);
            } else if ("COMPLETED".equals(state) && !r.isCanRate() && !r.isAlreadyRated()) {
                tvReviewSummary.setText("Ventana de calificación cerrada (48h).");
                tvReviewSummary.setVisibility(View.VISIBLE);
            } else {
                tvReviewSummary.setVisibility(View.GONE);
            }

// ─── Imagen desde lista de imágenes de la actividad ───
            if (r.getActivityImages() != null && !r.getActivityImages().isEmpty()) {
                Long imageId = r.getActivityImages().get(0).getIdImage();
                String imageUrl = "http://10.0.2.2:8080/api/images/" + imageId + "/raw";
                Glide.with(ivActivityImage.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_hero_landscape)
                        .error(R.drawable.bg_hero_landscape)
                        .centerCrop()
                        .into(ivActivityImage);
            } else {
                ivActivityImage.setImageResource(R.drawable.bg_hero_landscape);
            }
        }

        private void bindStateChip(String state) {
            if (state == null) state = "";
            int colorRes;
            String label;
            switch (state) {
                case "COMPLETED":
                    label    = "Completada";
                    colorRes = R.color.state_completed;
                    break;
                case "CONFIRMED":
                    label    = "Confirmada";
                    colorRes = R.color.state_confirmed;
                    break;
                case "PENDING":
                    label    = "Pendiente";
                    colorRes = R.color.state_pending;
                    break;
                case "CANCELLED":
                    label    = "Cancelada";
                    colorRes = R.color.state_cancelled;
                    break;
                default:
                    label    = state;
                    colorRes = R.color.state_pending;
            }
            chipState.setText(label);
            chipState.setChipBackgroundColor(
                    ColorStateList.valueOf(ContextCompat.getColor(chipState.getContext(), colorRes)));
        }

        private String formatDate(String raw) {
            if (raw == null || raw.isEmpty()) return "—";
            try {
                return LocalDateTime.parse(raw)
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault()));
            } catch (Exception e) {
                return raw.replace("T", " ");
            }
        }
    }
}