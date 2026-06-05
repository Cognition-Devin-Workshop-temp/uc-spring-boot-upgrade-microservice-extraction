package io.spring.application;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.ArticleRepository;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private ArticleRepository articleRepository;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private ArticleStatsReadService articleStatsReadService;

  public ArticleStatsData getArticleStats(String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              int favoriteCount = articleFavoritesReadService.articleFavoriteCount(article.getId());
              int commentCount = articleStatsReadService.countCommentsByArticleId(article.getId());
              int daysSincePublished =
                  Days.daysBetween(article.getCreatedAt(), new DateTime()).getDays();
              return new ArticleStatsData(0, favoriteCount, commentCount, daysSincePublished);
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  public List<TrendingArticleData> getTrendingArticles() {
    DateTime sevenDaysAgo = new DateTime().minusDays(7);
    return articleStatsReadService.findTrendingArticles(sevenDaysAgo, 10);
  }
}
