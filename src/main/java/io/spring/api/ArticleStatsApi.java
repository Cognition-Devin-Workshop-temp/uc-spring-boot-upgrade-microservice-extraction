package io.spring.api;

import io.spring.application.ArticleStatisticsQueryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@AllArgsConstructor
public class ArticleStatsApi {
  private ArticleStatisticsQueryService articleStatisticsQueryService;
}
