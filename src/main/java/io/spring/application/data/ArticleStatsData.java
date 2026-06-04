package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleStatsData {
  private String slug;
  private int viewCount;
  private int favoriteCount;
  private int commentCount;
  private long daysSincePublished;
}
