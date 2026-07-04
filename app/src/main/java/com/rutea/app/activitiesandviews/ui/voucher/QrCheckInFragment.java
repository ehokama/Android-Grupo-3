package com.rutea.app.activitiesandviews.ui.voucher;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.voucher.CheckInRequest;
import com.rutea.app.activitiesandviews.data.models.dto.voucher.CheckInResponse;
import com.rutea.app.activitiesandviews.data.network.VoucherApiService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class QrCheckInFragment extends Fragment {

    private static final String TAG = "QrCheckInFragment";
    private static final String ARG_RESERVE_ID = "reserveId";
    private static final String QR_CHECK_IN_VALUE = "true";

    @Inject VoucherApiService voucherApiService;

    private PreviewView previewView;
    private TextView tvScanHint, tvScanResult;
    private MaterialCardView cardResult;
    private MaterialButton btnScanAgain;
    private ProgressBar progressScan;

    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final AtomicBoolean scanLocked = new AtomicBoolean(false);
    private long reserveId = -1;

    public QrCheckInFragment() {
        super(R.layout.fragment_qr_check_in);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        iniciarCamara();
                    } else {
                        Toast.makeText(requireContext(),
                                "Se necesita el permiso de cámara para escanear QR",
                                Toast.LENGTH_LONG).show();
                    }
                });

        BarcodeScannerOptions opciones = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(opciones);
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.previewView);
        tvScanHint = view.findViewById(R.id.tvScanHint);
        tvScanResult = view.findViewById(R.id.tvScanResult);
        cardResult = view.findViewById(R.id.cardResult);
        btnScanAgain = view.findViewById(R.id.btnScanAgain);
        progressScan = view.findViewById(R.id.progressScan);

        btnScanAgain.setOnClickListener(v -> resetScan());

        reserveId = getArguments() != null ? getArguments().getLong(ARG_RESERVE_ID, -1) : -1;
        if (reserveId <= 0) {
            Toast.makeText(requireContext(), "Reserva no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        pedirPermisoCamara();
    }

    private void pedirPermisoCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            iniciarCamara();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void iniciarCamara() {
        ListenableFuture<ProcessCameraProvider> futuro =
                ProcessCameraProvider.getInstance(requireContext());

        futuro.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = futuro.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis analisis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                analisis.setAnalyzer(cameraExecutor, this::analizarFrame);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analisis);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error al iniciar cámara", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void analizarFrame(@NonNull ImageProxy imageProxy) {
        if (scanLocked.get()) {
            imageProxy.close();
            return;
        }

        InputImage imagen = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());

        barcodeScanner.process(imagen)
                .addOnSuccessListener(codigos -> {
                    for (Barcode codigo : codigos) {
                        String valor = codigo.getRawValue();
                        if (valor != null && !valor.isBlank()) {
                            procesarQr(valor);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error ML Kit", e))
                .addOnCompleteListener(t -> imageProxy.close());
    }

    private void procesarQr(String rawValue) {
        if (!processing.compareAndSet(false, true)) return;

        if (!QR_CHECK_IN_VALUE.equalsIgnoreCase(rawValue.trim())) {
            processing.set(false);
            requireActivity().runOnUiThread(() ->
                    mostrarError("QR inválido: se esperaba confirmación de check-in."));
            return;
        }

        scanLocked.set(true);

        requireActivity().runOnUiThread(() -> {
            progressScan.setVisibility(View.VISIBLE);
            tvScanHint.setVisibility(View.GONE);
        });

        voucherApiService.checkIn(new CheckInRequest(reserveId, true))
                .enqueue(new Callback<CheckInResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CheckInResponse> call,
                                           @NonNull Response<CheckInResponse> response) {
                        if (!isAdded()) return;
                        progressScan.setVisibility(View.GONE);
                        processing.set(false);

                        if (response.isSuccessful() && response.body() != null) {
                            mostrarResultado(response.body());
                        } else {
                            mostrarError("No se pudo validar el QR. Intentá de nuevo.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CheckInResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        progressScan.setVisibility(View.GONE);
                        processing.set(false);
                        scanLocked.set(false);
                        mostrarError("Error de conexión. Revisá tu red e intentá de nuevo.");
                    }
                });
    }

    private void mostrarResultado(CheckInResponse result) {
        tvScanResult.setVisibility(View.VISIBLE);
        tvScanResult.setText(result.getMessage());

        int colorRes = result.isSuccess() ? R.color.checkin_success : R.color.checkin_error;
        int bgColor = ContextCompat.getColor(requireContext(), colorRes);
        cardResult.setCardBackgroundColor(bgColor);
        tvScanResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));

        btnScanAgain.setVisibility(result.isSuccess() ? View.GONE : View.VISIBLE);
    }

    private void mostrarError(String message) {
        tvScanResult.setVisibility(View.VISIBLE);
        tvScanResult.setText(message);
        int bgColor = ContextCompat.getColor(requireContext(), R.color.checkin_error);
        cardResult.setCardBackgroundColor(bgColor);
        tvScanResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnScanAgain.setVisibility(View.VISIBLE);
    }

    private void resetScan() {
        scanLocked.set(false);
        processing.set(false);
        tvScanHint.setVisibility(View.VISIBLE);
        tvScanResult.setVisibility(View.GONE);
        btnScanAgain.setVisibility(View.GONE);
        cardResult.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), android.R.color.white));
        pedirPermisoCamara();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (barcodeScanner != null) barcodeScanner.close();
    }
}
