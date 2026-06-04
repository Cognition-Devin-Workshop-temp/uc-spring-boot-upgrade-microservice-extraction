package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import java.util.List;

public interface ArticleStatisticsQueryService {
  ArticleStatsData getArticleStats(String articleId);

  List<TrendingArticleData> getTrendingArticles();
}
