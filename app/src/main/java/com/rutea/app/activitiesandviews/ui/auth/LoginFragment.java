package com.rutea.app.activitiesandviews.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.ui.data.network.AuthApiService;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.AuthRequest;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.AuthResponse;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.OtpRequest;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.OtpVerificationRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.rutea.app.data.local.TokenManager;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private enum LoginMode {
        PASSWORD, OTP_REQUEST, OTP_VERIFY
    }

    private LoginMode currentMode = LoginMode.PASSWORD;
    private android.os.CountDownTimer countDownTimer;

    @Inject
    TokenManager tokenManager;

    @Inject
    AuthApiService authApiService;

    private EditText etNombre, etPassword, etOtpCode;
    private com.google.android.material.textfield.TextInputLayout tilEmail, tilPassword, tilOtpCode;
    private Button btnMainAction;
    private TextView tvSwitchMode, tvTimer, textRegistro;
    private android.widget.ProgressBar progressBar;

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

        // Initialize Views
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

        setupClickListeners();
        updateUI(LoginMode.PASSWORD);
    }

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
                btnMainAction.setText("Ingresar");
                tvSwitchMode.setText("Ingresar con código al email");
                btnMainAction.setEnabled(true);
                break;
            case OTP_REQUEST:
                tilPassword.setVisibility(View.GONE);
                tilOtpCode.setVisibility(View.GONE);
                tvTimer.setVisibility(View.GONE);
                btnMainAction.setText("Pedir Código OTP");
                tvSwitchMode.setText("Volver a contraseña");
                btnMainAction.setEnabled(true);
                break;
            case OTP_VERIFY:
                tilPassword.setVisibility(View.GONE);
                tilOtpCode.setVisibility(View.VISIBLE);
                tvTimer.setVisibility(View.VISIBLE);
                btnMainAction.setText("Verificar y Entrar");
                tvSwitchMode.setText("Cambiar método / Reenviar");
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
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
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
        authApiService.requestOtp(new OtpRequest(email)).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    updateUI(LoginMode.OTP_VERIFY);
                    Toast.makeText(requireContext(), "Código enviado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Error al solicitar código", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAndNavigate(AuthResponse authResponse) {
        tokenManager.saveSession(authResponse.getToken(), authResponse.getEmail(), authResponse.getName());
        Bundle args = new Bundle();
        args.putString("username", authResponse.getName() != null ? authResponse.getName() : authResponse.getEmail());
        Navigation.findNavController(requireView()).navigate(R.id.action_auth_to_home, args);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnMainAction.setEnabled(!loading);
        etNombre.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etOtpCode.setEnabled(!loading);
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        btnMainAction.setEnabled(true);
        
        countDownTimer = new android.os.CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Reenviar disponible en " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Ya podés solicitar un nuevo código");
            }
        }.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}