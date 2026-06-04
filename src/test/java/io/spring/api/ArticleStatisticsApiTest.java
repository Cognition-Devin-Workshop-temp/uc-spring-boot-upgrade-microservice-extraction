package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.ArticleStatisticsData;
import io.spring.application.data.TrendingArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleStatisticsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatisticsApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private ArticleRepository articleRepository;

  @MockBean private ArticleStatisticsQueryService articleStatisticsQueryService;

  private Article article;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
    article =
        new Article("Test Article", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug())))
        .thenReturn(Optional.of(article));
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    ArticleStatisticsData stats = new ArticleStatisticsData(10, 5, 3, 7);

    when(articleStatisticsQueryService.getArticleStatistics(eq(article.getId())))
        .thenReturn(stats);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", article.getSlug())
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(10))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(7));
  }

  @Test
  public void should_get_404_if_article_not_found_for_stats() throws Exception {
    when(articleRepository.findBySlug(eq("non-existent")))
        .thenReturn(Optional.empty());

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", "non-existent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_get_stats_with_zero_counts() throws Exception {
    ArticleStatisticsData stats = new ArticleStatisticsData(0, 0, 0, 0);

    when(articleStatisticsQueryService.getArticleStatistics(eq(article.getId())))
        .thenReturn(stats);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", article.getSlug())
        .then()
        .statusCode(200)
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0))
        .body("stats.daysSincePublished", equalTo(0));
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    TrendingArticleData trending1 =
        new TrendingArticleData("article-1", "trending-article", "Trending Article", 15);
    TrendingArticleData trending2 =
        new TrendingArticleData("article-2", "another-trending", "Another Trending", 10);

    when(articleStatisticsQueryService.getTrendingArticles())
        .thenReturn(Arrays.asList(trending1, trending2));

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articles[0].slug", equalTo("trending-article"))
        .body("articles[0].title", equalTo("Trending Article"))
        .body("articles[0].favoriteCount", equalTo(15))
        .body("articles[1].slug", equalTo("another-trending"))
        .body("articles[1].favoriteCount", equalTo(10));
  }

  @Test
  public void should_get_empty_trending_when_no_articles() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles())
        .thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0));
  }

  @Test
  public void should_get_article_stats_without_authentication() throws Exception {
    ArticleStatisticsData stats = new ArticleStatisticsData(5, 3, 1, 2);

    when(articleStatisticsQueryService.getArticleStatistics(eq(article.getId())))
        .thenReturn(stats);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", article.getSlug())
        .then()
        .statusCode(200)
        .body("stats.favoriteCount", equalTo(3));
  }

  @Test
  public void should_get_trending_articles_without_authentication() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles())
        .thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200);
  }
}
