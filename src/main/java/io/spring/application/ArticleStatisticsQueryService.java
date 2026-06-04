package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleStatisticsReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatisticsQueryService {
  private ArticleReadService articleReadService;
  private ArticleStatisticsReadService articleStatisticsReadService;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    var articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    int favoriteCount = articleStatisticsReadService.countFavoritesByArticleId(articleData.getId());
    int commentCount = articleStatisticsReadService.countCommentsByArticleId(articleData.getId());
    long daysSincePublished =
        Days.daysBetween(articleData.getCreatedAt(), new DateTime()).getDays();
    return Optional.of(
        new ArticleStatsData(slug, 0, favoriteCount, commentCount, daysSincePublished));
  }

  public List<TrendingArticleData> getTrendingArticles() {
    DateTime sevenDaysAgo = new DateTime().minusDays(7);
    return articleStatisticsReadService.findTrendingArticles(sevenDaysAgo, 10);
  }
}
