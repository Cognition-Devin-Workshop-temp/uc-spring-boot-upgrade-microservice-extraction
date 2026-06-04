package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatisticsQueryService;
import io.spring.application.data.TrendingArticleData;
import java.util.ArrayList;
import java.util.Arrays;
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

  @MockBean private ArticleStatisticsQueryService articleStatisticsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    List<TrendingArticleData> trendingArticles =
        Arrays.asList(
            new TrendingArticleData("popular-article", "Popular Article", 10, "author1"),
            new TrendingArticleData("another-hit", "Another Hit", 8, "author2"));

    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(trendingArticles);

    given()
        .when()
        .get("/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(2))
        .body("articles[0].slug", equalTo("popular-article"))
        .body("articles[0].title", equalTo("Popular Article"))
        .body("articles[0].favoriteCount", equalTo(10))
        .body("articles[0].author", equalTo("author1"))
        .body("articles[1].slug", equalTo("another-hit"))
        .body("articles[1].favoriteCount", equalTo(8));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    when(articleStatisticsQueryService.getTrendingArticles()).thenReturn(new ArrayList<>());

    given().when().get("/stats/trending").then().statusCode(200).body("articles", hasSize(0));
  }
}
