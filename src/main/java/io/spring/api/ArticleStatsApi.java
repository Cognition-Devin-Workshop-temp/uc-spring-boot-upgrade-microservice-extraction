package io.spring.api;

import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleStatsData;
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
  private ArticleStatsQueryService articleStatsQueryService;

  @GetMapping
  public ResponseEntity<?> getArticleStats(@PathVariable("slug") String slug) {
    ArticleStatsData stats = articleStatsQueryService.getArticleStats(slug);
    return ResponseEntity.ok(statsResponse(stats));
  }

  private Map<String, Object> statsResponse(ArticleStatsData statsData) {
    return new HashMap<String, Object>() {
      {
        put("stats", statsData);
      }
    };
  }
}
