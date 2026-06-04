package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatisticsData;
import io.spring.application.data.TrendingArticleData;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticleStatsApi.class, TrendingStatsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatisticsApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatisticsQueryService articleStatisticsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    String slug = "test-article";
    ArticleStatisticsData statsData = new ArticleStatisticsData(10, 5, 3, 7);

    when(articleStatisticsQueryService.getArticleStatistics(eq(slug)))
        .thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/api/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(10))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(7));
  }

  @Test
  public void should_return_404_when_article_not_found_for_stats() throws Exception {
    when(articleStatisticsQueryService.getArticleStatistics(eq("non-existent")))
        .thenReturn(Optional.empty());

    RestAssuredMockMvc.when()
        .get("/api/articles/{slug}/stats", "non-existent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    TrendingArticleData article1 = new TrendingArticleData("slug-1", "Title 1", "Desc 1", 20);
    TrendingArticleData article2 = new TrendingArticleData("slug-2", "Title 2", "Desc 2", 15);
    TrendingArticleData article3 = new TrendingArticleData("slug-3", "Title 3", "Desc 3", 10);
    List<TrendingArticleData> trendingArticles = Arrays.asList(article1, article2, article3);

    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(trendingArticles);

    RestAssuredMockMvc.when()
        .get("/api/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(3))
        .body("articles[0].slug", equalTo("slug-1"))
        .body("articles[0].title", equalTo("Title 1"))
        .body("articles[0].description", equalTo("Desc 1"))
        .body("articles[0].favoriteCount", equalTo(20))
        .body("articles[1].slug", equalTo("slug-2"))
        .body("articles[1].favoriteCount", equalTo(15))
        .body("articles[2].slug", equalTo("slug-3"))
        .body("articles[2].favoriteCount", equalTo(10));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(Arrays.asList());

    RestAssuredMockMvc.when()
        .get("/api/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0));
  }

  @Test
  public void should_return_at_most_10_trending_articles() throws Exception {
    TrendingArticleData[] articles = new TrendingArticleData[10];
    for (int i = 0; i < 10; i++) {
      articles[i] =
          new TrendingArticleData("slug-" + i, "Title " + i, "Desc " + i, 100 - i * 5);
    }
    List<TrendingArticleData> trendingArticles = Arrays.asList(articles);

    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(trendingArticles);

    RestAssuredMockMvc.when()
        .get("/api/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(10))
        .body("articles[0].favoriteCount", equalTo(100))
        .body("articles[9].favoriteCount", equalTo(55));
  }
}
