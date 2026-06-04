package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ArticleStatsData;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryServiceImpl implements ArticleStatsQueryService {
  private ArticleReadService articleReadService;
  private ArticleFavoritesReadService articleFavoritesReadService;
  private CommentReadService commentReadService;

  @Override
  public Optional<ArticleStatsData> getArticleStats(String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    int favoriteCount = articleFavoritesReadService.articleFavoriteCount(articleData.getId());
    int commentCount = commentReadService.findByArticleId(articleData.getId()).size();
    long daysSince = Days.daysBetween(articleData.getCreatedAt(), new DateTime()).getDays();
    return Optional.of(
        new ArticleStatsData(articleData.getSlug(), 0, favoriteCount, commentCount, daysSince));
  }

  @Override
  public List<ArticleData> getTrendingArticles() {
    List<String> recentArticleIds =
        articleReadService.queryArticles(null, null, null, new Page(0, 100));
    if (recentArticleIds.isEmpty()) {
      return new ArrayList<>();
    }
    List<ArticleData> articles = new ArrayList<>(articleReadService.findArticles(recentArticleIds));
    DateTime sevenDaysAgo = new DateTime().minusDays(7);
    articles.removeIf(a -> a.getCreatedAt().isBefore(sevenDaysAgo));
    if (articles.isEmpty()) {
      return new ArrayList<>();
    }
    List<ArticleFavoriteCount> favCounts =
        articleFavoritesReadService.articlesFavoriteCount(
            articles.stream().map(ArticleData::getId).collect(Collectors.toList()));
    Map<String, Integer> countMap = new HashMap<>();
    favCounts.forEach(fc -> countMap.put(fc.getId(), fc.getCount()));
    articles.forEach(a -> a.setFavoritesCount(countMap.getOrDefault(a.getId(), 0)));
    articles.sort((a, b) -> Integer.compare(b.getFavoritesCount(), a.getFavoritesCount()));
    return articles.subList(0, Math.min(10, articles.size()));
  }
}
