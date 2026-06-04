package io.spring.api;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
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

@WebMvcTest({ArticleStatsApi.class})
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
    ArticleStatsData statsData = new ArticleStatsData("test-slug", 0, 5, 3, 10);
    when(articleStatsQueryService.getArticleStats(eq("test-slug")))
        .thenReturn(Optional.of(statsData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/stats", "test-slug")
        .then()
        .statusCode(200)
        .body("stats.slug", equalTo("test-slug"))
        .body("stats.viewCount", equalTo(0))
        .body("stats.favoriteCount", equalTo(5))
        .body("stats.commentCount", equalTo(3))
        .body("stats.daysSincePublished", equalTo(10));
  }

  @Test
  public void should_404_if_article_not_found_for_stats() throws Exception {
    when(articleStatsQueryService.getArticleStats(anyString())).thenReturn(Optional.empty());

    RestAssuredMockMvc.when().get("/articles/{slug}/stats", "nonexistent").then().statusCode(404);
  }
}
