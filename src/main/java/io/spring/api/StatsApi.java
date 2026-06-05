package io.spring.api;

import io.spring.application.ArticleStatsQueryService;
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
    // TODO: implement in GREEN phase
    return ResponseEntity.status(501).build();
  }
}
