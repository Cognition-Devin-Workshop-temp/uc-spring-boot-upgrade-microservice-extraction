package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatisticsQueryService {

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  public List<TrendingArticleData> getTrendingArticles() {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
