package io.spring.api;

import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleDataList;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/stats")
@AllArgsConstructor
public class StatsApi {
  private ArticleStatsQueryService articleStatsQueryService;

  @GetMapping(path = "/trending")
  public ResponseEntity<?> getTrendingArticles() {
    ArticleDataList trendingArticles = articleStatsQueryService.getTrendingArticles();
    return ResponseEntity.ok(trendingResponse(trendingArticles));
  }

  private Map<String, Object> trendingResponse(ArticleDataList articleDataList) {
    return new HashMap<String, Object>() {
      {
        put("articles", articleDataList.getArticleDatas());
        put("articlesCount", articleDataList.getCount());
      }
    };
  }
}
