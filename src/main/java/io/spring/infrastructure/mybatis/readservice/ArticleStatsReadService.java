package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.TrendingArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.joda.time.DateTime;

@Mapper
public interface ArticleStatsReadService {
  int countCommentsByArticleId(@Param("articleId") String articleId);

  List<TrendingArticleData> findTrendingArticles(
      @Param("since") DateTime since, @Param("limit") int limit);
}
