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
import com.rutea.app.activitiesandviews.ui.data.network.RetrofitClient;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.AuthRequest;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.AuthResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

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

        RetrofitClient.init(requireContext());
        AuthApiService authApiService = RetrofitClient.createService(AuthApiService.class);

        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Button btnIngresar = view.findViewById(R.id.btnIngresar);
        TextView textRegistro = view.findViewById(R.id.tvRegistrate);

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

                    RetrofitClient.getSessionManager().saveSession(
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
    }
}