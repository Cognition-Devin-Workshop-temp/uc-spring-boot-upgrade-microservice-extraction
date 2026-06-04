package io.spring.api;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
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
  public void should_get_article_stats_success() throws Exception {
    String slug = "test-article";
    ArticleStatsData statsData = new ArticleStatsData(slug, 42, 5, 3, 10);

    when(articleStatisticsQueryService.getArticleStats(eq(slug)))
        .thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(42))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(10));
  }

  @Test
  public void should_return_404_if_article_not_found_for_stats() throws Exception {
    when(articleStatisticsQueryService.getArticleStats(eq("nonexistent-article")))
        .thenReturn(Optional.empty());

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", "nonexistent-article")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_get_article_stats_with_zero_counts() throws Exception {
    String slug = "new-article";
    ArticleStatsData statsData = new ArticleStatsData(slug, 0, 0, 0, 0);

    when(articleStatisticsQueryService.getArticleStats(eq(slug)))
        .thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0))
        .body("stats.daysSincePublished", equalTo(0));
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    DateTime now = new DateTime();
    TrendingArticleData article1 =
        new TrendingArticleData("id1", "popular-article", "Popular Article", "Desc 1", 100, now);
    TrendingArticleData article2 =
        new TrendingArticleData(
            "id2", "another-popular", "Another Popular", "Desc 2", 50, now.minusDays(1));

    when(articleStatisticsQueryService.getTrendingArticles())
        .thenReturn(Arrays.asList(article1, article2));

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].title", equalTo("Popular Article"))
        .body("articles[0].favoriteCount", equalTo(100))
        .body("articles[1].slug", equalTo("another-popular"))
        .body("articles[1].favoriteCount", equalTo(50));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0));
  }

  @Test
  public void should_get_article_stats_with_high_counts() throws Exception {
    String slug = "viral-article";
    ArticleStatsData statsData = new ArticleStatsData(slug, 100000, 9999, 500, 365);

    when(articleStatisticsQueryService.getArticleStats(eq(slug)))
        .thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(100000))
        .body("stats.favoriteCount", equalTo(9999))
        .body("stats.commentCount", equalTo(500))
        .body("stats.daysSincePublished", equalTo(365));
  }
}
