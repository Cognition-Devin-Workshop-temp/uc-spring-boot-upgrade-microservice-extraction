package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.TrendingArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleStatisticsReadService {
  int countArticleFavorites(@Param("articleId") String articleId);

  int countArticleComments(@Param("articleId") String articleId);

  int countArticleViews(@Param("articleId") String articleId);

  List<TrendingArticleData> findTrendingArticles();
}
