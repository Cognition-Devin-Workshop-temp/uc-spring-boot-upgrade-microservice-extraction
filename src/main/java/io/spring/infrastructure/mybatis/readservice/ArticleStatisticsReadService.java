package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.TrendingArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleStatisticsReadService {
  int countCommentsByArticleId(@Param("articleId") String articleId);

  int countFavoritesByArticleId(@Param("articleId") String articleId);

  String getArticleCreatedAt(@Param("articleId") String articleId);

  List<TrendingArticleData> findTrendingArticles(
      @Param("days") int days, @Param("limit") int limit);
}
