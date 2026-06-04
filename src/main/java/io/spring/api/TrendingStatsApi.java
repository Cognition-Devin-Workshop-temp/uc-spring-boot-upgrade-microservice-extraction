package io.spring.api;

import io.spring.application.ArticleStatisticsQueryService;
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
@RequestMapping(path = "/api/stats/trending")
@AllArgsConstructor
public class TrendingStatsApi {
  private ArticleStatisticsQueryService articleStatisticsQueryService;

  @GetMapping
  public ResponseEntity<?> getTrendingArticles() {
    List<TrendingArticleData> trendingArticles =
        articleStatisticsQueryService.getTrendingArticles();
    Map<String, Object> response = new HashMap<>();
    response.put("articles", trendingArticles);
    return ResponseEntity.ok(response);
  }
}
