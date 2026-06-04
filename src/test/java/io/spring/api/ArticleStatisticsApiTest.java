package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatsData;
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

@WebMvcTest({ArticleStatisticsApi.class})
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
  public void should_get_article_stats_by_slug() throws Exception {
    String slug = "test-article";
    ArticleStatsData statsData = new ArticleStatsData(slug, 150, 42, 7, 30);

    when(articleStatisticsQueryService.getArticleStats(eq(slug)))
        .thenReturn(Optional.of(statsData));

    given()
        .when()
        .get("/api/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(150))
        .body("stats.favoriteCount", equalTo(42))
        .body("stats.commentCount", equalTo(7))
        .body("stats.daysSincePublished", equalTo(30));
  }

  @Test
  public void should_return_404_when_article_not_found_for_stats() throws Exception {
    when(articleStatisticsQueryService.getArticleStats(eq("non-existent")))
        .thenReturn(Optional.empty());

    given().when().get("/api/articles/{slug}/stats", "non-existent").then().statusCode(404);
  }

  @Test
  public void should_get_trending_articles() throws Exception {
    TrendingArticleData trending1 =
        new TrendingArticleData("trending-article-1", "Trending Article 1", "Description 1", 100);
    TrendingArticleData trending2 =
        new TrendingArticleData("trending-article-2", "Trending Article 2", "Description 2", 85);
    TrendingArticleData trending3 =
        new TrendingArticleData("trending-article-3", "Trending Article 3", "Description 3", 60);

    List<TrendingArticleData> trendingList = Arrays.asList(trending1, trending2, trending3);
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(trendingList);

    given()
        .when()
        .get("/api/stats/trending")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(3))
        .body("articles[0].slug", equalTo("trending-article-1"))
        .body("articles[0].title", equalTo("Trending Article 1"))
        .body("articles[0].description", equalTo("Description 1"))
        .body("articles[0].favoriteCount", equalTo(100))
        .body("articles[1].slug", equalTo("trending-article-2"))
        .body("articles[1].favoriteCount", equalTo(85))
        .body("articles[2].slug", equalTo("trending-article-3"))
        .body("articles[2].favoriteCount", equalTo(60));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(Arrays.asList());

    given()
        .when()
        .get("/api/stats/trending")
        .then()
        .statusCode(200)
        .body("articles.size()", equalTo(0));
  }
}
