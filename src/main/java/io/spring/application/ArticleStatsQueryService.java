package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ArticleStatsQueryService {

  public ArticleStatsData getArticleStats(String slug) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  public List<TrendingArticleData> getTrendingArticles() {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
