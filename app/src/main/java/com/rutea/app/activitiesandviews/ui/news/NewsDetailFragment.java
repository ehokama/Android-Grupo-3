package com.rutea.app.activitiesandviews.ui.news;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.activity.ActivityDto;
import com.rutea.app.activitiesandviews.data.models.dto.news.NewsDto;
import com.rutea.app.activitiesandviews.data.network.ActivityApiService;
import com.rutea.app.activitiesandviews.data.network.NewsApiService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class NewsDetailFragment extends Fragment {

    @Inject
    NewsApiService newsApiService;

    @Inject
    ActivityApiService activityApiService;

    private ActivityDto activityForReservation;
    private long activityForReservationId = -1L;

    public NewsDetailFragment() {
        super(R.layout.fragment_news_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack()
        );

        Bundle args = getArguments();
        if (args == null) return;

        // Populate with passed args immediately for instant display
        bindArgs(view, args);

        long activityId = args.getLong("activityId", -1L);
        ensureActivityLoaded(activityId);

        // Then fetch fresh full detail from API if we have an ID
        long newsId = args.getLong("newsId", -1L);
        if (newsId > 0) {
            newsApiService.getNewsById(newsId).enqueue(new Callback<NewsDto>() {
                @Override
                public void onResponse(@NonNull Call<NewsDto> call,
                                       @NonNull Response<NewsDto> response) {
                    if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                    bindDto(view, response.body());
                }

                @Override
                public void onFailure(@NonNull Call<NewsDto> call, @NonNull Throwable t) { }
            });
        }
    }

    private void addDiscountArgs(Bundle bundle, String discount) {
        if (discount == null || discount.isEmpty()) return;
        String lower = discount.toLowerCase();
        Matcher m = Pattern.compile("\\d+").matcher(discount);
        if (!m.find()) return;
        int pct = Integer.parseInt(m.group());
        if (lower.contains("2da persona") || lower.contains("segunda")) {
            bundle.putString("discountMode", "SECOND_PERSON");
            bundle.putInt("discountPercent", pct);
        } else if (lower.contains("%")) {
            bundle.putString("discountMode", "ALL");
            bundle.putInt("discountPercent", pct);
        }
    }

    private void bindArgs(View view, Bundle args) {
        String title    = args.getString("newsTitle", "");
        String desc     = args.getString("newsDesc", "");
        String content  = args.getString("newsContent", "");
        String type     = args.getString("newsType", "");
        String imageUrl = args.getString("newsImageUrl", "");
        String discount = args.getString("newsDiscount", "");
        boolean hasActivity = args.containsKey("activityId");
        long activityId = args.getLong("activityId", -1L);

        setViews(view, title, desc, content, type, imageUrl, discount, hasActivity ? activityId : -1L);
    }

    private void bindDto(View view, NewsDto dto) {
        long activityId = dto.getActivityId() != null ? dto.getActivityId() : -1L;
        setViews(view,
                dto.getTitle(),
                dto.getDescription(),
                dto.getContent(),
                dto.getType(),
                NewsAdapter.resolveImageUrl(dto),
                dto.getDiscount(),
                activityId);
    }

    private void setViews(View view, String title, String desc, String content,
                          String type, String imageUrl, String discount, long activityId) {
        ImageView ivImage = view.findViewById(R.id.ivNewsDetailImage);
        TextView tvType    = view.findViewById(R.id.tvNewsDetailType);
        TextView tvTitle   = view.findViewById(R.id.tvNewsDetailTitle);
        TextView tvDesc    = view.findViewById(R.id.tvNewsDetailDesc);
        TextView tvContent = view.findViewById(R.id.tvNewsDetailContent);
        TextView tvDiscount = view.findViewById(R.id.tvNewsDetailDiscount);
        MaterialButton btnActivity = view.findViewById(R.id.btnVerActividad);

        if (title != null)  tvTitle.setText(title);
        if (desc != null)   tvDesc.setText(desc);

        if (content != null && !content.isEmpty()) {
            tvContent.setText(content);
            tvContent.setVisibility(View.VISIBLE);
        }

        if (discount != null && !discount.isEmpty()) {
            tvDiscount.setText("🏷 Descuento: " + discount);
            tvDiscount.setVisibility(View.VISIBLE);
        }

        if (type != null && !type.isEmpty()) {
            tvType.setText(type);
            tvType.setBackgroundColor(NewsAdapter.typeColor(type));
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(ivImage.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_hero_landscape)
                    .error(R.drawable.bg_hero_landscape)
                    .centerCrop()
                    .into(ivImage);
        }

        if (activityId > 0) {
            ensureActivityLoaded(activityId);
            btnActivity.setVisibility(View.VISIBLE);
            final long finalId = activityId;
            final String finalDiscount = discount;
            btnActivity.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putLong("activityId", finalId);
                String activityName = activityForReservation != null && activityForReservation.getTitle() != null
                        ? activityForReservation.getTitle()
                        : (title != null && !title.isEmpty() ? title : "Actividad");
                bundle.putString("nombre", activityName);
                if (activityForReservation != null && activityForReservation.getPrice() != null) {
                    bundle.putDouble("activityPrice", activityForReservation.getPrice());
                }
                addDiscountArgs(bundle, finalDiscount);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_news_detail_to_reservation, bundle);
            });
        } else {
            btnActivity.setVisibility(View.GONE);
        }
    }

    private void ensureActivityLoaded(long activityId) {
        if (activityId <= 0 || activityId == activityForReservationId) return;
        activityForReservationId = activityId;
        activityApiService.getActivityById(activityId).enqueue(new Callback<ActivityDto>() {
            @Override
            public void onResponse(@NonNull Call<ActivityDto> call,
                                   @NonNull Response<ActivityDto> response) {
                if (!isAdded() || !response.isSuccessful() || response.body() == null) return;
                activityForReservation = response.body();
            }

            @Override
            public void onFailure(@NonNull Call<ActivityDto> call, @NonNull Throwable t) { }
        });
    }
}
