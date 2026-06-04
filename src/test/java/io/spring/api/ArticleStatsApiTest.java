package io.spring.api;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleStatsData;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleStatsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleStatsApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_article_stats_success() throws Exception {
    String slug = "getting-started-with-spring-boot";
    ArticleStatsData statsData = new ArticleStatsData(slug, 0, 2, 3, 7);

    when(articleStatsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/api/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo(slug))
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(2))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(7));
  }

  @Test
  public void should_return_404_when_article_not_found() throws Exception {
    when(articleStatsQueryService.getArticleStats(eq("non-existent-slug")))
        .thenReturn(Optional.empty());

    RestAssuredMockMvc.when()
        .get("/api/articles/{slug}/stats", "non-existent-slug")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_return_zero_counts_for_new_article() throws Exception {
    String slug = "brand-new-article";
    ArticleStatsData statsData = new ArticleStatsData(slug, 0, 0, 0, 0);

    when(articleStatsQueryService.getArticleStats(eq(slug))).thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/api/articles/{slug}/stats", slug)
        .then()
        .statusCode(200)
        .body("stats.favoriteCount", equalTo(0))
        .body("stats.commentCount", equalTo(0))
        .body("stats.daysSincePublished", equalTo(0));
  }
}
