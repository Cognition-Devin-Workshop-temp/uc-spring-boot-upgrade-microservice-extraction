package io.spring.application;

import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.infrastructure.mybatis.readservice.ArticleStatsReadService;
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
public class ArticleStatsQueryServiceImpl implements ArticleStatsQueryService {

  private ArticleStatsReadService articleStatsReadService;

  @Override
  public Optional<ArticleStatsData> getArticleStats(String slug) {
    if (!articleStatsReadService.articleExistsBySlug(slug)) {
      return Optional.empty();
    }
    int favoriteCount = articleStatsReadService.favoriteCountBySlug(slug);
    int commentCount = articleStatsReadService.commentCountBySlug(slug);
    String createdAtStr = articleStatsReadService.createdAtBySlug(slug);

    long daysSincePublished = 0;
    if (createdAtStr != null) {
      DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
      DateTime createdAt = fmt.parseDateTime(createdAtStr);
      daysSincePublished =
          Days.daysBetween(createdAt.toLocalDate(), new DateTime().toLocalDate()).getDays();
    }

    ArticleStatsData statsData =
        new ArticleStatsData(slug, 0, favoriteCount, commentCount, daysSincePublished);
    return Optional.of(statsData);
  }

  @Override
  public List<TrendingArticleData> getTrendingArticles(int limit, int days) {
    return articleStatsReadService.findTrendingArticles(limit, days);
  }
}
