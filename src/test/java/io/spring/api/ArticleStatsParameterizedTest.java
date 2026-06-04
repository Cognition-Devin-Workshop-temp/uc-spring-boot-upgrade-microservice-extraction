package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatsData;
import io.spring.application.data.TrendingArticleData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleStatsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatsParameterizedTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatisticsQueryService articleStatisticsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  // --- Data-driven: article stats with various count combinations ---

  @ParameterizedTest(name = "stats for slug={0}: views={2}, favs={3}, comments={4}, days={5}")
  @CsvSource({
    "new-post,          New Post,          0,     0,  0,   0",
    "viral-article,     Viral Article,     9999,  500, 200, 1",
    "old-classic,       Old Classic,       300,   50,  25,  365",
    "one-fav,           One Fav,           10,    1,   0,   7",
    "heavily-commented, Heavily Commented, 50,    0,   999, 30",
    "just-published,    Just Published,    1,     0,   0,   0",
    "max-values,        Max Values,        999999, 99999, 99999, 9999",
  })
  public void should_return_correct_stats_for_various_articles(
      String slug,
      String title,
      long viewCount,
      int favoriteCount,
      int commentCount,
      long daysSincePublished)
      throws Exception {
    ArticleStatsData stats =
        new ArticleStatsData(
            slug, title, viewCount, favoriteCount, commentCount, daysSincePublished);

    when(articleStatisticsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(stats));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("articleStats.slug", equalTo(slug))
        .body("articleStats.title", equalTo(title))
        .body("articleStats.viewCount", equalTo((int) viewCount))
        .body("articleStats.favoriteCount", equalTo(favoriteCount))
        .body("articleStats.commentCount", equalTo(commentCount))
        .body("articleStats.daysSincePublished", equalTo((int) daysSincePublished));
  }

  // --- Data-driven: not-found slugs ---

  @ParameterizedTest(name = "404 for nonexistent slug: {0}")
  @ValueSource(
      strings = {
        "nonexistent-article",
        "deleted-post-123",
        "this-was-never-created",
        "a",
        "slug-with-numbers-12345",
      })
  public void should_return_404_for_various_nonexistent_slugs(String slug) throws Exception {
    when(articleStatisticsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/stats", slug).then().statusCode(404);
  }

  // --- Data-driven: unauthorized HTTP methods on article stats endpoint ---

  @ParameterizedTest(name = "401 for {0} /articles/some-article/stats without auth")
  @ValueSource(strings = {"POST", "PUT", "DELETE"})
  public void should_return_401_for_non_get_methods_on_article_stats(String method)
      throws Exception {
    switch (method) {
      case "POST":
        given()
            .contentType("application/json")
            .body("{}")
            .when()
            .post("/articles/{slug}/stats", "some-article")
            .then()
            .statusCode(401);
        break;
      case "PUT":
        given()
            .contentType("application/json")
            .body("{}")
            .when()
            .put("/articles/{slug}/stats", "some-article")
            .then()
            .statusCode(401);
        break;
      case "DELETE":
        given().when().delete("/articles/{slug}/stats", "some-article").then().statusCode(401);
        break;
    }
  }

  // --- Data-driven: unauthorized HTTP methods on trending endpoint ---

  @ParameterizedTest(name = "401 for {0} /stats/trending without auth")
  @ValueSource(strings = {"POST", "PUT", "DELETE"})
  public void should_return_401_for_non_get_methods_on_trending(String method) throws Exception {
    switch (method) {
      case "POST":
        given()
            .contentType("application/json")
            .body("{}")
            .when()
            .post("/stats/trending")
            .then()
            .statusCode(401);
        break;
      case "PUT":
        given()
            .contentType("application/json")
            .body("{}")
            .when()
            .put("/stats/trending")
            .then()
            .statusCode(401);
        break;
      case "DELETE":
        given().when().delete("/stats/trending").then().statusCode(401);
        break;
    }
  }

  // --- Data-driven: trending articles with varying list sizes ---

  static Stream<Arguments> trendingListSizeProvider() {
    return Stream.of(
        Arguments.of(0, "empty trending list"),
        Arguments.of(1, "single trending article"),
        Arguments.of(5, "five trending articles"),
        Arguments.of(10, "max trending articles (10)"));
  }

  @ParameterizedTest(name = "trending returns {0} articles ({1})")
  @MethodSource("trendingListSizeProvider")
  public void should_return_trending_list_of_various_sizes(int size, String description)
      throws Exception {
    List<TrendingArticleData> trending = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      trending.add(
          new TrendingArticleData(
              "article-" + i, "Article " + i, "Desc " + i, "author" + i, 100 - (i * 10)));
    }

    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("trendingArticles", hasSize(size));
  }

  // --- Data-driven: trending article field verification across entries ---

  static Stream<Arguments> trendingArticleFieldProvider() {
    return Stream.of(
        Arguments.of("tech-ai", "Tech AI Guide", "AI overview", "alice", 100),
        Arguments.of("cooking-101", "Cooking 101", "Learn to cook", "bob", 75),
        Arguments.of("travel-tips", "Travel Tips", "Best places", "charlie", 50),
        Arguments.of("no-description", "No Desc", "", "dave", 25),
        Arguments.of("single-fav", "Single Fav", "Just one like", "eve", 1));
  }

  @ParameterizedTest(name = "trending article {0} by {3} with {4} favorites")
  @MethodSource("trendingArticleFieldProvider")
  public void should_return_correct_fields_for_trending_article(
      String slug, String title, String description, String author, int favoriteCount)
      throws Exception {
    TrendingArticleData article =
        new TrendingArticleData(slug, title, description, author, favoriteCount);

    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(List.of(article));

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("trendingArticles", hasSize(1))
        .body("trendingArticles[0].slug", equalTo(slug))
        .body("trendingArticles[0].title", equalTo(title))
        .body("trendingArticles[0].description", equalTo(description))
        .body("trendingArticles[0].authorUsername", equalTo(author))
        .body("trendingArticles[0].favoriteCount", equalTo(favoriteCount));
  }
}
