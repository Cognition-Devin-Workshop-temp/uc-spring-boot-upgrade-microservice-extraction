package io.spring.api;

import io.spring.application.ArticleStatsQueryService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/stats")
@AllArgsConstructor
public class TrendingArticlesApi {
  private ArticleStatsQueryService articleStatsQueryService;

  @GetMapping(path = "trending")
  public ResponseEntity<?> getTrendingArticles() {
    Map<String, Object> response = new HashMap<>();
    response.put("trendingArticles", articleStatsQueryService.getTrendingArticles());
    return ResponseEntity.ok(response);
  }
}
