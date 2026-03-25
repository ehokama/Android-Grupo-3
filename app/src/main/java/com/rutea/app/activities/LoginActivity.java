    package com.rutea.app.activities;

    import android.content.Intent;
    import android.os.Bundle;
    import android.widget.*;
    import com.rutea.app.R;

    import androidx.appcompat.app.AppCompatActivity;

    public class LoginActivity extends AppCompatActivity {
        private EditText etEmail,etPassword;
        private Button btnLogin;
        private TextView tvIrARegistro;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            etEmail      = findViewById(R.id.etEmail);
            etPassword   = findViewById(R.id.etPassword);
            btnLogin     = findViewById(R.id.btnLogin);
            tvIrARegistro = findViewById(R.id.tvIrARegistro);

            btnLogin.setOnClickListener(v -> {
                String email    = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Completá todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                startActivity(new Intent(this, MainActivity.class));
            });
            tvIrARegistro.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
            });
        }

    }
