package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.TrendingArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StatisticsReadService {
  int daysSincePublished(@Param("articleId") String articleId);

  int viewCount(@Param("articleId") String articleId);

  List<TrendingArticleData> findTrendingArticles();
}
