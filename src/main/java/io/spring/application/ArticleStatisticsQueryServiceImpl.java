package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleStatisticsReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatisticsQueryServiceImpl implements ArticleStatisticsQueryService {
  private ArticleStatisticsReadService articleStatisticsReadService;
  private ArticleReadService articleReadService;

  @Override
  public ArticleStatsData getArticleStats(String articleId) {
    var articleData = articleReadService.findById(articleId);
    int favoriteCount = articleStatisticsReadService.countFavoritesByArticleId(articleId);
    int commentCount = articleStatisticsReadService.countCommentsByArticleId(articleId);
    int daysSincePublished = 0;
    if (articleData != null && articleData.getCreatedAt() != null) {
      daysSincePublished =
          Days.daysBetween(articleData.getCreatedAt().toLocalDate(), DateTime.now().toLocalDate())
              .getDays();
    }
    return new ArticleStatsData(
        articleData != null ? articleData.getSlug() : "",
        0,
        favoriteCount,
        commentCount,
        daysSincePublished);
  }

  @Override
  public List<TrendingArticleData> getTrendingArticles() {
    return articleStatisticsReadService.findTrendingArticles(7, 10);
  }
}
