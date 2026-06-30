package com.rutea.app.activitiesandviews.ui.voucher;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.voucher.VoucherDto;
import com.rutea.app.activitiesandviews.data.network.VoucherApiService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class VoucherFragment extends Fragment {

    private static final String ARG_RESERVE_ID = "reserveId";

    @Inject VoucherApiService voucherApiService;

    private ProgressBar progressVoucher;
    private TextView tvVoucherCode, tvVoucherActivity, tvVoucherDate,
            tvVoucherMeetingPoint, tvVoucherGuide, tvVoucherPeople;
    private Chip chipVoucherCheckedIn;
    private MaterialButton btnScanQr;

    public VoucherFragment() {
        super(R.layout.fragment_voucher);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressVoucher = view.findViewById(R.id.progressVoucher);
        tvVoucherCode = view.findViewById(R.id.tvVoucherCode);
        tvVoucherActivity = view.findViewById(R.id.tvVoucherActivity);
        tvVoucherDate = view.findViewById(R.id.tvVoucherDate);
        tvVoucherMeetingPoint = view.findViewById(R.id.tvVoucherMeetingPoint);
        tvVoucherGuide = view.findViewById(R.id.tvVoucherGuide);
        tvVoucherPeople = view.findViewById(R.id.tvVoucherPeople);
        chipVoucherCheckedIn = view.findViewById(R.id.chipVoucherCheckedIn);
        btnScanQr = view.findViewById(R.id.btnScanQr);

        long reserveId = getArguments() != null ? getArguments().getLong(ARG_RESERVE_ID, -1) : -1;
        if (reserveId <= 0) {
            Toast.makeText(requireContext(), "Reserva no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        btnScanQr.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong(ARG_RESERVE_ID, reserveId);
            Navigation.findNavController(view).navigate(R.id.action_voucher_to_qrCheckIn, args);
        });

        loadVoucher(reserveId);
    }

    private void loadVoucher(long reserveId) {
        progressVoucher.setVisibility(View.VISIBLE);
        voucherApiService.getVoucher(reserveId).enqueue(new Callback<VoucherDto>() {
            @Override
            public void onResponse(@NonNull Call<VoucherDto> call, @NonNull Response<VoucherDto> response) {
                if (!isAdded()) return;
                progressVoucher.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    bindVoucher(response.body());
                } else {
                    Toast.makeText(requireContext(), "No se pudo cargar el voucher", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<VoucherDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressVoucher.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error de conexión al cargar el voucher", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindVoucher(VoucherDto voucher) {
        tvVoucherCode.setText("Código: " + (voucher.getVoucherCode() != null ? voucher.getVoucherCode() : "—"));
        tvVoucherActivity.setText(voucher.getActivityTitle());
        tvVoucherDate.setText("Fecha y horario: " + formatDate(voucher.getReservationDate()));
        tvVoucherMeetingPoint.setText("Punto de encuentro: " + safe(voucher.getMeetingPoint()));
        tvVoucherGuide.setText("Guía: " + safe(voucher.getGuideName()));
        int people = voucher.getNumberOfPeople() != null ? voucher.getNumberOfPeople() : 0;
        tvVoucherPeople.setText("Participantes: " + people);

        if (voucher.isCheckedIn()) {
            chipVoucherCheckedIn.setVisibility(View.VISIBLE);
            chipVoucherCheckedIn.setChipBackgroundColor(
                    android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(requireContext(), R.color.state_completed)));
        } else {
            chipVoucherCheckedIn.setVisibility(View.GONE);
        }

        boolean canCheckIn = "CONFIRMED".equals(voucher.getState()) && !voucher.isCheckedIn();
        btnScanQr.setVisibility(canCheckIn ? View.VISIBLE : View.GONE);
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

    private String safe(String value) {
        return value == null || value.isBlank() ? "No disponible" : value;
    }
}
