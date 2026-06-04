package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleStatisticsQueryService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/articles/{slug}/stats")
@AllArgsConstructor
public class ArticleStatsApi {
  private ArticleStatisticsQueryService articleStatisticsQueryService;

  @GetMapping
  public ResponseEntity<?> getArticleStatistics(@PathVariable("slug") String slug) {
    return articleStatisticsQueryService
        .getArticleStatistics(slug)
        .map(
            statsData -> {
              Map<String, Object> response = new HashMap<>();
              response.put("stats", statsData);
              return ResponseEntity.ok(response);
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
