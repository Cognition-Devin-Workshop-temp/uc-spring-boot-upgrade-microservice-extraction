package io.spring.infrastructure.mybatis.readservice;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleStatsReadService {
  int countFavoritesByArticleId(@Param("articleId") String articleId);

  int countCommentsByArticleId(@Param("articleId") String articleId);

  int countViewsByArticleId(@Param("articleId") String articleId);

  List<String> findTrendingArticleIds(@Param("since") String since, @Param("limit") int limit);
}
