package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatisticsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.ArticleRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ArticleStatisticsApi {
  private ArticleRepository articleRepository;
  private ArticleStatisticsQueryService articleStatisticsQueryService;

  @GetMapping("/articles/{slug}/stats")
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              ArticleStatisticsData stats =
                  articleStatisticsQueryService.getArticleStatistics(article.getId());
              return ResponseEntity.ok(statsResponse(stats));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @GetMapping("/stats/trending")
  public ResponseEntity<?> getTrendingArticles() {
    List<TrendingArticleData> trending = articleStatisticsQueryService.getTrendingArticles();
    return ResponseEntity.ok(trendingResponse(trending));
  }

  private Map<String, Object> statsResponse(ArticleStatisticsData stats) {
    return new HashMap<String, Object>() {
      {
        put("stats", stats);
      }
    };
  }

  private Map<String, Object> trendingResponse(List<TrendingArticleData> articles) {
    return new HashMap<String, Object>() {
      {
        put("articles", articles);
      }
    };
  }
}
