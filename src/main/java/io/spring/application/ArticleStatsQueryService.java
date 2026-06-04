package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleStatsData;
import java.util.List;
import java.util.Optional;

public interface ArticleStatsQueryService {
  Optional<ArticleStatsData> getArticleStats(String slug);

  List<ArticleData> getTrendingArticles();
}
