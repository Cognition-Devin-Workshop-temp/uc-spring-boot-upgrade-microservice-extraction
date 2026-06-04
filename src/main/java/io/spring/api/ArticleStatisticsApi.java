package io.spring.api;

import io.spring.application.ArticleStatisticsQueryService;
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
    // TODO: implement - return article statistics
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/stats/trending")
  public ResponseEntity<?> getTrendingArticles() {
    // TODO: implement - return trending articles
    return ResponseEntity.ok().build();
  }
}
