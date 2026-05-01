package com.rutea.app.activitiesandviews.ui.news;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.rutea.app.R;
import com.rutea.app.activitiesandviews.data.models.dto.news.NewsDto;
import com.rutea.app.activitiesandviews.data.network.NewsApiService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class NewsFragment extends Fragment {

    @Inject
    NewsApiService newsApiService;

    private NewsAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<NewsDto> allNews = new ArrayList<>();

    public NewsFragment() {
        super(R.layout.fragment_news);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack()
        );

        RecyclerView rv = view.findViewById(R.id.rvNews);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new NewsAdapter(news -> navigateToDetail(news));
        rv.setAdapter(adapter);

        setupChips(view);
        loadNews();
    }

    private void setupChips(View view) {
        Chip chipAll      = view.findViewById(R.id.chipAll);
        Chip chipNoticias = view.findViewById(R.id.chipNoticias);
        Chip chipOfertas  = view.findViewById(R.id.chipOfertas);
        Chip chipDestinos = view.findViewById(R.id.chipDestinos);

        chipAll.setOnClickListener(v -> filterNews(null));
        chipNoticias.setOnClickListener(v -> filterNews("NOTICIA"));
        chipOfertas.setOnClickListener(v -> filterNews("OFERTA"));
        chipDestinos.setOnClickListener(v -> filterNews("DESTINO"));
    }

    private void filterNews(String type) {
        if (allNews.isEmpty()) return;
        if (type == null) {
            adapter.updateList(allNews);
            return;
        }
        List<NewsDto> filtered = new ArrayList<>();
        for (NewsDto n : allNews) {
            if (type.equalsIgnoreCase(n.getType())) filtered.add(n);
        }
        adapter.updateList(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadNews() {
        progressBar.setVisibility(View.VISIBLE);
        newsApiService.getNews().enqueue(new Callback<List<NewsDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<NewsDto>> call,
                                   @NonNull Response<List<NewsDto>> response) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    allNews = response.body();
                    adapter.updateList(allNews);
                    tvEmpty.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NewsDto>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void navigateToDetail(NewsDto news) {
        Bundle args = new Bundle();
        args.putLong("newsId", news.getId() != null ? news.getId() : -1L);
        args.putString("newsTitle", news.getTitle());
        args.putString("newsDesc", news.getDescription());
        args.putString("newsContent", news.getContent());
        args.putString("newsType", news.getType());
        args.putString("newsImageUrl", NewsAdapter.resolveImageUrl(news));
        args.putString("newsDiscount", news.getDiscount());
        if (news.getActivityId() != null) args.putLong("activityId", news.getActivityId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_news_to_news_detail, args);
    }
}
