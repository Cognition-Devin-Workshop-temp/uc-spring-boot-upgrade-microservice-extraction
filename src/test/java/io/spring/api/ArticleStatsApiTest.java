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
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticleStatsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatsApiTest extends TestWithCurrentUser {
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
        new Article(
            "Test New Article", "Desc", "Body", Arrays.asList("java", "spring"), user.getId());
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    String slug = article.getSlug();
    ArticleStatsData statsData = new ArticleStatsData(slug, 42, 5, 3, 7);

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
    when(articleStatisticsQueryService.getArticleStats(eq(article.getId()))).thenReturn(statsData);

    given()
        .when()
        .get("/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(42))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(7));
  }

  @Test
  public void should_return_404_when_article_not_found_for_stats() throws Exception {
    when(articleRepository.findBySlug(eq("not-exists"))).thenReturn(Optional.empty());

    given().when().get("/articles/{slug}/stats", "not-exists").then().statusCode(404);
  }
}
