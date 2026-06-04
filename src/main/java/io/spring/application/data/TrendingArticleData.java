package io.spring.application.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendingArticleData {
  private String id;
  private String slug;
  private String title;
  private String description;
  private int favoriteCount;
}
