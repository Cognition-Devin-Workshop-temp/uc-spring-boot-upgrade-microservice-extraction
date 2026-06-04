package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import java.util.List;
import java.util.Optional;

public interface ArticleStatisticsQueryService {
  Optional<ArticleStatsData> getArticleStats(String slug);

  List<TrendingArticleData> getTrendingArticles();
}
