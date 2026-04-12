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

    @Inject
    TokenManager tokenManager;

    @Inject
    AuthApiService authApiService;

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

        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Button btnIngresar = view.findViewById(R.id.btnIngresar);
        TextView textRegistro = view.findViewById(R.id.tvRegistrate);

        EditText etOtpCode = view.findViewById(R.id.etOtpCode);
        Button btnSolicitarOtp = view.findViewById(R.id.btnSolicitarOtp);
        Button btnVerificarOtp = view.findViewById(R.id.btnVerificarOtp);

        btnIngresar.setOnClickListener(v -> {
            String email = etNombre.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Completá email y contraseña.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnIngresar.setEnabled(false);
            Call<AuthResponse> request = authApiService.login(new AuthRequest(email, password));

            request.enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    btnIngresar.setEnabled(true);
                    if (!isAdded()) {
                        return;
                    }

                    AuthResponse authResponse = response.body();
                    if (!response.isSuccessful() || authResponse == null || authResponse.getToken() == null) {
                        Toast.makeText(requireContext(), "No se pudo iniciar sesión.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tokenManager.saveSession(
                            authResponse.getToken(),
                            authResponse.getEmail(),
                            authResponse.getName()
                    );

                    Bundle args = new Bundle();
                    String displayName = authResponse.getName();
                    args.putString("username", displayName == null || displayName.isEmpty() ? email : displayName);
                    Navigation.findNavController(view).navigate(R.id.action_auth_to_home, args);
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable throwable) {
                    btnIngresar.setEnabled(true);
                    if (!isAdded()) {
                        return;
                    }
                    Toast.makeText(requireContext(), "Error de red al iniciar sesión.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        textRegistro.setOnClickListener(v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_login_to_register);
        });

        btnSolicitarOtp.setOnClickListener(v -> {
            String email = etNombre.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Completá tu email primero.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSolicitarOtp.setEnabled(false);
            authApiService.requestOtp(new OtpRequest(email)).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                    btnSolicitarOtp.setEnabled(true);
                    if (!isAdded()) return;

                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Código enviado a tu email", Toast.LENGTH_SHORT).show();
                        // Hide password flow, show OTP flow
                        etPassword.setVisibility(View.GONE);
                        btnIngresar.setVisibility(View.GONE);
                        btnSolicitarOtp.setVisibility(View.GONE);
                        
                        etOtpCode.setVisibility(View.VISIBLE);
                        btnVerificarOtp.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(requireContext(), "Error al pedir OTP", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                    btnSolicitarOtp.setEnabled(true);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error de red (" + t.getMessage() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        btnVerificarOtp.setOnClickListener(v -> {
            String email = etNombre.getText().toString().trim();
            String otp = etOtpCode.getText().toString().trim();

            if (otp.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresá el código OTP.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnVerificarOtp.setEnabled(false);
            authApiService.verifyOtp(new OtpVerificationRequest(email, otp)).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    btnVerificarOtp.setEnabled(true);
                    if (!isAdded()) return;

                    AuthResponse authResponse = response.body();
                    if (!response.isSuccessful() || authResponse == null || authResponse.getToken() == null) {
                        Toast.makeText(requireContext(), "Código incorrecto o expirado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tokenManager.saveSession(
                            authResponse.getToken(),
                            authResponse.getEmail(),
                            authResponse.getName()
                    );

                    Bundle args = new Bundle();
                    String displayName = authResponse.getName();
                    args.putString("username", displayName == null || displayName.isEmpty() ? email : displayName);
                    Navigation.findNavController(view).navigate(R.id.action_auth_to_home, args);
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                    btnVerificarOtp.setEnabled(true);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}