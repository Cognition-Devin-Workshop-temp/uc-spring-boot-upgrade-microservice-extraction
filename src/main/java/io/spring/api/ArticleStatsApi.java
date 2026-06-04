package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatsData;
import io.spring.core.article.ArticleRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/stats")
@AllArgsConstructor
public class ArticleStatsApi {
  private ArticleRepository articleRepository;
  private ArticleStatisticsQueryService articleStatisticsQueryService;

  @GetMapping
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              ArticleStatsData stats =
                  articleStatisticsQueryService.getArticleStats(article.getId());
              return ResponseEntity.ok(statsResponse(stats));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> statsResponse(ArticleStatsData statsData) {
    return new HashMap<String, Object>() {
      {
        put("stats", statsData);
      }
    };
  }
}
