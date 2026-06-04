package io.spring.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ProfileData;
import io.spring.application.data.TrendingArticleData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StatsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TrendingApiTest extends TestWithCurrentUser {

  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles() throws Exception {
    ProfileData author = new ProfileData("user-1", "johndoe", "bio", "image-url", false);
    DateTime now = new DateTime();

    List<TrendingArticleData> trending =
        Arrays.asList(
            new TrendingArticleData(
                "popular-article", "Popular Article", "desc1", 10, now.minusDays(2), author),
            new TrendingArticleData(
                "another-hit", "Another Hit", "desc2", 5, now.minusDays(1), author));

    when(articleStatsQueryService.getTrendingArticles(eq(10), eq(7))).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].favoriteCount", equalTo(10))
        .body("articles[1].slug", equalTo("another-hit"))
        .body("articles[1].favoriteCount", equalTo(5));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatsQueryService.getTrendingArticles(eq(10), eq(7))).thenReturn(new ArrayList<>());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0));
  }

  @Test
  public void should_return_at_most_ten_trending_articles() throws Exception {
    ProfileData author = new ProfileData("user-1", "johndoe", "bio", "image-url", false);
    DateTime now = new DateTime();

    List<TrendingArticleData> trending = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      trending.add(
          new TrendingArticleData(
              "article-" + i, "Article " + i, "desc", 100 - i, now.minusDays(1), author));
    }

    when(articleStatsQueryService.getTrendingArticles(eq(10), eq(7))).thenReturn(trending);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(10));
  }
}
