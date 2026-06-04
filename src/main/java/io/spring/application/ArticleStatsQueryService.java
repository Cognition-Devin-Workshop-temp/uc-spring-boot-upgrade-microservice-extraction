package io.spring.application;

import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleStatsData;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ArticleStatsQueryService {

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    // TODO: implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  public ArticleDataList getTrendingArticles() {
    // TODO: implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
