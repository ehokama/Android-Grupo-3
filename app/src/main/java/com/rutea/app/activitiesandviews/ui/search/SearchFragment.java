package com.rutea.app.activitiesandviews.ui.search;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.data.models.dto.common.PageResponse;
import com.rutea.app.activitiesandviews.data.models.dto.ubication.UbicationDto;
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.data.network.UbicationApiService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.inject.Inject;

@AndroidEntryPoint
public class SearchFragment extends Fragment {

    @Inject ActivityApiService activityApiService;
    private List<UbicationDto> allUbications = new ArrayList<>();
    private EditText etMinPrice, etMaxPrice, etDate;
    private AutoCompleteTextView actvCountry;
    private AutoCompleteTextView actvCategory;
    private RecyclerView rvResults;
    private LinearLayout llEmptyState;
    private TextView tvEmptyMessage, tvResultCount;
    @Inject
    UbicationApiService ubicationApiService;
    private SearchResultsAdapter adapter;
    private final List<ActivityDto> results = new ArrayList<>();

    // Paginación
    private int currentPage = 0;
    private static final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    // Filtros activos (se quitó activeCity)
    private String activeCountry, activeCategory, activeDate;
    private Double activeMinPrice, activeMaxPrice;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        actvCountry = view.findViewById(R.id.actvCountry);
        etMinPrice    = view.findViewById(R.id.etMinPrice);
        etMaxPrice    = view.findViewById(R.id.etMaxPrice);
        etDate        = view.findViewById(R.id.etDate);
        actvCategory  = view.findViewById(R.id.actvCategory);
        rvResults     = view.findViewById(R.id.rvSearchResults);
        llEmptyState  = view.findViewById(R.id.llEmptyState);
        tvEmptyMessage= view.findViewById(R.id.tvEmptyMessage);
        tvResultCount = view.findViewById(R.id.tvResultCount);

        ubicationApiService.getUbications().enqueue(new Callback<List<UbicationDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<UbicationDto>> call,
                                   @NonNull Response<List<UbicationDto>> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                allUbications = response.body();

                List<String> countries = new ArrayList<>();
                for (UbicationDto u : allUbications) {
                    if (u.getCountry() != null && !countries.contains(u.getCountry()))
                        countries.add(u.getCountry());
                }

                // setThreshold ANTES del adapter
                actvCountry.setThreshold(1);
                ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_dropdown_item_1line, countries);
                actvCountry.setAdapter(countryAdapter);
            }
            @Override
            public void onFailure(@NonNull Call<List<UbicationDto>> call, @NonNull Throwable t) {}
        });

        // Se eliminó el Listener de actvCountry que filtraba las ciudades

        // RecyclerView + adapter
        adapter = new SearchResultsAdapter(results, activity -> {
            Bundle args = new Bundle();
            args.putLong("activityId", activity.getId() == null ? -1L : activity.getId());
            args.putString("nombre", activity.getTitle());
            Navigation.findNavController(view)
                    .navigate(R.id.action_search_to_detail, args);
        });
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rvResults.setLayoutManager(llm);
        rvResults.setAdapter(adapter);

        // Paginación por scroll
        rvResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy <= 0) return;
                int visible = llm.getChildCount();
                int total   = llm.getItemCount();
                int first   = llm.findFirstVisibleItemPosition();
                if (!isLoading && !isLastPage && (visible + first) >= total - 3) {
                    currentPage++;
                    fetchFiltered(false);
                }
            }
        });

        // Fecha con DatePicker
        etDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (dp, y, m, d) -> etDate.setText(String.format("%d-%02d-%02d", y, m + 1, d)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Aplicar filtros
        view.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> applyAndSearch());

        // Cargar categorías del backend
        loadCategories();

        // Mostrar estado inicial
        showEmpty("Usá los filtros para encontrar actividades");
    }

    private void loadCategories() {
        activityApiService.getCategories().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (!isAdded()) return;
                List<String> cats = new ArrayList<>();
                cats.add("Todas");
                if (response.isSuccessful() && response.body() != null)
                    cats.addAll(response.body());
                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_dropdown_item_1line, cats);
                actvCategory.setAdapter(catAdapter);
            }
            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                // categorías opcionales, si falla no bloqueamos
            }
        });
    }

    private void applyAndSearch() {
        String country = actvCountry.getText().toString().trim();

        activeCountry = country.isEmpty() ? null : country;

        activeCategory = actvCategory.getText().toString().trim();

        // Corrección del formato de fecha para Spring Boot
        String dateStr = etDate.getText().toString().trim();
        activeDate = dateStr.isEmpty() ? null : dateStr + "T00:00:00";

        String minStr = etMinPrice.getText().toString().trim();
        String maxStr = etMaxPrice.getText().toString().trim();
        activeMinPrice = minStr.isEmpty() ? null : Double.parseDouble(minStr);
        activeMaxPrice = maxStr.isEmpty() ? null : Double.parseDouble(maxStr);

        if (activeCategory.equals("Todas") || activeCategory.isEmpty()) activeCategory = null;

        currentPage = 0;
        isLastPage  = false;
        results.clear();
        adapter.notifyDataSetChanged();
        fetchFiltered(true);
    }

    private void fetchFiltered(boolean reset) {
        isLoading = true;
        activityApiService.getActivities(
                activeCountry, activeCategory, // Acá pasamos null en lugar de activeCity
                activeMinPrice, activeMaxPrice,
                activeDate, currentPage, PAGE_SIZE, null
        ).enqueue(pageCallback);
    }

    private final Callback<PageResponse<ActivityDto>> pageCallback = new Callback<PageResponse<ActivityDto>>() {
        @Override
        public void onResponse(@NonNull Call<PageResponse<ActivityDto>> call,
                               @NonNull Response<PageResponse<ActivityDto>> response) {
            if (!isAdded()) return;
            isLoading = false;

            if (!response.isSuccessful() || response.body() == null) {
                Toast.makeText(requireContext(), "Error al buscar", Toast.LENGTH_SHORT).show();
                return;
            }
            PageResponse<ActivityDto> page = response.body();
            List<ActivityDto> newItems = page.getContent();
            isLastPage = (currentPage + 1) >= page.getTotalPages();

            if (newItems == null || newItems.isEmpty()) {
                if (results.isEmpty()) showEmpty("No encontramos resultados para tu búsqueda");
                return;
            }

            int before = results.size();
            results.addAll(newItems);
            adapter.notifyItemRangeInserted(before, newItems.size());

            tvResultCount.setText(page.getTotalElements() + " actividades encontradas");
            showResults();
        }

        @Override
        public void onFailure(@NonNull Call<PageResponse<ActivityDto>> call, @NonNull Throwable t) {
            if (!isAdded()) return;
            isLoading = false;
            Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
        }
    };

    private void showResults() {
        rvResults.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
    }

    private void showEmpty(String msg) {
        rvResults.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(msg);
        tvResultCount.setText("");
    }
}