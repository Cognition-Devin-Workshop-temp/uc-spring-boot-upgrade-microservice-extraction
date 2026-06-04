package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.TrendingArticleData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({TrendingArticlesApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TrendingArticlesApiTest extends TestWithCurrentUser {
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
    List<TrendingArticleData> trendingList =
        Arrays.asList(
            new TrendingArticleData("id1", "slug-1", "Title 1", "Desc 1", 10),
            new TrendingArticleData("id2", "slug-2", "Title 2", "Desc 2", 5));

    when(articleStatsQueryService.getTrendingArticles()).thenReturn(trendingList);

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("trendingArticles", hasSize(2))
        .body("trendingArticles[0].slug", equalTo("slug-1"))
        .body("trendingArticles[0].favoriteCount", equalTo(10))
        .body("trendingArticles[1].slug", equalTo("slug-2"));
  }

  @Test
  public void should_return_empty_list_when_no_trending() throws Exception {
    when(articleStatsQueryService.getTrendingArticles()).thenReturn(Collections.emptyList());

    RestAssuredMockMvc.when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("trendingArticles", hasSize(0));
  }
}
