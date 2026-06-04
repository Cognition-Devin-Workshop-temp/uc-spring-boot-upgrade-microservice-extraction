package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.TestHelper;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleStatsQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.user.User;
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

@WebMvcTest({StatsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class StatsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleStatsQueryService articleStatsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_trending_articles_success() throws Exception {
    User author = new User("author@test.com", "author", "123", "bio", "image");
    ArticleData article1 = TestHelper.articleDataFixture("1", author);
    ArticleData article2 = TestHelper.articleDataFixture("2", author);
    ArticleData article3 = TestHelper.articleDataFixture("3", author);

    List<ArticleData> articles = Arrays.asList(article1, article2, article3);
    ArticleDataList articleDataList = new ArticleDataList(articles, 3);

    when(articleStatsQueryService.getTrendingArticles()).thenReturn(articleDataList);

    given()
        .when()
        .get("/api/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(3))
        .body("articlesCount", equalTo(3))
        .body("articles[0].slug", equalTo(article1.getSlug()));
  }

  @Test
  public void should_return_empty_list_when_no_trending_articles() throws Exception {
    ArticleDataList emptyList = new ArticleDataList(new ArrayList<>(), 0);

    when(articleStatsQueryService.getTrendingArticles()).thenReturn(emptyList);

    given()
        .when()
        .get("/api/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(0))
        .body("articlesCount", equalTo(0));
  }

  @Test
  public void should_return_at_most_ten_trending_articles() throws Exception {
    User author = new User("author@test.com", "author", "123", "bio", "image");
    List<ArticleData> articles = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      articles.add(TestHelper.articleDataFixture(String.valueOf(i), author));
    }
    ArticleDataList articleDataList = new ArticleDataList(articles, 10);

    when(articleStatsQueryService.getTrendingArticles()).thenReturn(articleDataList);

    given()
        .when()
        .get("/api/stats/trending")
        .then()
        .statusCode(200)
        .body("articles", hasSize(10))
        .body("articlesCount", equalTo(10));
  }

  @Test
  public void should_return_trending_articles_with_correct_structure() throws Exception {
    User author = new User("author@test.com", "author", "123", "bio", "image");
    ArticleData article = TestHelper.articleDataFixture("trending", author);

    List<ArticleData> articles = Arrays.asList(article);
    ArticleDataList articleDataList = new ArticleDataList(articles, 1);

    when(articleStatsQueryService.getTrendingArticles()).thenReturn(articleDataList);

    given()
        .when()
        .get("/api/stats/trending")
        .then()
        .statusCode(200)
        .body("articles[0].title", equalTo(article.getTitle()))
        .body("articles[0].description", equalTo(article.getDescription()))
        .body("articles[0].slug", equalTo(article.getSlug()));
  }
}
