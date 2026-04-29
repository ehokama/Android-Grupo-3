package com.rutea.app.activitiesandviews.ui.biometric;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.rutea.app.R;

import java.util.concurrent.Executor;

public class BiometricFragment extends Fragment {

    // Combinación de autenticadores permitidos: biometría fuerte O credencial del dispositivo (PIN/patrón/contraseña)
    private static final int ALLOWED_AUTHENTICATORS =
            Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL;

    private TextView tvStatus;
    private Button btnAuthenticate;
    private Button btnGoToSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_biometric, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvStatus = view.findViewById(R.id.tvStatus);
        btnAuthenticate = view.findViewById(R.id.btnAuthenticate);
        btnGoToSettings = view.findViewById(R.id.btnGoToSettings);

        // PASO 1: Verificar disponibilidad con BiometricManager
        checkBiometricAvailability();

        // PASO 2: Configurar click del botón principal
        btnAuthenticate.setOnClickListener(v -> launchBiometricPrompt());

        // PASO 3 (opcional): Si no hay enrolamiento, ofrecer ir a Ajustes
        btnGoToSettings.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
            startActivity(intent);
        });
    }

    /**
     * PASO 1 — BiometricManager: preguntar si la biometría está disponible.
     * Siempre hay que hacer esta verificación antes de mostrar el prompt.
     */
    private void checkBiometricAvailability() {
        BiometricManager manager = BiometricManager.from(requireContext());
        int result = manager.canAuthenticate(ALLOWED_AUTHENTICATORS);

        switch (result) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                tvStatus.setText("Listo para autenticar. Presiona el botón.");
                btnAuthenticate.setEnabled(true);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                tvStatus.setText("Este dispositivo no tiene sensor biométrico.");
                btnAuthenticate.setEnabled(false);
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                tvStatus.setText("El sensor biométrico no está disponible ahora.");
                btnAuthenticate.setEnabled(false);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                tvStatus.setText("No hay biometría enrolada en el dispositivo.");
                btnAuthenticate.setEnabled(false);
                btnGoToSettings.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * PASO 2 — PromptInfo: configurar cómo se ve el diálogo del sistema.
     * PASO 3 — BiometricPrompt: disparar el prompt y escuchar los tres callbacks.
     */
    private void launchBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación Biométrica")
                .setSubtitle("Confirmá tu identidad")
                .setDescription("Usá tu huella digital o el PIN del dispositivo")
                // Con DEVICE_CREDENTIAL no se puede usar setNegativeButtonText()
                .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
                .build();

        Executor executor = ContextCompat.getMainExecutor(requireContext());

        BiometricPrompt biometricPrompt = new BiometricPrompt(
                this,
                executor,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        tvStatus.setText("¡Autenticación exitosa!");
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        tvStatus.setText("Intento fallido. Reintentá.");
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        tvStatus.setText("Error: " + errString);
                    }
                }
        );

        biometricPrompt.authenticate(promptInfo);
    }
}
