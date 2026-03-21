package com.example.xplorernow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.xplorernow.adapters.ActividadAdapter;
import com.example.xplorernow.api.RetrofitClient;
import com.example.xplorernow.databinding.FragmentFirstBinding;
import com.example.xplorernow.models.Actividad;
import com.example.xplorernow.models.Destino;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirstFragment extends Fragment implements ActividadAdapter.OnActividadClickListener {

    private FragmentFirstBinding binding;
    private ActividadAdapter adapter;
    private List<Destino> listaDestinos = new ArrayList<>();
    private final String[] categorias = {"Free Tour", "Visita Guiada", "Excursión", "Experiencia Gastronómica", "Aventura"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupFilters();
        fetchActividades(null, null); // Initial fetch
        fetchDestinos(); // For filter dropdown
    }

    private void setupRecyclerView() {
        adapter = new ActividadAdapter(this);
        binding.rvActividades.setAdapter(adapter);
    }

    private void setupFilters() {
        // Setup Categories Filter
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categorias);
        binding.actvTipoFilter.setAdapter(catAdapter);

        binding.actvTipoFilter.setOnItemClickListener((parent, view, position, id) -> {
            String seleccion = categorias[position];
            fetchActividades(null, seleccion); // Filter by category
        });

        // Setup Destino Filter (will be populated after fetchDestinos)
    }

    private void fetchActividades(Integer idDestino, String tipo) {
        RetrofitClient.getApiService().getActividades(idDestino, tipo, null, null, null)
                .enqueue(new Callback<List<Actividad>>() {
                    @Override
                    public void onResponse(Call<List<Actividad>> call, Response<List<Actividad>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setActividades(response.body());
                        } else {
                            Toast.makeText(getContext(), "Error al cargar actividades", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Actividad>> call, Throwable t) {
                        Log.e("API_ERROR", t.getMessage());
                        Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                        
                        // Mock data for testing if API is not ready
                        loadMockData();
                    }
                });
    }

    private void fetchDestinos() {
        RetrofitClient.getApiService().getDestinos().enqueue(new Callback<List<Destino>>() {
            @Override
            public void onResponse(Call<List<Destino>> call, Response<List<Destino>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaDestinos = response.body();
                    List<String> nombresDestinos = new ArrayList<>();
                    for (Destino d : listaDestinos) nombresDestinos.add(d.getNombre());

                    ArrayAdapter<String> destAdapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, nombresDestinos);
                    binding.actvDestinoFilter.setAdapter(destAdapter);

                    binding.actvDestinoFilter.setOnItemClickListener((parent, v, position, id) -> {
                        int idDest = listaDestinos.get(position).getId();
                        fetchActividades(idDest, null);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Destino>> call, Throwable t) {}
        });
    }

    private void loadMockData() {
        List<Actividad> mock = new ArrayList<>();
        mock.add(new Actividad(1, 101, "City Tour Histórico", "Recorrido por el centro", "Visita Guiada"));
        mock.add(new Actividad(2, 102, "Excursión a las Montañas", "Senderismo y vistas", "Aventura"));
        adapter.setActividades(mock);
    }

    @Override
    public void onActividadClick(Actividad actividad) {
        // Enviar ID al detalle
        Bundle bundle = new Bundle();
        bundle.putInt("actividadId", actividad.getId());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
