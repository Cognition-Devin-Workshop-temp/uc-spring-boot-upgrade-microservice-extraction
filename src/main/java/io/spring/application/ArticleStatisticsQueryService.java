package io.spring.application;

import io.spring.application.data.ArticleStatisticsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.StatisticsReadService;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatisticsQueryService {
  private ArticleReadService articleReadService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private CommentReadService commentReadService;
  private StatisticsReadService statisticsReadService;

  public Optional<ArticleStatisticsData> getArticleStatistics(String slug) {
    return Optional.ofNullable(articleReadService.findBySlug(slug))
        .map(
            articleData -> {
              int favoriteCount =
                  articleFavoritesReadService.articleFavoriteCount(articleData.getId());
              int commentCount = commentReadService.findByArticleId(articleData.getId()).size();
              int daysSincePublished =
                  statisticsReadService.daysSincePublished(articleData.getId());
              int viewCount = statisticsReadService.viewCount(articleData.getId());
              return new ArticleStatisticsData(
                  viewCount, favoriteCount, commentCount, daysSincePublished);
            });
  }

  public List<TrendingArticleData> getTrendingArticles() {
    return statisticsReadService.findTrendingArticles();
  }
}
