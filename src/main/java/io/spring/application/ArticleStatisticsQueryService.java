package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleStatisticsReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatisticsQueryService {
  private static final DateTimeFormatter SQLITE_FORMAT =
      DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

  private ArticleStatisticsReadService articleStatisticsReadService;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    ArticleStatsData stats = articleStatisticsReadService.getArticleStatsBySlug(slug);
    return Optional.ofNullable(stats);
  }

  public List<TrendingArticleData> getTrendingArticles() {
    String since = new DateTime().minusDays(7).toString(SQLITE_FORMAT);
    return articleStatisticsReadService.getTrendingArticles(since);
  }
}
