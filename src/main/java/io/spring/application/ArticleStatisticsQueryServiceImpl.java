package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleStatisticsReadService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatisticsQueryServiceImpl implements ArticleStatisticsQueryService {
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

  private ArticleStatisticsReadService articleStatisticsReadService;

  @Override
  public Optional<ArticleStatsData> getArticleStats(String slug) {
    ArticleStatsData stats = articleStatisticsReadService.getArticleStatsBySlug(slug);
    return Optional.ofNullable(stats);
  }

  @Override
  public List<TrendingArticleData> getTrendingArticles() {
    String since = new DateTime().minusDays(7).toString(FORMATTER);
    List<TrendingArticleData> results = articleStatisticsReadService.getTrendingArticles(since);
    if (results == null) {
      return new ArrayList<>();
    }
    return results;
  }
}
