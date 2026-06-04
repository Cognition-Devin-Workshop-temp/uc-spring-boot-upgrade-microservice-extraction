package io.spring.application;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleStatsData;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleStatsQueryService {
  private static final int TRENDING_LIMIT = 10;
  private static final int TRENDING_DAYS = 7;
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

  private ArticleReadService articleReadService;
  private ArticleStatsReadService articleStatsReadService;

  public Optional<ArticleStatsData> getArticleStats(String slug) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return Optional.empty();
    }
    int favoriteCount = articleStatsReadService.countFavoritesByArticleId(articleData.getId());
    int commentCount = articleStatsReadService.countCommentsByArticleId(articleData.getId());
    int viewCount = articleStatsReadService.countViewsByArticleId(articleData.getId());
    long daysSincePublished =
        Days.daysBetween(articleData.getCreatedAt(), new DateTime()).getDays();

    return Optional.of(
        new ArticleStatsData(viewCount, favoriteCount, commentCount, daysSincePublished));
  }

  public ArticleDataList getTrendingArticles() {
    DateTime since = new DateTime().minusDays(TRENDING_DAYS);
    String sinceStr = DATE_FORMAT.print(since);
    List<String> articleIds =
        articleStatsReadService.findTrendingArticleIds(sinceStr, TRENDING_LIMIT);
    if (articleIds.isEmpty()) {
      return new ArticleDataList(new ArrayList<>(), 0);
    }
    List<ArticleData> articles = articleReadService.findArticles(articleIds);
    return new ArticleDataList(articles, articles.size());
  }
}
