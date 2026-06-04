package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

@WebMvcTest(ArticleStatsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatsApiTest extends TestWithCurrentUser {
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
    ArticleStatsData stats = new ArticleStatsData(slug, "Test Article", 150, 10, 5, 3);

    when(articleStatisticsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(stats));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("articleStats.slug", equalTo(slug))
        .body("articleStats.title", equalTo("Test Article"))
        .body("articleStats.viewCount", equalTo(150))
        .body("articleStats.favoriteCount", equalTo(10))
        .body("articleStats.commentCount", equalTo(5))
        .body("articleStats.daysSincePublished", equalTo(3));
  }

  @Test
  public void should_return_404_when_article_not_found_for_stats() throws Exception {
    when(articleStatisticsQueryService.getArticleStats(eq("nonexistent-slug")))
        .thenReturn(Optional.empty());

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", "nonexistent-slug")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_get_article_stats_with_zero_counts() throws Exception {
    String slug = "brand-new-article";
    ArticleStatsData stats = new ArticleStatsData(slug, "Brand New Article", 0, 0, 0, 0);

    when(articleStatisticsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(stats));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("articleStats.viewCount", equalTo(0))
        .body("articleStats.favoriteCount", equalTo(0))
        .body("articleStats.commentCount", equalTo(0))
        .body("articleStats.daysSincePublished", equalTo(0));
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    TrendingArticleData trending1 =
        new TrendingArticleData("popular-article", "Popular Article", "Desc 1", "alice", 50);
    TrendingArticleData trending2 =
        new TrendingArticleData("hot-topic", "Hot Topic", "Desc 2", "bob", 30);

    when(articleStatisticsQueryService.getTrendingArticles())
        .thenReturn(Arrays.asList(trending1, trending2));

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("trendingArticles", hasSize(2))
        .body("trendingArticles[0].slug", equalTo("popular-article"))
        .body("trendingArticles[0].title", equalTo("Popular Article"))
        .body("trendingArticles[0].favoriteCount", equalTo(50))
        .body("trendingArticles[0].authorUsername", equalTo("alice"))
        .body("trendingArticles[1].slug", equalTo("hot-topic"))
        .body("trendingArticles[1].favoriteCount", equalTo(30));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("trendingArticles", hasSize(0));
  }

  @Test
  public void should_get_trending_articles_sorted_by_favorite_count() throws Exception {
    TrendingArticleData first =
        new TrendingArticleData("most-popular", "Most Popular", "Desc", "user1", 100);
    TrendingArticleData second =
        new TrendingArticleData("second-popular", "Second Popular", "Desc", "user2", 75);
    TrendingArticleData third =
        new TrendingArticleData("third-popular", "Third Popular", "Desc", "user3", 50);

    when(articleStatisticsQueryService.getTrendingArticles())
        .thenReturn(Arrays.asList(first, second, third));

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("trendingArticles", hasSize(3))
        .body("trendingArticles[0].favoriteCount", equalTo(100))
        .body("trendingArticles[1].favoriteCount", equalTo(75))
        .body("trendingArticles[2].favoriteCount", equalTo(50));
  }

  // --- Negative test cases ---

  @Test
  public void should_return_404_for_stats_with_nonexistent_numeric_slug() throws Exception {
    String slug = "99999-nonexistent-article";
    when(articleStatisticsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/stats", slug).then().statusCode(404);
  }

  @Test
  public void should_return_401_when_post_to_article_stats_without_auth() throws Exception {
    given()
        .contentType("application/json")
        .body("{}")
        .when()
        .post("/articles/{slug}/stats", "some-article")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_return_401_when_put_to_article_stats_without_auth() throws Exception {
    given()
        .contentType("application/json")
        .body("{}")
        .when()
        .put("/articles/{slug}/stats", "some-article")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_return_401_when_delete_to_article_stats_without_auth() throws Exception {
    given().when().delete("/articles/{slug}/stats", "some-article").then().statusCode(401);
  }

  @Test
  public void should_return_401_when_post_to_trending_without_auth() throws Exception {
    given()
        .contentType("application/json")
        .body("{}")
        .when()
        .post("/stats/trending")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_return_401_when_put_to_trending_without_auth() throws Exception {
    given()
        .contentType("application/json")
        .body("{}")
        .when()
        .put("/stats/trending")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_return_401_when_delete_to_trending_without_auth() throws Exception {
    given().when().delete("/stats/trending").then().statusCode(401);
  }

  @Test
  public void should_get_article_stats_accessible_without_authentication() throws Exception {
    String slug = "public-article";
    ArticleStatsData stats = new ArticleStatsData(slug, "Public Article", 0, 5, 2, 10);

    when(articleStatisticsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(stats));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("articleStats.slug", equalTo(slug));
  }

  @Test
  public void should_get_trending_accessible_without_authentication() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when().get("/stats/trending").then().statusCode(200);
  }

  @Test
  public void should_return_404_for_stats_with_very_long_slug() throws Exception {
    String longSlug = "a".repeat(1000);
    when(articleStatisticsQueryService.getArticleStats(eq(longSlug))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/stats", longSlug).then().statusCode(404);
  }

  @Test
  public void should_propagate_exception_when_article_stats_service_fails() throws Exception {
    when(articleStatisticsQueryService.getArticleStats(any()))
        .thenThrow(new RuntimeException("Database error"));

    Assertions.assertThrows(
        NestedServletException.class,
        () -> RestAssuredMockMvc.when().get("/articles/{slug}/stats", "error-article"));
  }

  @Test
  public void should_propagate_exception_when_trending_service_fails() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles())
        .thenThrow(new RuntimeException("Database error"));

    Assertions.assertThrows(
        NestedServletException.class, () -> RestAssuredMockMvc.when().get("/stats/trending"));
  }
}
