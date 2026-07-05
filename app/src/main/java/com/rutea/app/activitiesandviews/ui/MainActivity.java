package com.rutea.app.activitiesandviews.ui;

import com.rutea.app.R;
import android.content.BroadcastReceiver;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rutea.app.activitiesandviews.data.local.TokenManager;
import com.rutea.app.activitiesandviews.di.NetworkModule;
import com.rutea.app.activitiesandviews.service.NotificationPollingService;
import com.rutea.app.activitiesandviews.util.NotificationHelper;

import android.os.Build;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    TokenManager tokenManager;

    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private TextView tvOfflineBanner;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    private ActivityResultLauncher<String> notifPermissionLauncher;

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

        notifPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted && tokenManager.hasToken()) {
                        NotificationHelper.startPollingService(this);
                    }
                });

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        tvOfflineBanner = findViewById(R.id.tvOfflineBanner);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            if (tokenManager.hasToken()) {
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.auth_nav_graph, true)
                        .build();
                navController.navigate(R.id.home_nav_graph, null, navOptions);
                setupNotifications();
            }

            handleNotificationDeepLink(getIntent());

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

        IntentFilter filter = new IntentFilter(NetworkModule.ACTION_SESSION_EXPIRED);
        registerReceiver(sessionExpiredReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        observeConnectivity();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationDeepLink(intent);
    }

    private void setupNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.startPollingService(this);
            } else {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            NotificationHelper.startPollingService(this);
        }
    }

    private void handleNotificationDeepLink(Intent intent) {
        if (intent == null || navController == null) return;
        String action = intent.getAction();

        if (NotificationPollingService.ACTION_OPEN_VOUCHER.equals(action)) {
            long reserveId = intent.getLongExtra(NotificationPollingService.EXTRA_RESERVE_ID, -1);
            intent.setAction(null);
            if (reserveId > 0) {
                Bundle args = new Bundle();
                args.putLong("reserveId", reserveId);
                navController.navigate(R.id.voucherFragment, args);
            } else {
                navController.navigate(R.id.historyFragment);
            }
        } else if (NotificationPollingService.ACTION_OPEN_HISTORY.equals(action)) {
            intent.setAction(null);
            navController.navigate(R.id.historyFragment);
        }
    }

    public void onUserLoggedIn() {
        setupNotifications();
    }

    @Override
    protected void onDestroy() {
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        super.onDestroy();
        unregisterReceiver(sessionExpiredReceiver);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void observeConnectivity() {
        connectivityManager = getSystemService(ConnectivityManager.class);
        if (connectivityManager == null) return;

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                runOnUiThread(() -> tvOfflineBanner.setVisibility(View.GONE));
            }

            @Override
            public void onLost(Network network) {
                runOnUiThread(() -> tvOfflineBanner.setVisibility(View.VISIBLE));
            }
        };

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
        connectivityManager.registerNetworkCallback(request, networkCallback);

        Network active = connectivityManager.getActiveNetwork();
        NetworkCapabilities caps = active != null ? connectivityManager.getNetworkCapabilities(active) : null;
        boolean online = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        tvOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
    }
}
