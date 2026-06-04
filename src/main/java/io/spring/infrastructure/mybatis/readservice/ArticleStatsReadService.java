package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.data.TrendingArticleData;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleStatsReadService {
  Integer favoriteCountBySlug(@Param("slug") String slug);

  Integer commentCountBySlug(@Param("slug") String slug);

  String createdAtBySlug(@Param("slug") String slug);

  boolean articleExistsBySlug(@Param("slug") String slug);

  List<TrendingArticleData> findTrendingArticles(
      @Param("limit") int limit, @Param("days") int days);
}
