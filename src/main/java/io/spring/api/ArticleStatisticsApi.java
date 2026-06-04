package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.TrendingArticleData;
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
  private ArticleStatisticsQueryService articleStatisticsQueryService;

  @GetMapping("/articles/{slug}/stats")
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    return articleStatisticsQueryService
        .getArticleStats(slug)
        .map(
            statsData -> {
              Map<String, Object> response = new HashMap<>();
              response.put("stats", statsData);
              return ResponseEntity.ok(response);
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @GetMapping("/stats/trending")
  public ResponseEntity<?> getTrendingArticles() {
    List<TrendingArticleData> trending = articleStatisticsQueryService.getTrendingArticles();
    Map<String, Object> response = new HashMap<>();
    response.put("articles", trending);
    return ResponseEntity.ok(response);
  }
}
