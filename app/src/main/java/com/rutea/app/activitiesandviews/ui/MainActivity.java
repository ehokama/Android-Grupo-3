package com.rutea.app.activitiesandviews.ui;

import com.rutea.app.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.di.NetworkModule;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    TokenManager tokenManager;

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    // Receptor que escucha cuando el token expira en cualquier parte de la app
    private final BroadcastReceiver sessionExpiredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Tu sesión expiró. Por favor, volvé a iniciar sesión.", Toast.LENGTH_LONG).show();
            if (navController != null) {
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build();
                navController.navigate(R.id.auth_nav_graph, null, navOptions);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav_view);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // Si ya hay un token guardado, saltar directamente al Home
            if (tokenManager.hasToken()) {
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.auth_nav_graph, true)
                        .build();
                navController.navigate(R.id.home_nav_graph, null, navOptions);
            }

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();

                if (    id == R.id.homeFragment ||
                        id == R.id.profileFragment ||
                        id == R.id.historyFragment ||
                        id == R.id.favoritesFragment) {

                    bottomNavigationView.setVisibility(View.VISIBLE);
                } else {
                    bottomNavigationView.setVisibility(View.GONE);
                }
            });
        }

        // Registrar el receptor para escuchar sesiones expiradas
        IntentFilter filter = new IntentFilter(NetworkModule.ACTION_SESSION_EXPIRED);
        registerReceiver(sessionExpiredReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sessionExpiredReceiver);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}