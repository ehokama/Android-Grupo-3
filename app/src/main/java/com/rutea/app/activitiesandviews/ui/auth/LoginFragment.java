package com.rutea.app.activitiesandviews.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.data.models.dto.auth.AuthRequest;
import com.rutea.app.activitiesandviews.data.models.dto.auth.AuthResponse;
import com.rutea.app.activitiesandviews.data.models.dto.auth.OtpRequest;
import com.rutea.app.activitiesandviews.data.models.dto.auth.OtpVerificationRequest;
import com.rutea.app.activitiesandviews.data.network.AuthApiService;
import com.rutea.app.activitiesandviews.ui.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private static final int ALLOWED_AUTHENTICATORS =
            Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL;

    private enum LoginMode {
        PASSWORD, OTP_REQUEST, OTP_VERIFY
    }

    private LoginMode currentMode = LoginMode.PASSWORD;
    private android.os.CountDownTimer countDownTimer;

    @Inject
    TokenManager tokenManager;

    @Inject
    AuthApiService authApiService;

    private LinearLayout layoutCredentials;
    private LinearLayout layoutBiometric;
    private TextView tvBiometricStatus;

    private EditText etNombre, etPassword, etOtpCode;
    private com.google.android.material.textfield.TextInputLayout tilEmail, tilPassword, tilOtpCode;
    private Button btnMainAction;
    private TextView tvSwitchMode, tvTimer, textRegistro;
    private android.widget.ProgressBar progressBar;
    private View btnReenviarOtp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutCredentials = view.findViewById(R.id.layoutCredentials);
        layoutBiometric = view.findViewById(R.id.layoutBiometric);
        tvBiometricStatus = view.findViewById(R.id.tvBiometricStatus);

        etNombre = view.findViewById(R.id.etNombre);
        etPassword = view.findViewById(R.id.etPassword);
        etOtpCode = view.findViewById(R.id.etOtpCode);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilOtpCode = view.findViewById(R.id.tilOtpCode);
        btnMainAction = view.findViewById(R.id.btnMainAction);
        tvSwitchMode = view.findViewById(R.id.tvSwitchMode);
        tvTimer = view.findViewById(R.id.tvTimer);
        textRegistro = view.findViewById(R.id.tvRegistrate);
        progressBar = view.findViewById(R.id.progressBar);
        btnReenviarOtp = view.findViewById(R.id.btnReenviarOtp);

        setupClickListeners();

        if (tokenManager.isBiometricEnabled()) {
            prepararPantallaBiometrica();
        } else {
            showCredentialForm();
        }
    }

    // -------------------------------------------------------------------------
    // Alternancia entre pantalla biométrica y formulario de credenciales
    // -------------------------------------------------------------------------

    private void showCredentialForm() {
        layoutCredentials.setVisibility(View.VISIBLE);
        layoutBiometric.setVisibility(View.GONE);
        updateUI(LoginMode.PASSWORD);
    }

    private void prepararPantallaBiometrica() {
        BiometricManager manager = BiometricManager.from(requireContext());
        int canAuth = manager.canAuthenticate(ALLOWED_AUTHENTICATORS);

        if (canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Sin seguridad configurada")
                    .setMessage("Tu dispositivo no tiene huella, PIN ni patrón configurados. ¿Querés configurarlos ahora para poder usar el acceso biométrico?")
                    .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                        if (isAdded() && getActivity() != null) {
                            getActivity().startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                        }
                        showCredentialForm();
                    })
                    .setNegativeButton("No, gracias", (dialog, which) -> showCredentialForm())
                    .show();
            return;
        }

        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            showCredentialForm();
            return;
        }

        layoutCredentials.setVisibility(View.GONE);
        layoutBiometric.setVisibility(View.VISIBLE);
        tvBiometricStatus.setText("Usá tu huella digital o PIN para ingresar a tu cuenta");

        View btnLanzarBiometria = requireView().findViewById(R.id.btnLanzarBiometria);
        btnLanzarBiometria.setOnClickListener(v -> lanzarPromptBiometrico());
    }

    private void lanzarPromptBiometrico() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Bienvenido de nuevo")
                .setSubtitle("Confirmá tu identidad para ingresar")
                .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                new BiometricPrompt.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        String token = tokenManager.getEncryptedToken();
                        String email = tokenManager.getEmail();
                        String name = tokenManager.getName();
                        if (token != null) {
                            tokenManager.saveSession(token, email, name);
                            goToHome(name != null && !name.isEmpty() ? name : email);
                        } else {
                            showCredentialForm();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        showCredentialForm();
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    // -------------------------------------------------------------------------
    // Oferta de activación biométrica tras un login exitoso
    // -------------------------------------------------------------------------

    private void ofrecerBiometria(AuthResponse authResponse) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Activar acceso biométrico")
                .setMessage("¿Querés usar tu huella digital la próxima vez que inicies sesión?")
                .setPositiveButton("Sí, activar", (dialog, which) -> {
                    tokenManager.setBiometricEnabled(true);
                    tokenManager.saveEncryptedToken(authResponse.getToken());
                    goToHome(authResponse.getName() != null ? authResponse.getName() : authResponse.getEmail());
                })
                .setNegativeButton("No, gracias", (dialog, which) -> {
                    tokenManager.setBiometricEnabled(false);
                    goToHome(authResponse.getName() != null ? authResponse.getName() : authResponse.getEmail());
                })
                .setCancelable(false)
                .show();
    }

    // -------------------------------------------------------------------------
    // Manejo de los tres modos de login: contraseña, solicitud OTP y verificación OTP
    // -------------------------------------------------------------------------

    private void setupClickListeners() {
        btnMainAction.setOnClickListener(v -> {
            switch (currentMode) {
                case PASSWORD:
                    handlePasswordLogin();
                    break;
                case OTP_REQUEST:
                    handleOtpRequest();
                    break;
                case OTP_VERIFY:
                    handleOtpVerify();
                    break;
            }
        });

        tvSwitchMode.setOnClickListener(v -> {
            if (currentMode == LoginMode.PASSWORD) {
                updateUI(LoginMode.OTP_REQUEST);
            } else {
                updateUI(LoginMode.PASSWORD);
            }
        });

        textRegistro.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_register)
        );

        btnReenviarOtp.setOnClickListener(v -> resendOtp());

        // Permite volver al formulario desde la pantalla biométrica
        View btnUsarCredenciales = requireView().findViewById(R.id.btnUsarCredenciales);
        btnUsarCredenciales.setOnClickListener(v -> showCredentialForm());
    }

    private void updateUI(LoginMode mode) {
        currentMode = mode;
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilOtpCode.setError(null);

        switch (mode) {
            case PASSWORD:
                tilPassword.setVisibility(View.VISIBLE);
                tilOtpCode.setVisibility(View.GONE);
                tvTimer.setVisibility(View.GONE);
                btnReenviarOtp.setVisibility(View.GONE);
                btnMainAction.setText("Ingresar");
                tvSwitchMode.setText("Ingresar con código al email");
                btnMainAction.setEnabled(true);
                break;
            case OTP_REQUEST:
                tilPassword.setVisibility(View.GONE);
                tilOtpCode.setVisibility(View.GONE);
                tvTimer.setVisibility(View.GONE);
                btnReenviarOtp.setVisibility(View.GONE);
                btnMainAction.setText("Pedir Código OTP");
                tvSwitchMode.setText("Volver a contraseña");
                btnMainAction.setEnabled(true);
                break;
            case OTP_VERIFY:
                tilPassword.setVisibility(View.GONE);
                tilOtpCode.setVisibility(View.VISIBLE);
                tvTimer.setVisibility(View.VISIBLE);
                btnReenviarOtp.setVisibility(View.GONE);
                btnMainAction.setText("Verificar y Entrar");
                tvSwitchMode.setText("Cambiar método");
                startTimer();
                break;
        }
    }

    private void handlePasswordLogin() {
        String email = etNombre.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            tilEmail.setError("Ingresá tu email");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Ingresá tu contraseña");
            return;
        }

        setLoading(true);
        authApiService.login(new AuthRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    saveAndNavigate(response.body());
                } else {
                    Toast.makeText(requireContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), "No se pudo iniciar sesión. Revisá tu conexión e intentá de nuevo.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleOtpRequest() {
        String email = etNombre.getText().toString().trim();
        if (email.isEmpty()) {
            tilEmail.setError("Ingresá tu email");
            return;
        }

        setLoading(true);
        authApiService.requestOtp(new OtpRequest(email)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    updateUI(LoginMode.OTP_VERIFY);
                    Toast.makeText(requireContext(), "Código enviado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al solicitar código", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                setLoading(false);
                String errorMsg = "Error de red";
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "El servidor tardó demasiado en responder (Timeout).";
                }
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                Log.e("LoginFragment", "Error en handleOtpRequest: " + t.getMessage());
            }
        });
    }

    private void handleOtpVerify() {
        String email = etNombre.getText().toString().trim();
        String otp = etOtpCode.getText().toString().trim();

        if (otp.length() < 6) {
            tilOtpCode.setError("El código debe ser de 6 dígitos");
            return;
        }

        setLoading(true);
        authApiService.verifyOtp(new OtpVerificationRequest(email, otp)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    saveAndNavigate(response.body());
                } else {
                    tilOtpCode.setError("Código incorrecto o expirado");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), "No se pudo verificar el código. Revisá tu conexión e intentá de nuevo.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAndNavigate(AuthResponse authResponse) {
        tokenManager.saveSession(authResponse.getToken(), authResponse.getEmail(), authResponse.getName());
        ofrecerBiometria(authResponse);
    }

    // -------------------------------------------------------------------------
    // Navegación y utilidades
    // -------------------------------------------------------------------------

    private void goToHome(String username) {
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).onUserLoggedIn();
        }
        Bundle args = new Bundle();
        args.putString("username", username);
        Navigation.findNavController(requireView()).navigate(R.id.action_auth_to_home, args);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnMainAction.setEnabled(!loading);
        btnReenviarOtp.setEnabled(!loading);
        etNombre.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etOtpCode.setEnabled(!loading);
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        btnMainAction.setEnabled(true);
        btnReenviarOtp.setVisibility(View.GONE);

        countDownTimer = new android.os.CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Reenviar disponible en " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                if (!isAdded()) return;
                tvTimer.setText("¿No te llegó el código?");
                btnReenviarOtp.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void resendOtp() {
        String email = etNombre.getText().toString().trim();
        if (email.isEmpty()) {
            tilEmail.setError("Ingresá tu email");
            return;
        }

        setLoading(true);
        authApiService.requestOtp(new OtpRequest(email)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                setLoading(false);
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    etOtpCode.setText("");
                    tilOtpCode.setError(null);
                    btnReenviarOtp.setVisibility(View.GONE);
                    startTimer();
                    Toast.makeText(requireContext(), "Código reenviado a tu email", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "No se pudo reenviar el código", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                setLoading(false);
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red al reenviar código", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
