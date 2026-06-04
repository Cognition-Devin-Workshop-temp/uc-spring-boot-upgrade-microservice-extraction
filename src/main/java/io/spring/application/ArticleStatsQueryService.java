package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private ArticleReadService articleReadService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private ArticleStatsReadService articleStatsReadService;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    int favoriteCount = articleFavoritesReadService.articleFavoriteCount(articleData.getId());
    int commentCount = articleStatsReadService.commentCountByArticleId(articleData.getId());
    long daysSincePublished =
        Days.daysBetween(articleData.getCreatedAt(), new DateTime()).getDays();
    return Optional.of(
        new ArticleStatsData(slug, 0, favoriteCount, commentCount, daysSincePublished));
  }

  public List<TrendingArticleData> getTrendingArticles() {
    return articleStatsReadService.findTrendingArticles();
  }
}
