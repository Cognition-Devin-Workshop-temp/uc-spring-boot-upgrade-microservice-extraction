package io.spring.application;

import io.spring.application.data.ArticleStatisticsData;
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
public class ArticleStatisticsQueryService {
  private ArticleStatisticsReadService articleStatisticsReadService;
  private ArticleReadService articleReadService;

  public ArticleStatisticsData getArticleStatistics(String articleId) {
    int viewCount = articleStatisticsReadService.countArticleViews(articleId);
    int favoriteCount = articleStatisticsReadService.countArticleFavorites(articleId);
    int commentCount = articleStatisticsReadService.countArticleComments(articleId);

    var articleData = articleReadService.findById(articleId);
    int daysSincePublished = 0;
    if (articleData != null && articleData.getCreatedAt() != null) {
      daysSincePublished =
          Days.daysBetween(articleData.getCreatedAt().toLocalDate(), DateTime.now().toLocalDate())
              .getDays();
    }

    return new ArticleStatisticsData(viewCount, favoriteCount, commentCount, daysSincePublished);
  }

  public List<TrendingArticleData> getTrendingArticles() {
    return articleStatisticsReadService.findTrendingArticles();
  }
}
