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
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.AuthResponse;
import com.rutea.app.activitiesandviews.ui.data.network.dto.auth.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RetrofitClient.init(requireContext());
        AuthApiService authApiService = RetrofitClient.createService(AuthApiService.class);

        EditText etNombre = view.findViewById(R.id.etNombre);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        Button btnRegistrar = view.findViewById(R.id.btnRegistrar);
        TextView tvYaTengoLogin = view.findViewById(R.id.tvYaTengoLogin);

        btnRegistrar.setOnClickListener(v -> {
            String name = etNombre.getText() == null ? "" : etNombre.getText().toString().trim();
            String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
            String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Completá todos los campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegistrar.setEnabled(false);
            RegisterRequest request = new RegisterRequest(email, password, name, null, null);
            authApiService.register(request).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                    btnRegistrar.setEnabled(true);
                    if (!isAdded()) {
                        return;
                    }

                    if (!response.isSuccessful() || response.body() == null) {
                        Toast.makeText(requireContext(), "No se pudo registrar la cuenta.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(requireContext(), "Registro exitoso, iniciá sesión.", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_register_to_login);
                }

                @Override
                public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable throwable) {
                    btnRegistrar.setEnabled(true);
                    if (!isAdded()) {
                        return;
                    }
                    Toast.makeText(requireContext(), "Error de red al registrarse.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        tvYaTengoLogin.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_register_to_login));
    }
}