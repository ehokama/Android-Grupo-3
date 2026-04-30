package com.rutea.app.activitiesandviews.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.rutea.app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryFilterBottomSheet extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onFiltersApplied(String country, String dateFrom, String dateTo);
        void onFiltersCleared();
    }

    private static final String ARG_COUNTRY   = "country";
    private static final String ARG_DATE_FROM = "dateFrom";
    private static final String ARG_DATE_TO   = "dateTo";

    private FilterListener listener;
    private TextInputEditText etDateFrom, etDateTo;
    private AutoCompleteTextView etCountry;

    // Fechas seleccionadas en millis (nullable)
    private Long selectedFrom = null;
    private Long selectedTo   = null;

    private static final String ARG_COUNTRIES = "countries";

    public static HistoryFilterBottomSheet newInstance(
            String country, String dateFrom, String dateTo,
            ArrayList<String> countries) {             // ← nuevo parámetro
        HistoryFilterBottomSheet f = new HistoryFilterBottomSheet();
        Bundle b = new Bundle();
        b.putString(ARG_COUNTRY,   country);
        b.putString(ARG_DATE_FROM, dateFrom);
        b.putString(ARG_DATE_TO,   dateTo);
        b.putStringArrayList(ARG_COUNTRIES, countries); // ← guardar
        f.setArguments(b);
        return f;
    }

    public void setFilterListener(FilterListener l) { this.listener = l; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_history, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etCountry  = view.findViewById(R.id.etCountry);
        etDateFrom = view.findViewById(R.id.etDateFrom);
        etDateTo   = view.findViewById(R.id.etDateTo);

        // Configurar autocompletado
        Bundle args = getArguments();
        if (args != null) {
            ArrayList<String> countries = args.getStringArrayList(ARG_COUNTRIES);
            if (countries != null && !countries.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        countries);
                etCountry.setAdapter(adapter);
            }
            // Restaurar valor previo
            etCountry.setText(args.getString(ARG_COUNTRY, ""));
            etDateFrom.setText(args.getString(ARG_DATE_FROM, ""));
            etDateTo.setText(args.getString(ARG_DATE_TO, ""));
        }

        // Pickers de fecha
        etDateFrom.setOnClickListener(v -> showDatePicker(true));
        etDateTo.setOnClickListener(v  -> showDatePicker(false));

        view.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> {
            if (listener != null) {
                String country  = etCountry.getText() != null
                        ? etCountry.getText().toString().trim() : "";
                String dateFrom = etDateFrom.getText() != null
                        ? etDateFrom.getText().toString().trim() : "";
                String dateTo   = etDateTo.getText() != null
                        ? etDateTo.getText().toString().trim() : "";
                listener.onFiltersApplied(country, dateFrom, dateTo);
            }
            dismiss();
        });

        view.findViewById(R.id.btnClearFilters).setOnClickListener(v -> {
            if (listener != null) listener.onFiltersCleared();
            dismiss();
        });
    }

    private void showDatePicker(boolean isFrom) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isFrom ? "Fecha desde" : "Fecha hasta")
                .setSelection(isFrom
                        ? (selectedFrom != null ? selectedFrom : MaterialDatePicker.todayInUtcMilliseconds())
                        : (selectedTo   != null ? selectedTo   : MaterialDatePicker.todayInUtcMilliseconds()))
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            String formatted = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(selection));
            if (isFrom) {
                selectedFrom = selection;
                etDateFrom.setText(formatted);
            } else {
                selectedTo = selection;
                etDateTo.setText(formatted);
            }
        });

        picker.show(getChildFragmentManager(), "date_picker");
    }
}