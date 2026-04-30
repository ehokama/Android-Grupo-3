package com.rutea.app.activitiesandviews.ui.history;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.reserve.ReserveDto;
import com.rutea.app.activitiesandviews.data.models.dto.review.CreateReviewRequest;
import com.rutea.app.activitiesandviews.data.models.dto.review.ReviewDto;
import com.rutea.app.activitiesandviews.data.network.ReviewApiService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class ReserveDetailFragment extends Fragment {

    private static final String ARG_RESERVE = "reserve";

    // Views
    private ImageView ivDetailImage;
    private Chip chipDetailState;
    private TextView tvDetailTitle, tvDetailReserveId, tvDetailDestino,
            tvDetailGuide, tvDetailActivityDate, tvDetailDuration,
            tvDetailPeople, tvDetailPrice, tvDisplayComment, tvCannotRate;
    private LinearLayout layoutReviewDisplay, layoutReviewForm;
    private LinearLayout starsActivityDisplay, starsGuideDisplay;
    private LinearLayout starsActivityInput, starsGuideInput;
    private TextInputEditText etComment;
    private MaterialButton btnSubmitReview;

    private int selectedActivityRating = 0;
    private int selectedGuideRating    = 0;

    @Inject ReviewApiService reviewApiService;

    public ReserveDetailFragment() {
        super(R.layout.fragment_reserve_detail);
    }

    public static Bundle buildArgs(ReserveDto reserve) {
        // Pasamos los campos que necesitamos como Bundle (ReserveDto no es Serializable/Parcelable)
        Bundle b = new Bundle();
        b.putLong("idReserve",       reserve.getIdReserve() != null ? reserve.getIdReserve() : -1);
        b.putString("title",         reserve.getActivityTitle());
        b.putString("state",         reserve.getState());
        b.putString("destino",       reserve.getCountry() + ", " + reserve.getCity());
        b.putString("guide",         reserve.getGuideName());
        b.putString("activityDate",  reserve.getReservationDate());
        b.putInt("duration",         reserve.getDuration() != null ? reserve.getDuration() : 0);
        b.putInt("people",           reserve.getNumberOfPeople() != null ? reserve.getNumberOfPeople() : 0);
        b.putDouble("price",         reserve.getTotalPrice() != null ? reserve.getTotalPrice() : 0.0);
        b.putLong("activityId",      reserve.getActivityId() != null ? reserve.getActivityId() : -1);
        b.putBoolean("canRate", "COMPLETED".equals(reserve.getState()));
        b.putBoolean("alreadyRated", reserve.isAlreadyRated());
        b.putInt("myActivityRating", reserve.getMyActivityRating() != null ? reserve.getMyActivityRating() : 0);
        b.putInt("myGuideRating",    reserve.getMyGuideRating() != null ? reserve.getMyGuideRating() : 0);
        b.putString("myComment",     reserve.getMyComment());

        // Imagen: primera URL si existe
        String imageUrl = null;
        if (reserve.getActivityImages() != null && !reserve.getActivityImages().isEmpty()) {
            Long imgId = reserve.getActivityImages().get(0).getIdImage();
            imageUrl = "http://10.0.2.2:8080/api/images/" + imgId + "/raw";
        }
        b.putString("imageUrl", imageUrl);

        ReserveDetailFragment f = new ReserveDetailFragment();
        f.setArguments(b);
        return b;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        ivDetailImage        = view.findViewById(R.id.ivDetailImage);
        chipDetailState      = view.findViewById(R.id.chipDetailState);
        tvDetailTitle        = view.findViewById(R.id.tvDetailTitle);
        tvDetailReserveId    = view.findViewById(R.id.tvDetailReserveId);
        tvDetailDestino      = view.findViewById(R.id.tvDetailDestino);
        tvDetailGuide        = view.findViewById(R.id.tvDetailGuide);
        tvDetailActivityDate = view.findViewById(R.id.tvDetailActivityDate);
        tvDetailDuration     = view.findViewById(R.id.tvDetailDuration);
        tvDetailPeople       = view.findViewById(R.id.tvDetailPeople);
        tvDetailPrice        = view.findViewById(R.id.tvDetailPrice);
        tvDisplayComment     = view.findViewById(R.id.tvDisplayComment);
        tvCannotRate         = view.findViewById(R.id.tvCannotRate);
        layoutReviewDisplay  = view.findViewById(R.id.layoutReviewDisplay);
        layoutReviewForm     = view.findViewById(R.id.layoutReviewForm);
        starsActivityDisplay = view.findViewById(R.id.starsActivityDisplay);
        starsGuideDisplay    = view.findViewById(R.id.starsGuideDisplay);
        starsActivityInput   = view.findViewById(R.id.starsActivityInput);
        starsGuideInput      = view.findViewById(R.id.starsGuideInput);
        etComment            = view.findViewById(R.id.etComment);
        btnSubmitReview      = view.findViewById(R.id.btnSubmitReview);

        Bundle args = getArguments();
        if (args == null) return;

        bindData(args);
        bindReviewSection(args);
    }

    // ─── Bind info general ────────────────────────────────────────────────────

    private void bindData(Bundle args) {
        String title   = args.getString("title", "Actividad");
        long idReserve = args.getLong("idReserve", -1);
        String state   = args.getString("state", "");
        String destino = args.getString("destino", "");
        String guide   = args.getString("guide", "");
        String date    = args.getString("activityDate", "");
        int duration   = args.getInt("duration", 0);
        int people     = args.getInt("people", 0);
        double price   = args.getDouble("price", 0.0);
        String imgUrl  = args.getString("imageUrl");

        tvDetailTitle.setText(title);
        tvDetailReserveId.setText("Reserva #" + idReserve);
        tvDetailDestino.setText(destino);
        tvDetailGuide.setText("Guía: " + guide);
        tvDetailActivityDate.setText("Fecha: " + formatDate(date));
        tvDetailDuration.setText("Duración: " + duration + " minutos");
        tvDetailPeople.setText(people + (people == 1 ? " persona" : " personas"));
        tvDetailPrice.setText(String.format(Locale.getDefault(), "$%.2f", price));

        bindStateChip(state);

        if (imgUrl != null) {
            Glide.with(this)
                    .load(imgUrl)
                    .placeholder(R.drawable.bg_hero_landscape)
                    .error(R.drawable.bg_hero_landscape)
                    .centerCrop()
                    .into(ivDetailImage);
        } else {
            ivDetailImage.setImageResource(R.drawable.bg_hero_landscape);
        }
    }

    // ─── Bind sección review ─────────────────────────────────────────────────

    private void bindReviewSection(Bundle args) {
        boolean alreadyRated = args.getBoolean("alreadyRated", false);
        int actRating        = args.getInt("myActivityRating", 0);
        int guideRating      = args.getInt("myGuideRating", 0);
        String comment       = args.getString("myComment", "");
        long activityId      = args.getLong("activityId", -1);
        String state         = args.getString("state", "");

        // ✅ 1. Ya calificó → mostrar review
        if (alreadyRated) {
            layoutReviewDisplay.setVisibility(View.VISIBLE);
            renderStarsReadOnly(starsActivityDisplay, actRating);
            renderStarsReadOnly(starsGuideDisplay, guideRating);

            if (comment != null && !comment.trim().isEmpty()) {
                tvDisplayComment.setText("\"" + comment.trim() + "\"");
                tvDisplayComment.setVisibility(View.VISIBLE);
            }

            // ❌ 2. Cancelada → NO puede calificar
        } else if ("CANCELLED".equals(state)) {
            tvCannotRate.setText("No podrás dejar reseña. \uD83D\uDE22");
            tvCannotRate.setVisibility(View.VISIBLE);

            // ⭐ 3. Completada → puede calificar
        } else if ("COMPLETED".equals(state)) {
            tvCannotRate.setText("Dejá una reseña");
            tvCannotRate.setVisibility(View.VISIBLE);

            tvCannotRate.setOnClickListener(v -> {
                tvCannotRate.setVisibility(View.GONE);
                layoutReviewForm.setVisibility(View.VISIBLE);

                renderStarsInteractive(starsActivityInput, rating -> selectedActivityRating = rating);
                renderStarsInteractive(starsGuideInput,    rating -> selectedGuideRating    = rating);

                btnSubmitReview.setOnClickListener(btn -> submitReview(activityId));
            });

            // ⏳ 4. Otros estados (CONFIRMED / PENDING)
        } else {
            tvCannotRate.setText("Podrás dejar una reseña luego de haber finalizado la actividad");
            tvCannotRate.setVisibility(View.VISIBLE);
        }
    }
    // ─── Estrellas solo lectura ───────────────────────────────────────────────

    private void renderStarsReadOnly(LinearLayout container, int rating) {
        container.removeAllViews();
        for (int i = 1; i <= 5; i++) {
            ImageView star = buildStarView();
            star.setImageResource(i <= rating
                    ? R.drawable.ic_star_filled
                    : R.drawable.ic_star_outline);
            star.setColorFilter(ContextCompat.getColor(requireContext(),
                    i <= rating ? R.color.star_yellow : R.color.star_grey));
            container.addView(star);
        }
    }

    // ─── Estrellas interactivas ───────────────────────────────────────────────

    interface RatingCallback { void onRate(int rating); }

    private void renderStarsInteractive(LinearLayout container, RatingCallback cb) {
        container.removeAllViews();
        ImageView[] stars = new ImageView[5];

        for (int i = 0; i < 5; i++) {
            ImageView star = buildStarView();
            star.setImageResource(R.drawable.ic_star_outline);
            star.setColorFilter(ContextCompat.getColor(requireContext(), R.color.star_grey));
            stars[i] = star;
            container.addView(star);

            final int pos = i + 1; // 1-based
            star.setOnClickListener(v -> {
                cb.onRate(pos);
                updateInteractiveStars(stars, pos);
            });
        }
    }

    private void updateInteractiveStars(ImageView[] stars, int selected) {
        for (int i = 0; i < stars.length; i++) {
            boolean filled = i < selected;
            stars[i].setImageResource(filled ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            stars[i].setColorFilter(ContextCompat.getColor(requireContext(),
                    filled ? R.color.star_yellow : R.color.star_grey));
        }
    }

    private ImageView buildStarView() {
        ImageView iv = new ImageView(requireContext());
        int size = (int) (36 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMarginEnd((int) (2 * getResources().getDisplayMetrics().density));
        iv.setLayoutParams(lp);
        return iv;
    }

    // ─── Enviar review ────────────────────────────────────────────────────────

    private void submitReview(long activityId) {
        if (selectedActivityRating == 0) {
            Toast.makeText(requireContext(), "Seleccioná una calificación para la actividad", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedGuideRating == 0) {
            Toast.makeText(requireContext(), "Seleccioná una calificación para el guía", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = etComment.getText() != null ? etComment.getText().toString().trim() : "";
        btnSubmitReview.setEnabled(false);

        CreateReviewRequest req = new CreateReviewRequest(
                selectedActivityRating, selectedGuideRating, comment, activityId);

        reviewApiService.createReview(req).enqueue(new Callback<ReviewDto>() {
            @Override
            public void onResponse(@NonNull Call<ReviewDto> call,
                                   @NonNull Response<ReviewDto> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "¡Calificación enviada!", Toast.LENGTH_SHORT).show();
                    showSavedReview(response.body());
                } else {
                    Toast.makeText(requireContext(), "No se pudo guardar la calificación", Toast.LENGTH_SHORT).show();
                    btnSubmitReview.setEnabled(true);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReviewDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show();
                btnSubmitReview.setEnabled(true);
            }
        });
    }

    /** Reemplaza el formulario por la review recién guardada. */
    private void showSavedReview(ReviewDto dto) {
        layoutReviewForm.setVisibility(View.GONE);
        layoutReviewDisplay.setVisibility(View.VISIBLE);
        renderStarsReadOnly(starsActivityDisplay, dto.getRating() != null ? dto.getRating() : 0);
        renderStarsReadOnly(starsGuideDisplay,    dto.getGuideRating() != null ? dto.getGuideRating() : 0);
        if (dto.getComment() != null && !dto.getComment().trim().isEmpty()) {
            tvDisplayComment.setText("\"" + dto.getComment().trim() + "\"");
            tvDisplayComment.setVisibility(View.VISIBLE);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void bindStateChip(String state) {
        if (state == null) state = "";
        int colorRes;
        String label;
        switch (state) {
            case "COMPLETED": label = "Completada"; colorRes = R.color.state_completed; break;
            case "CONFIRMED": label = "Confirmada"; colorRes = R.color.state_confirmed; break;
            case "PENDING":   label = "Pendiente";  colorRes = R.color.state_pending;   break;
            case "CANCELLED": label = "Cancelada";  colorRes = R.color.state_cancelled; break;
            default:          label = state;         colorRes = R.color.state_pending;
        }
        chipDetailState.setText(label);
        chipDetailState.setChipBackgroundColor(
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), colorRes)));
    }

    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        try {
            return LocalDateTime.parse(raw)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault()));
        } catch (Exception e) {
            return raw.replace("T", " ");
        }
    }
}