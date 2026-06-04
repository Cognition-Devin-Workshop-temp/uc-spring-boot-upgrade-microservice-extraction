package io.spring.api;

import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.TrendingArticleData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/stats")
@AllArgsConstructor
public class StatsApi {
  private ArticleStatsQueryService articleStatsQueryService;

  @GetMapping(path = "/trending")
  public ResponseEntity<?> getTrendingArticles() {
    List<TrendingArticleData> trending = articleStatsQueryService.getTrendingArticles(10, 7);
    Map<String, Object> response = new HashMap<>();
    response.put("articles", trending);
    return ResponseEntity.ok(response);
  }
}
