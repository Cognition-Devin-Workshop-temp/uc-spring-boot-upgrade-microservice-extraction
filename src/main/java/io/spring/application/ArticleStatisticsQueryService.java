package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ArticleStatisticsQueryService {

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    // TODO: implement
    return Optional.empty();
  }

  public List<TrendingArticleData> getTrendingArticles() {
    // TODO: implement
    return Collections.emptyList();
  }
}
