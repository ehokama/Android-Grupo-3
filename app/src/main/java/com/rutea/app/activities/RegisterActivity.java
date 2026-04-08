package com.rutea.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.rutea.app.R;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNombre, etEmail, etPassword, etConfirmPassword;
    private Button btnRegistrarse;
    private TextView tvIrALogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etNombre          = findViewById(R.id.etNombre);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegistrarse    = findViewById(R.id.btnRegistrarse);
        tvIrALogin        = findViewById(R.id.tvIrALogin);

        btnRegistrarse.setOnClickListener(v -> {
            String nombre   = etNombre.getText().toString().trim();
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm  = etConfirmPassword.getText().toString().trim();

            // validaciones basicas
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Completá todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            // api
            registroExitoso("token_de_prueba");
        });

        tvIrALogin.setOnClickListener(v -> finish()); // finish() vuelve a la pantalla anterior
    }

    private void registroExitoso(String token) {
        // guardar token
        SharedPreferences prefs = getSharedPreferences("rutea_prefs", MODE_PRIVATE);
        prefs.edit().putString("auth_token", token).apply();

        // homeActivity limpiando backstack
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}